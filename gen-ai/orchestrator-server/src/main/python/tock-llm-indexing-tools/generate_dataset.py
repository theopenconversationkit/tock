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
Dataset Generator: Generate CSV or Langfuse datasets from an Excel file.

Usage:
    generate_dataset.py [-v] --input-excel=<ie> [--csv-output=<co>] [--langsmith-dataset-name=<lsdn>] [--langfuse-dataset-name=<lfdn>] [--locale=<l>] [--no-answer=<na>]

Description:
    This script processes an input Excel file to generate a testing dataset. The output can be saved as a CSV file,
    uploaded to Langsmith, or uploaded to Langfuse. The input Excel file must follow the specified format
    (see examples/generate_dataset_input.example.xlsx).

Arguments:
    --input-excel=<ie>              Path to the input Excel file. This is a required argument.

Options:
    --csv-output=<co>               Path to save the generated dataset as a CSV file. Optional.
    --langsmith-dataset-name=<lsdn> Name of the dataset to be uploaded to Langsmith. Optional.
    --langfuse-dataset-name=<lfdn>  Name of the dataset to be uploaded to Langfuse. Optional.
    --locale=<l>                    Locale information to include in the dataset. Defaults to "French". Optional.
    --no-answer=<na>                Label of no-answer to include in the dataset. Defaults to "NO_RAG_SENTENCE". Optional.
    -v                              Enable verbose output for debugging purposes. If not set, the script runs silently except for errors.
    -h, --help                      Display this help message and exit.
    --version                       Display the version of the script.

Examples:
    1. Generate a CSV dataset:
        python generate_dataset.py --input-excel=path/to/input.xlsx --csv-output=path/to/output.csv

    2. Generate and upload a dataset to Langfuse:
        python generate_dataset.py --input-excel=path/to/input.xlsx --langfuse-dataset-name=my_dataset

    3. Generate a CSV dataset with a specified locale and verbose mode:
        python generate_dataset.py --input-excel=path/to/input.xlsx --csv-output=path/to/output.csv --locale=English -v

Notes:
    - The input Excel file must adhere to the required format. Check examples/generate_dataset_input.example.xlsx for reference.
    - You can simultaneously save the dataset locally (as a CSV) and upload it to Langsmith or Langfuse by providing the respective options.
"""

import base64
import logging
import os
from pathlib import Path

import boto3
import httpx
import pandas as pd
from docopt import docopt
from dotenv import load_dotenv
from gen_ai_orchestrator.configurations.environment.settings import application_settings
from gen_ai_orchestrator.models.security.proxy_server_type import ProxyServerType
from httpx_auth_awssigv4 import SigV4Auth
from langfuse import Langfuse
from langsmith import Client

from models import DatasetItem


def _extract_dataset_items(filename: str, locale: str, no_answer: str) -> list[DatasetItem]:
    dataset_items = []
    input_df = pd.read_excel(filename, sheet_name='Template_Suivi_Recette', header=None)
    # Replace NaN with None in the DataFrame
    df = input_df.map(lambda x: None if pd.isna(x) else x)
    # Data extraction
    # Using panda lines and columns are numbered from 0, row 8 match with raw "I" in Excel or LibreOffice
    topics = df.iloc[2, 8:].tolist()     # Line 2 (line 3 in Excel or LibreOffice): Topic
    questions = df.iloc[3, 8:].tolist()  # Line 3 (line 4 in Excel or LibreOffice): Question
    answers = df.iloc[4, 8:].tolist()    # Line 4 (line 5 in Excel or LibreOffice): Expected answer
    # DatasetItems
    for topic, question, answer in zip(topics, questions, answers):
        if question is not None:
            dataset_items.append(DatasetItem(
                topic=topic,
                question=question,
                locale=locale,
                answer=answer,
                no_answer=no_answer
            ))

    return dataset_items

def _save_on_fs(dataset_items: list[DatasetItem], output_path: str):
    logging.info('Saving dataset on path %s', output_path)
    dataset = pd.DataFrame([item.model_dump() for item in dataset_items])
    dataset.to_csv(output_path, index=False)

def _send_to_langsmith(dataset_items: list[DatasetItem], dataset_name: str):
    # Creates dataset in langsmith
    client = Client()
    logging.info('Creating dataset %s on langsmith...', dataset_name)
    ls_dataset = client.create_dataset(dataset_name=dataset_name)
    logging.info('Creating examples on langsmith dataset id %s...', ls_dataset.id)
    client.create_examples(
        inputs=[
            {
                'question': item.question,
                'locale': item.locale,
                'no_answer': item.no_answer,
                'metadata': {
                    'topic': item.topic,
                },
            }
            for item in dataset_items
        ],
        outputs=[
            {
                'answer': item.answer
            }
            for item in dataset_items
        ],
        dataset_id=ls_dataset.id,
    )

def init_langfuse():
    if ProxyServerType.AWS_LAMBDA == application_settings.observability_proxy_server:
        """
        This AWSLambda proxy is used when the architecture implemented for the Langfuse
        observability tool places it behind an API Gateway which requires its
        own authentication, itself invoked by an AWS Lambda.
        The API Gateway uses the standard "Authorization" header,
        and uses observability_proxy_server_authorization_header_name
        to define the "Authorization bearer token" for Langfuse.
        """
        aws_session = boto3.Session()
        aws_credentials = aws_session.get_credentials()
        auth = SigV4Auth(
            access_key=aws_credentials.access_key,
            secret_key=aws_credentials.secret_key,
            token=aws_credentials.token,
            service="lambda",
            region=aws_session.region_name,
        )

        langfuse_creds = base64.b64encode(
            f"{os.environ['LANGFUSE_PUBLIC_KEY']}:{os.environ['LANGFUSE_SECRET_KEY']}".encode()
        ).decode()

        return Langfuse(httpx_client=httpx.Client(
            auth=auth,
            headers={
                application_settings.observability_proxy_server_authorization_header_name: f"Basic {langfuse_creds}"
            },
        ))

    return Langfuse()


def _send_to_langfuse(dataset_items: list[DatasetItem], dataset_name: str):
    # Initializes the Langfuse client
    client = init_langfuse()
    logging.info('Creating dataset %s on Langfuse...', dataset_name)
    lf_dataset = client.create_dataset(name=dataset_name)
    logging.info('Creating examples on Langfuse dataset id %s...', lf_dataset.id)
    for item in dataset_items:
        item_input = {
            'question': item.question,
            'locale': item.locale,
            'no_answer': item.no_answer,
        }
        item_metadata = {
            'topic': item.topic
        }
        item_output = {
            'answer': item.answer
        }

        # Creates examples in the dataset on Langfuse
        client.create_dataset_item(
            dataset_name=dataset_name,
            input=item_input,
            expected_output=item_output,
            metadata=item_metadata,
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
    input_excel_filename = cli_args['--input-excel']
    if not os.path.isfile(input_excel_filename):
        logging.error(f'Specified input excel file was not found ({input_excel_filename}).')
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

    dataset = _extract_dataset_items(
        filename=input_excel_filename,
        locale=cli_args['--locale'] or 'French',
        no_answer=cli_args['--no-answer'] or 'NO_RAG_SENTENCE',
    )

    if output_path:
        _save_on_fs(dataset, output_path)

    if langsmith_dataset_name:
        _send_to_langsmith(dataset, cli_args['--langsmith-dataset-name'])

    if langfuse_dataset_name:
        _send_to_langfuse(dataset, cli_args['--langfuse-dataset-name'])
