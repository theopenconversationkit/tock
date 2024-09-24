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
"""Index a ready-to-index CSV file ('title'|'url'|'text' lines) file contents
into an OpenSearch vector database.

Usage:
    index_documents.py [-v] <input_directory> <index_name> <embeddings_cfg> <chunks_size> [<env_file>]
    index_documents.py -h | --help
    index_documents.py --version

Arguments:
    input_directory      path to the directory containing  ready-to-index files.
    index_name      name of the OpenSearch index (shall follow indexes
                    naming rules)
    embeddings_cfg  path to an embeddings configuration file (JSON format)
                    (shall describe settings for one of OpenAI or AzureOpenAI
                    embeddings model)
    chunks_size     size of the embedded chunks of documents

Options:
    -h --help   Show this screen
    --version   Show version
    -v          Verbose output for debugging (without this option, script will
                be silent but for errors and the unique indexing session id)

Index a ready-to-index CSV file contents into an OpenSearch vector database.
CSV columns are 'title'|'url'|'text'. 'text' will be chunked according to
chunks_size, and embedded using configuration described in embeddings_cfg (it
uses the embeddings constructor from the orchestrator module, so JSON file
shall follow corresponding format). Documents will be indexed in OpenSearch DB
under index_name index (index_name shall follow OpenSearch naming restrictions).
A unique indexing session id is produced and printed to the console (will be
the last line printed if the '-v' option is used).
"""
import csv
import json
import logging
import os
import sys
from datetime import datetime
from pathlib import Path
from typing import Iterable
from uuid import uuid4

import pandas as pd
from docopt import docopt
from gen_ai_orchestrator.models.em.azureopenai.azure_openai_em_setting import (
    AzureOpenAIEMSetting,
)
from gen_ai_orchestrator.models.em.em_provider import EMProvider
from gen_ai_orchestrator.models.em.em_setting import BaseEMSetting
from gen_ai_orchestrator.models.em.openai.openai_em_setting import (
    OpenAIEMSetting,
)
from gen_ai_orchestrator.models.vector_stores.vectore_store_provider import (
    VectorStoreProvider,
)
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_em_factory,
    get_vector_store_factory,
)
from langchain.embeddings.base import Embeddings
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_community.document_loaders.dataframe import DataFrameLoader
from langchain_core.documents import Document

# Define the size of the csv field -> Set to maximum to process large csv
csv.field_size_limit(sys.maxsize)


def index_documents(args):
    """
    Read a ready-to-index CSV file, then index its contents to an OpenSearch DB.

    Args:

        args (dict):    A dictionary containing command-line arguments.
                        Expecting keys: '<input_directory>'
                                        '<index_name>'
                                        '<embeddings_cfg>'
                                        '<chunks_size>'

    Returns:
        The indexing session unique id.
    """
    # unique date / uuid for each indexing session (stored as metadata)
    start_time = datetime.now()
    formatted_datetime = start_time.strftime('%Y-%m-%d %H:%M:%S')
    session_uuid = uuid4()
    logging.debug(
        f"Beginning indexation session {session_uuid} at '{formatted_datetime}'"
    )
    input_directory = args['<input_directory>']
    list_input_csv = []
    for filename in os.listdir(input_directory):
        if filename.endswith('.csv'):
            logging.debug(f'Read input CSV file {filename}')
            filepath = os.path.join(input_directory, filename)
            logging.debug('Processing file: %s', filepath)
            list_input_csv.append(
                pd.read_csv(
                    input_directory + '/' + filename,
                    delimiter='|',
                    quotechar='"',
                    names=['title', 'source', 'text'],
                )
            )
    df = pd.concat(list_input_csv)
    loader = DataFrameLoader(df, page_content_column='text')
    docs = loader.load()

    for doc in docs:
        doc.metadata['index_session_id'] = session_uuid
        doc.metadata['index_datetime'] = formatted_datetime
        doc.metadata['id'] = uuid4()  # A uuid for the doc (will be used by TOCK)

    logging.debug(f"Split texts in {args['<chunks_size>']} characters-sized chunks")
    # recursive splitter is used to preserve sentences & paragraphs
    text_splitter = RecursiveCharacterTextSplitter(
        chunk_size=int(args['<chunks_size>'])
    )
    splitted_docs = text_splitter.split_documents(docs)
    # Add chunk id ('n/N') metadata to each chunk
    splitted_docs = generate_ids_for_each_chunks(splitted_docs=splitted_docs)
    # Add title to text (for better semantic search)
    splitted_docs = add_title_to_text(splitted_docs=splitted_docs)

    logging.debug(f"Get embeddings model from {args['<embeddings_cfg>']} config file")
    with open(args['<embeddings_cfg>'], 'r') as file:
        config_dict = json.load(file)
    em_settings = em_settings_from_config(config_dict)

    # Use embeddings factory from orchestrator
    em_factory = get_em_factory(em_settings)
    em_factory.check_embedding_model_setting()
    embeddings = em_factory.get_embedding_model()

    # Index all chunks in vector DB
    embed_and_store_docs(
        splitted_docs, embeddings=embeddings, index_name=args['<index_name>']
    )

    # Print statistics
    duration = datetime.now() - start_time
    logging.debug(
        f"Indexed {len(splitted_docs)} chunks in '{args['<index_name>']}' from {len(docs)} lines in '{args['<input_directory>']}' (duration: {duration})"
    )

    # Return session index uuid to main script
    return session_uuid


def generate_ids_for_each_chunks(
    splitted_docs: Iterable[Document],
) -> Iterable[Document]:
    """Add chunk id ('n/N') to the documents' metadata using Pandas."""
    metadata = [doc.metadata for doc in splitted_docs]
    df_metadata = pd.DataFrame(metadata)
    df_metadata['total_chunks'] = df_metadata.groupby('id')['id'].transform('count')
    df_metadata['chunk_id'] = df_metadata.groupby('id').cumcount() + 1
    df_metadata['chunk'] = (
        df_metadata['chunk_id'].astype(str)
        + '/'
        + df_metadata['total_chunks'].astype(str)
    )
    for i, doc in enumerate(splitted_docs):
        doc.metadata['chunk'] = df_metadata.loc[i, 'chunk']
    return splitted_docs


def add_title_to_text(
    splitted_docs: Iterable[Document],
) -> Iterable[Document]:
    """
    Add 'title' from metadata to Document's page_content for better semantic search.

    The concatenation model used when indexing data is {title}\n\n{content_page}.
    The aim is to add the ‘title’ prefix from the document content when sending to embedding.
    """
    for doc in splitted_docs:
        # Add title to page_content
        if 'title' in doc.metadata:
            title = doc.metadata['title']
            doc.page_content = f'{title}\n\n{doc.page_content}'
    return splitted_docs


def em_settings_from_config(setting_dict: dict) -> BaseEMSetting:
    """Get embeddings settings from config dict."""
    # Create settings class according to embeddings provider from config file
    if config_dict['provider'] == EMProvider.OPEN_AI:
        em_settings = OpenAIEMSetting(**setting_dict)
    elif config_dict['provider'] == EMProvider.AZURE_OPEN_AI_SERVICE:
        em_settings = AzureOpenAIEMSetting(**setting_dict)

    return em_settings


def embed_and_store_docs(
    documents: Iterable[Document], embeddings: Embeddings, index_name: str
) -> None:
    """ "Embed all chunks in vector database."""
    logging.debug('Index chunks in DB')
    # Use vector store factory from orchestrator
    vectorstore_factory = get_vector_store_factory(
        VectorStoreProvider.OPEN_SEARCH,
        embedding_function=embeddings,
        index_name=index_name,
    )
    opensearch_db = vectorstore_factory.get_vector_store()
    # Index respecting bulk_size (500 is from_documents current default: it is described for clarity only)
    bulk_size = 500
    for i in range(0, len(documents), bulk_size):
        logging.debug(f'i={i}, splitted_docs={len(documents)}')
        opensearch_db.add_documents(
            documents=documents[i : i + bulk_size], bulk_size=bulk_size
        )


def index_name_is_valid(index_name: str) -> bool:
    """
    Check if index_name is a valid OpenSearch index name.
    (https://opensearch.org/docs/latest/api-reference/index-apis/create-index)
    """
    if not index_name.islower():
        logging.error('Index name must be all lowercase')
        return False

    if index_name.startswith('_') or index_name.startswith('-'):
        logging.error('Index names can’t begin with underscores (_) or hyphens (-)')
        return False

    # List of invalid characters
    invalid_chars = [':', '"', '*', '+', '/', '\\', '|', '?', '#', '>', '<', ',', ' ']
    for char in invalid_chars:
        if char in index_name:
            logging.error(f'Index name contains invalid character: {char}')
            return False

    return True


if __name__ == '__main__':
    cli_args = docopt(__doc__, version='index document 0.1.0')

    # Set logging level
    log_format = '%(levelname)s:%(module)s:%(message)s'
    logging.basicConfig(
        level=logging.DEBUG if cli_args['-v'] else logging.WARNING, format=log_format
    )

    # Check args:
    # - input file path
    inputfile_path = Path(cli_args['<input_directory>'])
    if not inputfile_path.exists():
        logging.error(
            f"Cannot proceed: input CSV file '{cli_args['<input_directory>']}' does not exist"
        )
        sys.exit(1)

    # - index name
    # could be checked via factory in a future version
    if not index_name_is_valid(cli_args['<index_name>']):
        logging.error(
            f"Cannot proceed: index name {cli_args['<index_name>']} is not a valid OpenSearch index name"
        )
        sys.exit(1)

    # - embeddings config JSON file
    cfg_file_path = Path(cli_args['<embeddings_cfg>'])
    if not cfg_file_path.exists():
        logging.error(
            f"Cannot proceed: embeddings config file '{cli_args['<embeddings_cfg>']}' does not exist"
        )
        sys.exit(1)
    try:
        with open(cfg_file_path, 'r') as file:
            config_dict = json.load(file)
    except json.JSONDecodeError:
        logging.error(
            f"Cannot proceed: embeddings config file '{cli_args['<embeddings_cfg>']}' is not a valid JSON file"
        )
        sys.exit(1)
    if not EMProvider.has_value(config_dict['provider']):
        logging.error(
            f"Cannot proceed: embeddings config file references an unknown embedding model : '{config_dict['provider']}'"
        )
        sys.exit(1)

    # - chunks size
    try:
        int(cli_args['<chunks_size>'])
    except ValueError:
        logging.error(
            f"Cannot proceed: chunks size ({cli_args['<chunks_size>']}) is not a number"
        )
        sys.exit(1)

    # Main func
    indexing_session_uuid = index_documents(cli_args)

    # Print indexation session's unique id
    print(indexing_session_uuid)
