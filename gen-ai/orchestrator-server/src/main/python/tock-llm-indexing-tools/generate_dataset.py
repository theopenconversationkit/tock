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
"""Dataset generator. This script takes an excel file as input and generates a csv dataset as output. The generated dataset can also be directly sent to langsmith.

Usage:
    generate_dataset.py [-v] <input_excel> --range=<s> [--csv-output=<path>] [ --langsmith-dataset-name=<name> ] [ --langfuse-dataset-name=<name> ] [--locale=<locale>] [--no-answer=<na>]
    generate_dataset.py [-v] <input_excel> --sheet=<n>... [--csv-output=<path>] [ --langsmith-dataset-name=<name> ] [ --langfuse-dataset-name=<name> ] [--locale=<locale>] [--no-answer=<na>]

Arguments:
    input_excel path to the input excel file

Options:
    --range=<s>                     Range of sheet to be parsed. The expected format is X,Y where X is the first sheet to be included, and Y is the last. Indices are 0-indexed.
    --sheet=<n>                     Sheet numbers to be parsed. Indices are 0-indexed.
    --csv-output=<path>             Output path of csv file to be generated.
    --langsmith-dataset-name=<name> Name of the dataset to be saved on langsmith.
    --langfuse-dataset-name=<name> Name of the dataset to be saved on langfuse.
    --locale=<locale>               Locale to be included in de dataset. [default: French]
    --no-answer=<na>                Label of no_answer to be included in the dataset. [default: NO_RAG_SENTENCE]
    -h --help                       Show this screen
    --version                       Show version
    -v                              Verbose output for debugging (without this option, script will be silent but for errors)

Generates a testing dataset based on an input file. The input file should have the correct format (see generate_datset_input.xlsx for sample). The generated dataset can be saved on filesystem, using the --csv-output option, on langsmith, using the --langsmith-dataset-name option, on langfuse using the --langfuse-dataset-name option, or both.
"""

import logging
import os
from json import loads
from pathlib import Path
from typing import List

import pandas as pd
from docopt import docopt
from dotenv import load_dotenv
from langfuse import Langfuse
from langsmith import Client


def _generate_dataset(
    filename: str, sheet_indices: List[int], locale: str, no_answer: str
) -> pd.DataFrame:
    dataframes = [_parse_sheet(filename, i) for i in sheet_indices]
    dataset = pd.concat(dataframes)
    dataset = _add_locale(dataset, locale)
    dataset = _add_no_answer(dataset, no_answer)
    return dataset


def _parse_sheet(filename: str, sheet_index: int) -> pd.DataFrame:
    logging.debug('Parsing sheet %s', sheet_index)
    df = pd.read_excel(filename, sheet_name=sheet_index, header=None)

    df = df.drop(0, axis=0)  # Remove header row
    df = df.drop([3, 4, 6, 7], axis=1)  # Remove unnecessary columns
    df = df.rename(columns={0: 'topic', 1: 'question', 2: 'answer', 5: 'quality'})
    df = df.loc[df['quality'].notnull()]  # Ignore not annotated questions
    df = df.loc[df['answer'].notnull()]  # Ignore not answered questions
    df['topic'] = df['topic'].ffill()  # Fill in blanks in topic
    return df


def _add_locale(dataset: pd.DataFrame, locale: str) -> pd.DataFrame:
    logging.debug('Using locale %s', locale)
    return dataset.assign(locale=locale)


def _add_no_answer(dataset: pd.DataFrame, no_answer: str) -> pd.DataFrame:
    logging.debug('Using no_answer label %s', no_answer)
    return dataset.assign(no_answer=no_answer)


def _parse_range(input_range: str) -> List[int]:
    [a, b] = input_range.split(',')
    return [i for i in range(int(a), int(b) + 1)]


def _save_on_fs(dataset: pd.DataFrame, path: str):
    logging.info('Saving dataset on path %s', path)
    dataset.to_csv(path, index=False)


def _send_to_langsmith(dataset: pd.DataFrame, dataset_name: str):
    # Transforms dataset to langsmith format
    records = dataset.to_json(orient='records')
    records = loads(str(records))

    # Creates dataset in langsmith
    client = Client()
    logging.info('Creating dataset %s on langsmith...', dataset_name)
    ls_dataset = client.create_dataset(dataset_name=dataset_name)
    logging.info('Creating examples on langsmith dataset id %s...', ls_dataset.id)
    client.create_examples(
        inputs=[
            {
                'question': r['question'],
                'locale': r['locale'],
                'no_answer': r['no_answer'],
                'metadata': {
                    'topic': r['topic'],
                },
            }
            for r in records
        ],
        outputs=[
            {
                'answer': r['answer'],
                'quality': r['quality'],
            }
            for r in records
        ],
        dataset_id=ls_dataset.id,
    )


def _send_to_langfuse(dataset: pd.DataFrame, dataset_name: str):
    # Transforms dataset to JSON format
    records = dataset.to_json(orient='records')
    records = loads(str(records))

    # Initializes the Langfuse client
    client = Langfuse()

    logging.info('Creating dataset %s on Langfuse...', dataset_name)

    # Creates dataset in Langfuse
    lf_dataset = client.create_dataset(name=dataset_name)

    logging.info('Creating examples on Langfuse dataset id %s...', lf_dataset.id)

    # Prepares inputs and outputs
    inputs = [
        {
            'question': r['question'],
            'locale': r['locale'],
            'no_answer': r['no_answer'],
        }
        for r in records
    ]
    metadatas = [{'topic': r['topic']} for r in records]

    outputs = [
        {
            'answer': r['answer'],
            'quality': r['quality'],
        }
        for r in records
    ]

    # Creates examples in the dataset on Langfuse
    for input, metadata, output in zip(inputs, metadatas, outputs):
        logging.info('import data')
        client.create_dataset_item(
            dataset_name=dataset_name,
            input=input,
            expected_output=output,
            metadata=metadata,
        )


if __name__ == '__main__':
    cli_args = docopt(__doc__, version='Dataset generator 0.1.0')
    load_dotenv()
    # Set logging level
    log_format = '%(levelname)s:%(module)s:%(message)s'
    logging.basicConfig(
        level=logging.DEBUG if cli_args['-v'] else logging.INFO, format=log_format
    )

    # check if input filer exists
    filename = cli_args['<input_excel>']
    if not os.path.isfile(filename):
        logging.error('Specified input excel file was not found.')
        exit(1)

    # check if langsmith creds is set
    langsmith_dataset_name = cli_args['--langsmith-dataset-name']
    if langsmith_dataset_name and not os.environ.get('LANGCHAIN_API_KEY'):
        logging.error('Envvar LANGCHAIN_API_KEY not found.')
        exit(1)

    # check if langfuse creds is set
    langfuse_dataset_name = cli_args['--langfuse-dataset-name']
    if langfuse_dataset_name and not os.environ.get('LANGFUSE_PUBLIC_KEY'):
        logging.error('Envvar LANGFUSE_PUBLIC_KEY not found.')
        exit(1)

    # check if output file can be written
    output_path = cli_args['--csv-output']
    if output_path and not Path(output_path).parent.exists():
        logging.error(
            'Cannot proceed: directory %s does not exist', Path(output_path).parent
        )
        exit(1)

    if cli_args.get('--range') is not None:
        sheet_indices = _parse_range(str(cli_args['--range']))
    else:
        sheet_indices = [int(i) for i in cli_args['--sheet']]

    dataset = _generate_dataset(
        filename=filename,
        sheet_indices=sheet_indices,
        locale=cli_args['--locale'] or 'French',
        no_answer=cli_args['--no-answer'] or 'NO_RAG_SENTENCE',
    )

    if output_path:
        _save_on_fs(dataset, output_path)

    if langsmith_dataset_name:
        _send_to_langsmith(dataset, cli_args['--langsmith-dataset-name'])

    if langfuse_dataset_name:
        _send_to_langfuse(dataset, cli_args['--langfuse-dataset-name'])
