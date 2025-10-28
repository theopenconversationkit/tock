#   Copyright (C) 2023-2025 Credit Mutuel Arkea
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
    smarttribune_formatter.py [-v] <input_csv> <tag_title> <base_url> <output_csv>

Arguments:
    input_csv   path to the Smart Tribune CSV export file
    tag_title   tag title to discrimate FAQ source ('Tag (ID system)' column
                will be filtered for lines containing this tag)
    base_url    the base URL to prefix every FAQ entry's query parameter to
                create a full URL
    output_csv  path to the output, ready-to-index CSV file

Options:
    -h --help   Show this screen
    --version   Show version
    -v          Verbose output for debugging (without this option, script will
                be silent but for errors)

Turns a Smart Tribune CSV export file into a ready-to-index CSV file
(one 'title'|'source'|'text' line per filtered entry).
"""
import logging
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
    """
    logging.debug(f"Read input CSV file {args['<input_csv>']}")
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
            prefixed_column: 'source',
            'FAQ answer (text)': 'text',
        }
    )
    logging.debug(f"Export to output CSV file {args['<output_csv>']}")
    result_df.to_csv(args['<output_csv>'], sep='|', index=False)


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
            f"Cannot proceed: input CSV file '{cli_args['<input_csv>']}' does not exist"
        )
        sys.exit(1)

    # - tag title is arbitrary

    # - base url must be valid
    result = urlparse(cli_args['<base_url>'])
    if not result.scheme or not result.netloc:
        logging.error(f"Cannot proceed: '{cli_args['<base_url>']}' is not a valid URL")
        sys.exit(1)

    # - output file path
    target_dir = Path(cli_args['<output_csv>']).parent
    if not target_dir.exists():
        logging.error(f'Cannot proceed: directory {target_dir} does not exist')
        sys.exit(1)

    # Main func
    format(cli_args)
