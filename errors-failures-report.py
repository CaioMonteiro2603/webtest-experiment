import os
import xml.etree.ElementTree as ET
from collections import defaultdict
import pandas as pd
import re

def parse_surefire_reports(directory, model):
    error_counts = defaultdict(int)
    failure_counts = defaultdict(int)
    total_errors = 0
    total_failures = 0
    total_tests = 0

    # Pattern to match XML files for the specific model
    pattern = re.compile(rf"^TEST-{re.escape(model)}\..*\.xml$", re.IGNORECASE)
    
    for file in os.listdir(directory):
        if pattern.match(file):
            filepath = os.path.join(directory, file)
            try:
                tree = ET.parse(filepath)
                root = tree.getroot()
                
                # Get total tests from testsuite attributes
                tests_in_suite = int(root.attrib.get("tests", 0))
                total_tests += tests_in_suite

                for testcase in root.findall("testcase"):
                    error = testcase.find("error")
                    failure = testcase.find("failure")

                    if error is not None:
                        err_type = error.attrib.get("type", "UnknownError")
                        error_counts[err_type] += 1
                        total_errors += 1

                    if failure is not None:
                        fail_type = failure.attrib.get("type", "UnknownFailure")
                        failure_counts[fail_type] += 1
                        total_failures += 1
            except ET.ParseError:
                print(f"Failed to parse XML: {filepath}")
            except Exception as e:
                print(f"Error processing {filepath}: {e}")

    # Prepare CSV data
    csv_data = [("Total Number of Tests", total_tests)]
    csv_data.append(("Number of Errors", total_errors))
    csv_data.extend(sorted(error_counts.items(), key=lambda x: -x[1]))
    csv_data.append(("Number of Failures", total_failures))
    csv_data.extend(sorted(failure_counts.items(), key=lambda x: -x[1]))

    return pd.DataFrame(csv_data, columns=["Type", "Count"])

def generate_csv_report(xml_dir, model, output_csv):
    df = parse_surefire_reports(xml_dir, model)
    df.to_csv(output_csv, index=False)
    print(f"CSV report generated at: {output_csv}")


if __name__ == "__main__":
    models = [
        "deepseek",
        "geminiPro",
        "GPT20b",
        "GPT4",
        "GPT5",
        "Qwen3",
        "SunaDeepSeek",
        "SunaGPT20b",
        "SunaQwen3"
    ]
    
    xml_directory = "./target/surefire-reports"
    
    for model in models:
        output_csv_path = f"surefire_summary_{model}.csv"
        print(f"Processing model: {model}")
        generate_csv_report(xml_directory, model, output_csv_path)
        print(f"Completed: {output_csv_path}")