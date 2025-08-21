import os
import xml.etree.ElementTree as ET
from collections import defaultdict
import pandas as pd

def parse_surefire_reports(directory):
    error_counts = defaultdict(int)
    failure_counts = defaultdict(int)
    total_errors = 0
    total_failures = 0

    for root_dir, _, files in os.walk(directory):
        for file in files:
            if file.endswith(".xml"):
                filepath = os.path.join(root_dir, file)
                try:
                    tree = ET.parse(filepath)
                    root = tree.getroot()

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

    # Prepare CSV data
    csv_data = [("Number of Errors", total_errors)]
    csv_data.extend(sorted(error_counts.items(), key=lambda x: -x[1]))
    csv_data.append(("Number of Failures", total_failures))
    csv_data.extend(sorted(failure_counts.items(), key=lambda x: -x[1]))

    return pd.DataFrame(csv_data, columns=["Type", "Count"])

def generate_csv_report(xml_dir, output_csv):
    df = parse_surefire_reports(xml_dir)
    df.to_csv(output_csv, index=False)
    print(f"CSV report generated at: {output_csv}")


# Example usage:
if __name__ == "__main__":
    xml_directory = "./target/surefire-reports/deepseek"  # Replace with your directory path
    output_csv_path = "surefire_summary_deepseek.csv"
    generate_csv_report(xml_directory, output_csv_path)

