#   Copyright (C) 2023-2024 Credit Mutuel Arkea
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#
"""
CSV Generator. This script takes a directory of JSON files from TOCK Studio FAQ export as input and generates a CSV file ready to ingest as output.

Usage:
    export_csv_to_ingest_data.py [-v] <input_directory> [--output=<name>] [--label=<label>]

Arguments:
    input_directory    Path to the directory containing JSON files.

Options:
    --output=<name>      name of the output CSV file to be generated [default: output_faq_story.csv].
    --label=<label>      label name, name of the folder in ready-to_index_file folder where csv while be created.
    -h --help            Show this screen.
    -v                   Verbose output for debugging (without this option, the script will be silent but for errors).


This script processes all JSON files from TOCK Studio FAQ export in the given directory and combines their contents into a single CSV file ready to ingest as output.. The output file will contain three columns: title, url (empty), and text.
"""

import csv
import json
import logging
import os
from pathlib import Path

from docopt import docopt


def json_to_csv(input_directory: str, output_csv: str, label: str):
    """Parses JSON files from the given directory and writes to a CSV file."""
    output_directory = "ready-to_index_file/" + label if label else "ready-to_index_file"
    if not os.path.exists(output_directory):
        os.makedirs(output_directory)
        logging.debug(f"Répertoire de sortie créé : {output_directory}")
    # Chemin du fichier CSV de sortie
    csv_file_path = os.path.join(output_directory, 'output.csv')
    try:
        with open(csv_file_path, 'w', newline='', encoding='utf-8') as csvfile:
            csvwriter = csv.writer(csvfile, delimiter='|')
            csvwriter.writerow(['title', 'url', 'text'])

            for filename in os.listdir(input_directory):
                if filename.endswith('.json'):
                    filepath = os.path.join(input_directory, filename)
                    logging.debug('Processing file: %s', filepath)

                    with open(filepath, 'r', encoding='utf-8') as f:
                        data = json.load(f)

                    for utterance in data.get('utterances', []):
                        csvwriter.writerow([utterance, '', data.get('answer', '')])

    except Exception as e:
        logging.error('Failed to generate CSV: %s', e)
        raise


def main():
    """Main entry point for the script."""
    cli_args = docopt(__doc__)

    input_directory = cli_args['<input_directory>']
    output_csv = cli_args['--output']
    label = cli_args['--label']

    # Set logging level
    log_format = '%(levelname)s:%(module)s:%(message)s'
    logging.basicConfig(level=logging.DEBUG if cli_args['-v'] else logging.WARNING, format=log_format)

    # Check if input directory exists
    if not os.path.isdir(input_directory):
        logging.error('Specified input directory does not exist.')
        exit(1)

    # Check if output directory is valid
    if not Path(output_csv).parent.exists():
        logging.error('Cannot proceed: directory %s does not exist', Path(output_csv).parent)
        exit(1)

    # Process JSON files and generate the CSV
    logging.info('Generating CSV file: %s', output_csv)
    json_to_csv(input_directory, output_csv, label)
    logging.info('CSV generation completed successfully.')


if __name__ == '__main__':
    main()
