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
"""Retrieval-Augmented Generation (RAG) endpoint settings testing tool based
on LangSmith's SDK: runs a specific RAG Settings configuration against a
reference dataset.

Usage:
    rag_testing_tool.py [-v] <rag_query> <dataset_name> <test_name> [<delay>]
    rag_testing_tool.py -h | --help
    rag_testing_tool.py --version

Arguments:
    rag_query       path to a JSON 'RAGQuery' JSON file containing RAG settings
                    to be tested: llm model, embedding model, vector database
                    provider, indexation session's unique id, and 'k', i.e. nb
                    of retrieved docs (question and chat history are ignored,
                    as they will come from the dataset)
    dataset_name    the reference dataset name
    test_name       name of the test run
    delay           delay between two calls to the inference method in ms

Options:
    -h --help   Show this screen
    --version   Show version
    -v          Verbose output for debugging (without this option, script will
                be silent but for errors)

<full desc>
"""
import json
import logging
import os
import sys
import time
from datetime import datetime
from pathlib import Path
from uuid import uuid4

from docopt import docopt
from dotenv import load_dotenv
from langsmith import Client

from gen_ai_orchestrator.routers.requests.requests import RagQuery
from gen_ai_orchestrator.services.langchain.rag_chain import create_rag_chain


def test_rag(args):
    """
    Test RAG endpoint settings against a reference dataset.

    Args:

        args (dict):    A dictionary containing command-line arguments.
                        Expecting keys: '<rag_query>'
                                        '<dataset_name>'
                                        '<test_name>'
                                        '<delay>'
    """
    start_time = datetime.now()

    with open(args['<rag_query>'], 'r') as file:
        rag_query = json.load(file)

    # Modify this if you are testing against a dataset that follows another
    # format
    def _construct_chain():
        if args['<delay>']:
            time.sleep(int(args['<delay>']) / 1000)

        return {
            'question': lambda x: x['question'],
            'locale': lambda x: x['locale'],
            'no_answer': lambda x: x['no_answer'],
            'chat_history': lambda x: x['chat_history'] if 'chat_history' in x else [],
        } | create_rag_chain(RagQuery(**rag_query))

    client = Client()
    search_params = rag_query['document_search_params']
    index_session_id = search_params['filter'][0]['term'][
        'metadata.index_session_id.keyword'
    ]
    k = search_params['k']

    # This is LangSmith's default concurrency level
    concurrency_level = 5
    # If delay arg is present, we want to space out executions, running them
    # one at a time
    if args['<delay>']:
        concurrency_level = 1

    client.run_on_dataset(
        dataset_name=args['<dataset_name>'],
        llm_or_chain_factory=_construct_chain,
        project_name=args['<test_name>'] + '-' + str(uuid4())[:8],
        project_metadata={
            'index_session_id': index_session_id,
            'k': k,
        },
        concurrency_level=concurrency_level,
    )

    duration = datetime.now() - start_time
    hours, remainder = divmod(duration.seconds, 3600)
    minutes, seconds = divmod(remainder, 60)
    formatted_duration = '{:02}:{:02}:{:02}'.format(hours, minutes, seconds)
    logging.debug(
        f'Ran RAGQuery (k={k}, index_session_id={index_session_id}) on '
        f"{args['<dataset_name>']} dataset (duration: {formatted_duration})"
    )


if __name__ == '__main__':
    cli_args = docopt(__doc__, version='RAG Testing Tool 0.1.0')

    # Set logging level
    log_format = '%(levelname)s:%(module)s:%(message)s'
    logging.basicConfig(
        level=logging.DEBUG if cli_args['-v'] else logging.WARNING, format=log_format
    )

    load_dotenv()

    # Check env (LangSmith)
    langchain_endpoint = os.getenv('LANGCHAIN_ENDPOINT')
    if not langchain_endpoint:
        logging.error(
            'Cannot proceed: LANGCHAIN_ENDPOINT env variable is not defined (define it in a .env file)'
        )
        sys.exit(1)

    langchain_apikey = os.getenv('LANGCHAIN_API_KEY')
    if not langchain_apikey:
        logging.error(
            'Cannot proceed: LANGCHAIN_API_KEY env variable is not defined (define it in a .env file)'
        )
        sys.exit(1)

    # Check args:
    # - RAGQuery JSON file
    rag_query_file_path = Path(cli_args['<rag_query>'])
    if not rag_query_file_path.exists():
        logging.error(
            f"Cannot proceed: RAGQuery JSON file '{cli_args['<rag_query>']}' does not exist"
        )
        sys.exit(1)
    try:
        with open(rag_query_file_path, 'r') as file:
            rag_query = json.load(file)
    except json.JSONDecodeError:
        logging.error(
            f"Cannot proceed: RAGQuery JSON file '{cli_args['<rag_query>']}' is not a valid JSON file"
        )
        sys.exit(1)

    # - dataset name is always valid
    # - test name is always valid

    # - delay
    delay_ms = cli_args['<delay>']
    if delay_ms:
        try:
            int(delay_ms)
        except ValueError:
            logging.error(
                f"Cannot proceed: delay arg '{cli_args['<delay>']}' is not a valid integer"
            )
            sys.exit(1)

    # Main func
    test_rag(cli_args)
