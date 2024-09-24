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
"""Smart Tribune export file formatter.

Usage:
    smarttribune_formatter.py [-v] <input_csv> <tag_title> <base_url> <output_csv> [--label=<label>]

Arguments:
    input_csv   path to the Smart Tribune CSV export file
    tag_title   tag title to discrimate FAQ source ('Tag (ID system)' column
                will be filtered for lines containing this tag)
    base_url    the base URL to prefix every FAQ entry's query parameter to
                create a full URL
    output_csv  name of the output csv, ready-to-index CSV file

Options:
    --label=<label> label name, name of the folder in ready-to_index_file folder where csv while be created.
    -h --help   Show this screen
    --version   Show version
    -v          Verbose output for debugging (without this option, script will
                be silent but for errors)

Turns a Smart Tribune CSV export file into a ready-to-index CSV file
(one 'title'|'url'|'text' line per filtered entry).
"""
import logging
import os
import sys
from pathlib import Path
from urllib.parse import urlparse

import pandas as pd
from docopt import docopt


def format(args):
    """
    Read a CSV Smart Tribune export then format it into a ready-to-index CSV file.

        Parameters:
        args (dict): A dictionary containing command-line arguments.
                    Expecting keys:    '<input_csv>'
                                        '<tag_title>'
                                        '<base_url>'
                                        '<output_csv>'
                                        '--label'
    """
    logging.debug("Read input CSV file %s", args['<input_csv>'])
    df = pd.read_csv(args['<input_csv>'], sep='|', encoding='utf-8')
    # Filter entries based on id
    filtered_df = df[
        df['Tag (ID system)'].str.contains(args['<tag_title>'], na=False)
    ].copy()  # Ensure copy (not a view)
    # Add common prefix to all lines' URL
    prefixed_column = 'Prefixed URL'
    filtered_df.loc[:, prefixed_column] = (
        args['<base_url>'] + filtered_df['Question URL']
    )
    # Select only destination columns
    result_df = filtered_df[['Question Title', prefixed_column, 'FAQ answer (text)']]
    # Rename the columns
    result_df = result_df.rename(
        columns={
            'Question Title': 'title',
            prefixed_column: 'url',
            'FAQ answer (text)': 'text',
        }
    )
    label = args.get('--label')
    output_directory = f"ready-to_index_file/{label}" if label else "ready-to_index_file"
    if not os.path.exists(output_directory):
        os.makedirs(output_directory)
        logging.debug("Répertoire de sortie créé : %s", output_directory)
    # Chemin du fichier CSV de sortie
    csv_file_path = os.path.join(output_directory, args.get('<output_csv>'))

    logging.debug("Export to output CSV file %s", csv_file_path)
    result_df.to_csv(csv_file_path, sep='|', index=False)


if __name__ == '__main__':
    cli_args = docopt(__doc__, version='Smart Tribune formatter 0.1.0')

    # Set logging level
    log_format = '%(levelname)s:%(module)s:%(message)s'
    logging.basicConfig(
        level=logging.DEBUG if cli_args['-v'] else logging.WARNING, format=log_format
    )

    # Check args:
    # - input file path
    inputfile_path = Path(cli_args['<input_csv>'])
    if not inputfile_path.exists():
        logging.error(
            "Cannot proceed: input CSV file '%s' does not exist", cli_args['<input_csv>']
        )
        sys.exit(1)

    # - tag title is arbitrary

    # - base url must be valid
    result = urlparse(cli_args['<base_url>'])
    if not result.scheme or not result.netloc:
        logging.error("Cannot proceed: '%s' is not a valid URL", cli_args['<base_url>'])
        sys.exit(1)

    # Main func
    format(cli_args)
