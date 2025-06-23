#   Copyright (C) 2025 Credit Mutuel Arkea
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
Create a new dataset into Langfuse

Usage:
    create_dataset.py [-v] --json-config-file=<jcf>

Description:
    This script is used to create a new dataset into Langfuse based on a json configuration file.
    The configuration file specifies the template to be used to download the elements of the dataset:
        - For "xlsx", see examples/dataset-items-example.xlsx
        - For "json", see examples/dataset-items-example.json

Arguments:
    --json-config-file=<jcf>   Path to the input config file. This is a required argument.

Options:
    -v                         Enable verbose output for debugging purposes. If not set, the script runs silently except for errors.
    -h, --help                 Display this help message and exit.
    --version                  Display the version of the script.

Examples:
    python create_dataset.py --json-config-file=path/to/config-file.json
"""
from datetime import datetime

import pandas as pd
from docopt import docopt
from langfuse import Langfuse
from langfuse.api import DatasetItem, NotFoundError
from scripts.common.logging_config import configure_logging
from scripts.common.models import ActivityStatus, StatusWithReason
from scripts.dataset.creation.models import (
    CreateDatasetInput,
    CreateDatasetOutput,
    DatasetItemInfo,
)

from gen_ai_orchestrator.services.security.security_service import (
    fetch_secret_key_value,
)


def extract_dataset_items(template_file_path: str) -> list[DatasetItemInfo]:
    dataset_items = []
    input_df = pd.read_excel(
        template_file_path, sheet_name='Template_Suivi_Recette', header=None
    )
    # Replace NaN with None in the DataFrame
    df = input_df.map(lambda x: None if pd.isna(x) else x)
    # Data extraction
    # Using panda lines and columns are numbered from 0, row 8 match with raw "I" in Excel or LibreOffice
    topics = df.iloc[2, 8:].tolist()  # Line 2 (line 3 in Excel or LibreOffice): Topic
    questions = df.iloc[
        3, 8:
    ].tolist()  # Line 3 (line 4 in Excel or LibreOffice): Question
    answers = df.iloc[
        4, 8:
    ].tolist()  # Line 4 (line 5 in Excel or LibreOffice): Expected answer
    # DatasetItems
    for topic, question, answer in zip(topics, questions, answers):
        if pd.notna(question):
            dataset_items.append(
                DatasetItemInfo(
                    topic=topic, question=question, answer=answer if answer else ''
                )
            )

    return dataset_items


def send_to_langfuse(
    logger, input_config: CreateDatasetInput, dataset_items: list[DatasetItemInfo]
) -> list[DatasetItem]:
    # Initializes the Langfuse client
    client = Langfuse(
        host=str(input_config.observability_setting.url),
        public_key=input_config.observability_setting.public_key,
        secret_key=fetch_secret_key_value(
            input_config.observability_setting.secret_key
        ),
    )

    try:
        dataset = client.get_dataset(name=input_config.dataset.name)
        raise RuntimeError(f"The '{dataset.name}' dataset already exists!")
    except NotFoundError:
        logger.info('Creating dataset %s on Langfuse...', input_config.dataset.name)

    lf_dataset = client.create_dataset(
        name=input_config.dataset.name,
        description=input_config.dataset.description,
        metadata=input_config.dataset.metadata,
    )
    logger.info('Creating examples on Langfuse dataset id %s...', lf_dataset.id)
    items: list[DatasetItem] = []
    for item in dataset_items:
        item_input = {'question': item.question}
        item_metadata = {'topic': item.topic}
        item_output = {'answer': item.answer}

        # Creates examples in the dataset on Langfuse
        items.append(
            client.create_dataset_item(
                dataset_name=input_config.dataset.name,
                input=item_input,
                expected_output=item_output,
                metadata=item_metadata,
            )
        )

    return items


def main():
    start_time = datetime.now()
    cli_args = docopt(__doc__, version='Run Chunk Contextualization 1.0.0')
    logger = configure_logging(cli_args)

    dataset_name: str = 'UNKNOWN'
    dataset_items: list[DatasetItemInfo] = []
    created_items: list[DatasetItem] = []
    try:
        logger.info('Loading input data...')
        input_config = CreateDatasetInput.from_json_file(cli_args['--json-config-file'])
        logger.debug(f'\n{input_config.format()}')

        dataset_name = input_config.dataset.name
        location = f'{input_config.bot.file_location}/{input_config.bot.namespace}-{input_config.bot.bot_id}'
        template_file_path = f'{location}/input/{input_config.dataset.template.file}'

        if 'xlsx' == input_config.dataset.template.type:
            dataset_items = extract_dataset_items(template_file_path=template_file_path)
        else:
            raise RuntimeError(
                f"The '{input_config.dataset.template.type}' dataset template is not yet supported!"
            )

        created_items = send_to_langfuse(logger, input_config, dataset_items)

        activity_status = StatusWithReason(status=ActivityStatus.COMPLETED)
    except Exception as e:
        full_exception_name = f'{type(e).__module__}.{type(e).__name__}'
        activity_status = StatusWithReason(
            status=ActivityStatus.FAILED, status_reason=f'{full_exception_name} : {e}'
        )
        logger.error(e, exc_info=True)

    len_dataset_items = len(dataset_items)
    output = CreateDatasetOutput(
        status=activity_status,
        dataset_name=dataset_name,
        duration=datetime.now() - start_time,
        items_count=len_dataset_items,
        success_rate=100 * (len(created_items) / len_dataset_items)
        if len_dataset_items > 0
        else 0,
    )
    logger.debug(f'\n{output.format()}')


if __name__ == '__main__':
    main()
