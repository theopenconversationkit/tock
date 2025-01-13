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
on LangSmith's or LangFuse's SDK: runs a specific RAG Settings configuration against a
reference dataset.

Usage:
    rag_testing_tool.py [-v] <rag_query> <dataset_provider> <dataset_name> <test_name>
    rag_testing_tool.py -h | --help
    rag_testing_tool.py --version

Arguments:
    rag_query       path to a JSON 'RAGQuery' JSON file containing RAG settings
                    to be tested: llm model, embedding model, vector database
                    provider, indexation session's unique id, and 'k', i.e. nb
                    of retrieved docs (question and chat history are ignored,
                    as they will come from the dataset)
    dataset_provider the dataset provider (langsmith or langfuse)
    dataset_name    the reference dataset name
    test_name       name of the test run

Options:
    -h --help   Show this screen
    --version   Show version
    -v          Verbose output for debugging (without this option, script will
                be silent but for errors)

Build a RAG (Lang)chain from the RAG Query and runs it against the provided
LangSmith or LangFuse dataset. The chain is created anew for each entry of the dataset.
"""
import json
import logging
import os
import sys
from datetime import datetime
from pathlib import Path
from uuid import uuid4

from docopt import docopt
from dotenv import load_dotenv
from gen_ai_orchestrator.routers.requests.requests import RagQuery
from gen_ai_orchestrator.services.langchain.rag_chain import create_rag_chain
from langsmith import Client

from generate_dataset import init_langfuse


def test_rag(args):
    """
    Test RAG endpoint settings against a reference dataset.

    Args:

        args (dict):    A dictionary containing command-line arguments.
                        Expecting keys: '<rag_query>'
                                        '<dataset_provider>'
                                        '<dataset_name>'
                                        '<test_name>'
    """
    start_time = datetime.now()

    with open(args['<rag_query>'], 'r') as file:
        rag_query = json.load(file)

    def _construct_chain():
        # Modify this if you are testing against a dataset that follows another
        # format
        return {
            'question': lambda x: x['question'],
            'locale': lambda x: x['locale'],
            'no_answer': lambda x: x['no_answer'],
            'chat_history': lambda x: x['chat_history'] if 'chat_history' in x else [],
        } | create_rag_chain(RagQuery(**rag_query), vector_db_async_mode=False)

    def run_dataset(run_name_dataset):

        if args['<dataset_provider>'].lower() == 'langsmith':
            client = Client()
            client.run_on_dataset(

                dataset_name=args['<dataset_name>'],
                llm_or_chain_factory=_construct_chain,
                project_name=run_name_dataset,
                project_metadata={
                    'document_index_name': document_index_name,
                    'k': k,
                },
                concurrency_level=concurrency_level,
            )
        elif args['<dataset_provider>'].lower() == 'langfuse':
            client = init_langfuse()
            dataset = client.get_dataset(args['<dataset_name>'])

            for item in dataset.items:
                callback_handlers = []
                handler = item.get_langchain_handler(
                    run_name=run_name_dataset,
                    run_metadata={
                        'document_index_name': document_index_name,
                        'k': k,
                    },
                )
                callback_handlers.append(handler)
                _construct_chain().invoke(
                    item.input, config={'callbacks': callback_handlers}
                )
            client.flush()

    document_index_name = rag_query['document_index_name']
    search_params = rag_query['document_search_params']
    k = search_params['k']

    # This is LangSmith's default concurrency level
    concurrency_level = 5
    run_name_dataset = args['<test_name>'] + '-' + str(uuid4())[:8]
    run_dataset(run_name_dataset)

    duration = datetime.now() - start_time
    hours, remainder = divmod(duration.seconds, 3600)
    minutes, seconds = divmod(remainder, 60)
    formatted_duration = '{:02}:{:02}:{:02}'.format(hours, minutes, seconds)
    logging.debug(
        f'Ran RAGQuery (k={k}, document_index_name={document_index_name}) on '
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
    if cli_args['<dataset_provider>'].lower() == 'langsmith':
        # Check env (LangSmith)
        langchain_apikey = os.getenv('LANGCHAIN_API_KEY')
        if not langchain_apikey:
            logging.error(
                'Cannot proceed: LANGCHAIN_API_KEY env variable is not defined (define it in a .env file)'
            )
            sys.exit(1)
    elif cli_args['<dataset_provider>'].lower() == 'langfuse':
        langfuse_secret_key = os.getenv('LANGFUSE_SECRET_KEY')
        if not langfuse_secret_key:
            logging.error(
                'Cannot proceed: LANGFUSE_SECRET_KEY env variable is not defined (define it in a .env file)'
            )
            sys.exit(1)
        langchain_host = os.getenv('LANGFUSE_HOST')
        if not langchain_host:
            logging.error(
                'Cannot proceed: LANGFUSE_HOST env variable is not defined (define it in a .env file)'
            )
            sys.exit(1)
        langfuse_public_key = os.getenv('LANGFUSE_PUBLIC_KEY')
        if not langfuse_public_key:
            logging.error(
                'Cannot proceed: LANGFUSE_PUBLIC_KEY env variable is not defined (define it in a .env file)'
            )
            sys.exit(1)
    else:
        logging.error(
            'Cannot proceed: dataset_provider is not valid, only langfuse or langsmith'
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

    # Main func
    test_rag(cli_args)
