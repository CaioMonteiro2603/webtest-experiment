import os
import sys
import xml.etree.ElementTree as ET
import argparse
import re
from pathlib import Path
import requests
import json
import time
from datetime import datetime

def parse_xml_filename(xml_filename):
    """
    Parse XML filename in format: TEST-{model}.{ws}.{seq}.{test-file-name}.xml
    Returns: (model, ws, seq, test_file_name)
    """
    match = re.match(r'^TEST-(.+?)\.(.+?)\.(.+?)\.(.+?)\.xml$', xml_filename)
    if match:
        return match.groups()
    return None

def get_test_results(xml_path):
    """
    Parse surefire XML report and extract test results.
    Returns: dict with test results including errors and failures
    """
    try:
        tree = ET.parse(xml_path)
        root = tree.getroot()
        
        results = {
            'total_tests': int(root.attrib.get('tests', 0)),
            'errors': int(root.attrib.get('errors', 0)),
            'failures': int(root.attrib.get('failures', 0)),
            'test_cases': []
        }
        
        for testcase in root.findall('testcase'):
            test_name = testcase.attrib.get('name', '')
            test_class = testcase.attrib.get('classname', '')
            
            error = testcase.find('error')
            failure = testcase.find('failure')
            
            test_info = {
                'name': test_name,
                'classname': test_class,
                'status': 'passed'
            }
            
            if error is not None:
                test_info['status'] = 'error'
                test_info['type'] = error.attrib.get('type', 'Unknown')
                test_info['message'] = error.attrib.get('message', '')
                test_info['details'] = error.text or ''
            elif failure is not None:
                test_info['status'] = 'failure'
                test_info['type'] = failure.attrib.get('type', 'Unknown')
                test_info['message'] = failure.attrib.get('message', '')
                test_info['details'] = failure.text or ''
            
            results['test_cases'].append(test_info)
        
        return results
    except Exception as e:
        print(f"Error parsing XML {xml_path}: {e}")
        return None

def read_java_file(java_path):
    """Read Java test file content"""
    try:
        with open(java_path, 'r', encoding='utf-8') as f:
            return f.read()
    except Exception as e:
        print(f"Error reading Java file {java_path}: {e}")
        return None

def call_llm_to_fix_tests(java_content, test_results, llm_model, api_key, max_retries=5):
    """
    Call LLM via OpenRouter to fix failing tests with retry logic for rate limits
    """
    # Prepare the prompt
    failed_tests = [tc for tc in test_results['test_cases'] 
                   if tc['status'] in ['error', 'failure']]
    passed_tests = [tc for tc in test_results['test_cases'] 
                   if tc['status'] == 'passed']
    
    failure_details = "\n\n".join([
        f"Test: {tc['name']}\n"
        f"Status: {tc['status']}\n"
        f"Type: {tc.get('type', 'N/A')}\n"
        f"Message: {tc.get('message', 'N/A')}\n"
        f"Details:\n{tc.get('details', 'N/A')}"
        for tc in failed_tests
    ])
    
    passed_test_names = [tc['name'] for tc in passed_tests]
    
    prompt = f"""You are an expert web test automation engineer. I have a Selenium WebDriver test suite written in Java that has some failing tests.

Your task is to:
1. Keep all test cases that passed correctly (DO NOT modify them)
2. Fix the test cases that failed or had errors
3. Return the COMPLETE fixed Java test file

**Passed tests (keep these exactly as they are):**
{', '.join(passed_test_names) if passed_test_names else 'None'}

**Failed/Error tests that need fixing:**
{failure_details}

**Current Java test file:**
```java
{java_content}
```

**Instructions:**
- Analyze the error messages and stack traces carefully
- Common issues include: incorrect element locators, timing issues, wrong assertions, missing waits
- For TimeoutException: the element locator might be wrong or the wait time insufficient
- For NoSuchElementException: the element locator is incorrect or the element doesn't exist
- For AssertionFailedError: the expected value or condition is wrong
- Keep the same package, imports, and class structure
- Maintain the same test method names
- DO NOT modify tests that passed
- Return ONLY the complete Java code, no explanations before or after

Return the complete fixed Java test file:"""

    retry_count = 0
    base_wait_time = 2  # Start with 2 seconds
    
    while retry_count < max_retries:
        try:
            response = requests.post(
                "https://openrouter.ai/api/v1/chat/completions",
                headers={
                    "Authorization": f"Bearer {api_key}",
                    "Content-Type": "application/json",
                    "HTTP-Referer": "https://github.com/test-automation",  # Optional but recommended
                    "X-Title": "Selenium Test Fixer"  # Optional but recommended
                },
                json={
                    "model": llm_model,
                    "messages": [
                        {
                            "role": "user",
                            "content": prompt
                        }
                    ]
                },
                timeout=300
            )
            
            # Check for rate limit or other API errors
            if response.status_code == 429:
                # Rate limit exceeded
                retry_after = int(response.headers.get('Retry-After', base_wait_time * (2 ** retry_count)))
                print(f"  ⚠ Rate limit exceeded. Waiting {retry_after} seconds before retry {retry_count + 1}/{max_retries}...")
                time.sleep(retry_after)
                retry_count += 1
                continue
            
            elif response.status_code == 402:
                # Insufficient credits
                print(f"  ✗ Error 402: Insufficient credits or negative balance. Please add credits to your OpenRouter account.")
                return None
            
            elif response.status_code == 503:
                # Service unavailable (model might be overloaded)
                wait_time = base_wait_time * (2 ** retry_count)
                print(f"  ⚠ Service unavailable (503). Waiting {wait_time} seconds before retry {retry_count + 1}/{max_retries}...")
                time.sleep(wait_time)
                retry_count += 1
                continue
            
            elif response.status_code >= 500:
                # Server error - retry with exponential backoff
                wait_time = base_wait_time * (2 ** retry_count)
                print(f"  ⚠ Server error ({response.status_code}). Waiting {wait_time} seconds before retry {retry_count + 1}/{max_retries}...")
                time.sleep(wait_time)
                retry_count += 1
                continue
            
            # Raise exception for other HTTP errors
            response.raise_for_status()
            
            # Success - parse response
            result = response.json()
            
            if 'error' in result:
                print(f"  ✗ API Error: {result['error']}")
                return None
            
            fixed_code = result['choices'][0]['message']['content']
            
            # Extract Java code from markdown code blocks if present
            code_match = re.search(r'```java\n(.*?)\n```', fixed_code, re.DOTALL)
            if code_match:
                fixed_code = code_match.group(1)
            
            return fixed_code
            
        except requests.exceptions.Timeout:
            wait_time = base_wait_time * (2 ** retry_count)
            print(f"  ⚠ Request timeout. Waiting {wait_time} seconds before retry {retry_count + 1}/{max_retries}...")
            time.sleep(wait_time)
            retry_count += 1
            
        except requests.exceptions.ConnectionError as e:
            wait_time = base_wait_time * (2 ** retry_count)
            print(f"  ⚠ Connection error: {e}. Waiting {wait_time} seconds before retry {retry_count + 1}/{max_retries}...")
            time.sleep(wait_time)
            retry_count += 1
            
        except requests.exceptions.RequestException as e:
            print(f"  ✗ Request error: {e}")
            return None
            
        except Exception as e:
            print(f"  ✗ Unexpected error calling LLM: {e}")
            return None
    
    print(f"  ✗ Max retries ({max_retries}) exceeded. Giving up on this file.")
    return None

def save_fixed_test(fixed_content, model, ws, seq, test_file_name, output_base_dir):
    """
    Save fixed test file to new location
    """
    # Create directory structure
    output_dir = Path(output_base_dir) / model / ws / seq
    output_dir.mkdir(parents=True, exist_ok=True)
    
    # Save file
    output_path = output_dir / f"{test_file_name}.java"
    try:
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(fixed_content)
        print(f"  ✓ Fixed test saved to: {output_path}")
        return True
    except Exception as e:
        print(f"  ✗ Error saving fixed test: {e}")
        return False

def process_test_suite(reports_dir, tests_dir, output_dir, llm_model, api_key, dry_run=False, delay_between_requests=1.0):
    """
    Main processing function
    """
    reports_path = Path(reports_dir)
    tests_path = Path(tests_dir)
    
    if not reports_path.exists():
        print(f"Error: Reports directory not found: {reports_dir}")
        return
    
    if not tests_path.exists():
        print(f"Error: Tests directory not found: {tests_dir}")
        return
    
    # Get all XML files
    xml_files = list(reports_path.glob("TEST-*.xml"))
    print(f"Found {len(xml_files)} XML report files\n")
    
    stats = {
        'total_files': 0,
        'files_with_failures': 0,
        'files_fixed': 0,
        'files_skipped': 0,
        'files_failed': 0,
        'start_time': datetime.now()
    }
    
    for idx, xml_file in enumerate(xml_files):
        stats['total_files'] += 1
        
        # Parse filename
        parsed = parse_xml_filename(xml_file.name)
        if not parsed:
            print(f"⚠ Skipping file with invalid format: {xml_file.name}")
            stats['files_skipped'] += 1
            continue
        
        model, ws, seq, test_file_name = parsed
        
        print(f"\n{'='*80}")
        print(f"Processing [{idx + 1}/{len(xml_files)}]: {xml_file.name}")
        print(f"  Model: {model}, WS: {ws}, Seq: {seq}, Test: {test_file_name}")
        
        # Get test results
        results = get_test_results(xml_file)
        if not results:
            stats['files_skipped'] += 1
            continue
        
        print(f"  Tests: {results['total_tests']}, "
              f"Errors: {results['errors']}, "
              f"Failures: {results['failures']}")
        
        # Check if there are failures or errors
        if results['errors'] == 0 and results['failures'] == 0:
            print("  ✓ All tests passed, skipping...")
            continue
        
        stats['files_with_failures'] += 1
        
        # Find corresponding Java file
        java_path = tests_path / model / ws / seq / f"{test_file_name}.java"
        if not java_path.exists():
            print(f"  ✗ Java file not found: {java_path}")
            stats['files_skipped'] += 1
            continue
        
        # Read Java file
        java_content = read_java_file(java_path)
        if not java_content:
            stats['files_skipped'] += 1
            continue
        
        if dry_run:
            print("  [DRY RUN] Would fix this file")
            continue
        
        # Call LLM to fix tests
        print(f"  → Calling LLM to fix tests... (API delay: {delay_between_requests}s)")
        fixed_content = call_llm_to_fix_tests(java_content, results, llm_model, api_key)
        
        if not fixed_content:
            print("  ✗ Failed to get fixed code from LLM")
            stats['files_failed'] += 1
            continue
        
        # Save fixed test
        if save_fixed_test(fixed_content, model, ws, seq, test_file_name, output_dir):
            stats['files_fixed'] += 1
        else:
            stats['files_failed'] += 1
        
        # Add delay between requests to avoid rate limiting (only if not the last file)
        if idx < len(xml_files) - 1 and delay_between_requests > 0:
            time.sleep(delay_between_requests)
    
    # Calculate elapsed time
    elapsed = datetime.now() - stats['start_time']
    
    # Print summary
    print(f"\n{'='*80}")
    print("SUMMARY")
    print(f"{'='*80}")
    print(f"Total XML files processed: {stats['total_files']}")
    print(f"Files with failures/errors: {stats['files_with_failures']}")
    print(f"Files successfully fixed: {stats['files_fixed']}")
    print(f"Files skipped: {stats['files_skipped']}")
    print(f"Files failed to fix: {stats['files_failed']}")
    print(f"Total time elapsed: {elapsed}")
    print(f"{'='*80}")

def main():
    parser = argparse.ArgumentParser(
        description='Fix failing Selenium tests using LLM'
    )
    parser.add_argument(
        '--reports-dir',
        default='./target/surefire-reports',
        help='Directory containing XML surefire reports (default: ./target/surefire-reports)'
    )
    parser.add_argument(
        '--tests-dir',
        default='./src/test/java',
        help='Directory containing Java test files (default: ./src/test/java)'
    )
    parser.add_argument(
        '--output-dir',
        default='./src/test/new',
        help='Output directory for fixed tests (default: ./src/test/new)'
    )
    parser.add_argument(
        '--llm-model',
        default='moonshotai/kimi-k2-0905',
        help='LLM model to use via OpenRouter (default: moonshotai/kimi-k2-0905)'
    )
    parser.add_argument(
        '--api-key',
        help='OpenRouter API key (or set OPENROUTER_API_KEY env variable)'
    )
    parser.add_argument(
        '--dry-run',
        action='store_true',
        help='Perform a dry run without actually fixing tests'
    )
    parser.add_argument(
        '--delay',
        type=float,
        default=1.0,
        help='Delay in seconds between API requests to avoid rate limiting (default: 1.0)'
    )
    parser.add_argument(
        '--max-retries',
        type=int,
        default=5,
        help='Maximum number of retries for failed API requests (default: 5)'
    )
    
    args = parser.parse_args()
    
    # Get API key
    api_key = args.api_key or os.environ.get('OPENROUTER_API_KEY')
    if not api_key and not args.dry_run:
        print("Error: OpenRouter API key required. Set --api-key or OPENROUTER_API_KEY environment variable")
        sys.exit(1)
    
    print(f"Configuration:")
    print(f"  Reports directory: {args.reports_dir}")
    print(f"  Tests directory: {args.tests_dir}")
    print(f"  Output directory: {args.output_dir}")
    print(f"  LLM model: {args.llm_model}")
    print(f"  Dry run: {args.dry_run}")
    print(f"  Delay between requests: {args.delay}s")
    print(f"  Max retries: {args.max_retries}")
    
    process_test_suite(
        args.reports_dir,
        args.tests_dir,
        args.output_dir,
        args.llm_model,
        api_key,
        args.dry_run,
        args.delay
    )

if __name__ == "__main__":
    main()