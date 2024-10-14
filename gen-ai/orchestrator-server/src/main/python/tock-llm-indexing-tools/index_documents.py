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
"""Index a ready-to-index CSV ('title'|'source'|'text' lines) file contents into a given vector database.
Usage:
    index_documents.py [-v] <input_csv> <namespace> <bot_id> <embeddings_json_config> <vector_store_json_config> <chunks_size> <ignore_source> [<env_file>]
    index_documents.py -h | --help
    index_documents.py --version

Arguments:
    input_csv       path to the ready-to-index file
    namespace       the namespace
    bot_id          the bot ID
    embeddings_json_config  path to an embeddings configuration file (JSON format)
                    (shall describe settings for one of OpenAI or AzureOpenAI
                    embeddings model)
    vector_store_json_config  path to a vector store configuration file (JSON format)
                    (shall describe settings for one of OpenSearch or PGVector store)
    chunks_size     size of the embedded chunks of documents
    ignore_source   To ignore source
TODO MASS
Options:
    -h --help   Show this screen
    --version   Show version
    -v          Verbose output for debugging

Index a ready-to-index CSV file contents into an OpenSearch vector database.
CSV columns are 'title'|'source'|'text'. 'text' will be chunked according to
chunks_size, and embedded using configuration described in embeddings_json_config (it
uses the embeddings constructor from the orchestrator module, so JSON file
shall follow corresponding format). Documents will be indexed in a vector store (vector_store_json_config)
under index_name index (index_name is generated following the naming restrictions of the vector store,
example for OpenSearch : ns-{namespace}-bot-{bot_id}-session-{uuid4})
The index_name is unique and will be printed to the console at the end
"""

import asyncio
import csv
import json
import logging
import re
import sys
import time
from datetime import datetime
from logging.handlers import RotatingFileHandler
from pathlib import Path
from typing import List
from uuid import uuid4

import pandas as pd
from docopt import docopt
from gen_ai_orchestrator.models.em.azureopenai.azure_openai_em_setting import AzureOpenAIEMSetting
from gen_ai_orchestrator.models.em.em_provider import EMProvider
from gen_ai_orchestrator.models.em.em_setting import BaseEMSetting
from gen_ai_orchestrator.models.em.ollama.ollama_em_setting import OllamaEMSetting
from gen_ai_orchestrator.models.em.openai.openai_em_setting import OpenAIEMSetting
from gen_ai_orchestrator.models.vector_stores.open_search.open_search_setting import OpenSearchVectorStoreSetting
from gen_ai_orchestrator.models.vector_stores.pgvector.pgvector_setting import PGVectorStoreSetting
from gen_ai_orchestrator.models.vector_stores.vector_store_setting import BaseVectorStoreSetting
from gen_ai_orchestrator.models.vector_stores.vectore_store_provider import VectorStoreProvider
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_em_factory,
    get_vector_store_factory,
)

from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_community.document_loaders.dataframe import DataFrameLoader
from langchain_core.documents import Document
from openai import RateLimitError

from indexing_details import IndexingDetails

# Define the size of the csv field -> Set to maximum to process large csvs
csv.field_size_limit(sys.maxsize)

async def index_documents(args):
    """
    Read a ready-to-index CSV file, then index its contents to a Vector Store DB.

    Args:

        args (dict):    A dictionary containing command-line arguments.
                        Expecting keys: '<input_csv>'
                                        '<namespace>'
                                        '<bot_id>'
                                        '<embeddings_json_config>'
                                        '<vector_store_json_config>'
                                        '<chunks_size>'
                                        '<ignore_source>'

    Returns:
        The indexing details.
    """

    start_time = datetime.now()
    formatted_datetime = start_time.strftime('%Y-%m-%d %H:%M:%S')
    session_uuid = str(uuid4())
    logging.debug(f"Beginning indexation session {session_uuid} at '{formatted_datetime}'")

    logging.debug(f"Read input CSV file {args['<input_csv>']}")
    df = pd.read_csv(args['<input_csv>'], delimiter='|', quotechar='"', header=0)
    # Filter the DataFrame to only include rows where the 'text' column is not empty or null
    df_filtered = df[df['text'].notnull() & df['text'].str.strip().ne('')]
    # Prevent NaN value in the 'source' column with a default value 'UNKNOWN', then replace it with None
    if bool(args['<ignore_source>']):
        df_filtered['source'] = None
    else:
        df_filtered['source'] = df_filtered['source'].fillna('UNKNOWN')
        df_filtered['source'] = df_filtered['source'].replace('UNKNOWN', None)

    loader = DataFrameLoader(df_filtered, page_content_column='text')
    docs = loader.load()

    for doc in docs:
        doc.metadata['index_session_id'] = session_uuid
        doc.metadata['index_datetime'] = formatted_datetime
        doc.metadata['id'] = str(uuid4())  # An uuid for the doc (will be used by TOCK)

    logging.debug(f"Split texts in {args['<chunks_size>']} characters-sized chunks")
    text_splitter = RecursiveCharacterTextSplitter(chunk_size=int(args['<chunks_size>']))
    splitted_docs = text_splitter.split_documents(docs)
    # Add chunk id ('n/N') metadata to each chunk
    splitted_docs = generate_ids_for_each_chunks(splitted_docs)
    # Add title to text (for better semantic search)
    splitted_docs = add_title_to_text(splitted_docs)

    logging.debug(f"Get embeddings model from {args['<embeddings_json_config>']} config file")
    with open(Path(args['<embeddings_json_config>']), 'r') as json_file:
        config_dict = json.load(json_file)
    em_settings = load_setting(
        data=config_dict,
        provider_mapping={
            EMProvider.OPEN_AI: OpenAIEMSetting,
            EMProvider.AZURE_OPEN_AI_SERVICE: AzureOpenAIEMSetting,
            EMProvider.OLLAMA: OllamaEMSetting,
        },
        base_class=BaseEMSetting
    )

    logging.debug(f"Get vector store from {args['<vector_store_json_config>']} config file")
    with open(Path(args['<vector_store_json_config>']), 'r') as json_file:
        config_dict = json.load(json_file)
    vector_store_settings = load_setting(
        data=config_dict,
        provider_mapping={
            VectorStoreProvider.OPEN_SEARCH: OpenSearchVectorStoreSetting,
            VectorStoreProvider.PGVECTOR: PGVectorStoreSetting
        },
        base_class=BaseVectorStoreSetting
    )

    # Use embeddings factory from orchestrator
    em_factory = get_em_factory(em_settings)
    #await em_factory.check_embedding_model_setting()
    embeddings = em_factory.get_embedding_model()

    # generating index name
    index_name = normalize_index_name(vector_store_settings.provider, args['<namespace>'], args['<bot_id>'], session_uuid)

    vector_store_factory = get_vector_store_factory(
        setting=vector_store_settings,
        index_name=index_name,
        embedding_function=embeddings
    )
    # await vector_store_factory.check_vector_store_connection()
    vector_store = vector_store_factory.get_vector_store()

    await embedding_and_indexing(splitted_docs, vector_store)

    return IndexingDetails(
        index_name = index_name,
        indexing_session_uuid = session_uuid,
        documents_count = len(docs),
        chunks_count = len(splitted_docs),
        chunk_size = args['<chunks_size>'],
        em_settings = em_settings,
        vector_store_settings = vector_store_settings,
        ignore_source = args['<ignore_source>'],
        input_csv = args['<input_csv>'],
        duration = datetime.now() - start_time
    )


async def embedding_and_indexing(splitted_docs: List[Document], vector_store):
    # Index all chunks in vector DB
    logging.debug('Index chunks in DB')
    bulk_size = 500  # Adjust bulk_size to suit your use case
    for i in range(0, len(splitted_docs), bulk_size):
        logging.debug(f'i={i}, splitted_docs={len(splitted_docs)}')
        await retry_vector_store_add_documents(vector_store, splitted_docs[i: i + bulk_size], bulk_size)

MAX_RETRIES = 5  # Define a maximum number of retries
INITIAL_WAIT_TIME = 15  # Initial wait time in seconds
async def retry_vector_store_add_documents(vector_store, docs_chunk, bulk_size):
    retries = 0
    wait_time = INITIAL_WAIT_TIME
    while retries < MAX_RETRIES:
        try:
            await vector_store.aadd_documents(documents=docs_chunk, bulk_size=bulk_size)
            return  # If successful, exit the retry loop
        except RateLimitError as e:
            # Capture the rate limit error and log it
            logging.warning(f"Rate limit exceeded. Waiting for {wait_time} seconds before retrying...")
            retries += 1
            if retries == MAX_RETRIES:
                logging.error("Maximum retries reached. Unable to index documents.")
                raise  # Rethrow the exception if max retries are reached
            time.sleep(wait_time)  # Wait before retrying
            wait_time *= 2  # Exponential backoff: double the wait time after each retry
        except Exception as e:
            logging.error(f"An error occurred: {e}")
            raise  # Re-raise other types of exceptions


def load_setting(data: dict, provider_mapping: dict, base_class):
    """Function to load and instantiate the right class according to the provider"""
    base_setting = base_class(**data)
    provider = base_setting.provider
    setting_class = provider_mapping.get(provider)

    if setting_class:
        return setting_class(**data)
    else:
        raise ValueError(f"Unknown Provider {provider}.")


def generate_ids_for_each_chunks(
    splitted_docs: List[Document],
) -> List[Document]:
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
    splitted_docs: List[Document],
) -> List[Document]:
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


def normalize_index_name(provider: VectorStoreProvider, namespace: str, bot_id: str, index_session_id: str) -> str:
    if VectorStoreProvider.OPEN_SEARCH == provider :
        return normalize_opensearch_index_name(namespace, bot_id, index_session_id)

    if VectorStoreProvider.PGVECTOR == provider:
        return normalize_pgvector_index_name(namespace, bot_id, index_session_id)

    raise ValueError(f"Unknown Provider {provider}.")

def normalize_pgvector_index_name(namespace: str, bot_id: str, index_session_id: str) -> str:
    """
    Normalize the document index name, base on PGVector rules.
    Same treatment as tock/gen-ai/orchestrator-core/src/main/kotlin/ai/tock/genai/orchestratorcore/utils/PGVectorUtils.kt#normalizeDocumentIndexName()
    """

    # Convert to lowercase
    normalized = f"ns-{namespace}-bot-{bot_id}-session-{index_session_id}".lower()

    # Replace invalid characters with underscores
    normalized = re.sub(r'[^a-z0-9_]', '_', normalized)

    # Ensure the name starts with a letter or underscore
    if not re.match(r'^[a-z_]', normalized):
        normalized = f"_{normalized}"

    return normalized

def normalize_opensearch_index_name(namespace: str, bot_id: str, index_session_id: str) -> str:
    """
    Normalize the document index name, base on OpenSearch rules.
    Same treatment as tock/gen-ai/orchestrator-core/src/main/kotlin/ai/tock/genai/orchestratorcore/utils/OpenSearchUtils.kt#normalizeDocumentIndexName()
    """

    # Convert to lowercase
    normalized = f"ns-{namespace}-bot-{bot_id}-session-{index_session_id}".lower()

    # Replace underscores and spaces with hyphens
    normalized = normalized.replace('_', '-').replace(' ', '-')

    # Remove invalid characters
    invalid_characters = {' ', ',', ':', '"', '*', '+', '/', '\\', '|', '?', '#', '>', '<'}
    normalized = ''.join(c for c in normalized if c not in invalid_characters)

    return normalized


# Configure logging
def setup_logging(cli_args):
    log_format = '%(levelname)s:%(module)s:%(message)s'
    # Create log directory if it doesn't exist
    log_dir = Path('logs')
    log_dir.mkdir(exist_ok=True)

    log_file_name = log_dir / f"index_documents_{datetime.now().strftime('%Y%m%d_%H%M%S')}.log"
    file_handler = RotatingFileHandler(log_file_name, maxBytes=10 * 1024 * 1024, backupCount=5)
    file_handler.setLevel(logging.DEBUG if cli_args['-v'] else logging.INFO)
    file_handler.setFormatter(logging.Formatter(log_format))

    console_handler = logging.StreamHandler()
    console_handler.setLevel(logging.DEBUG if cli_args['-v'] else logging.INFO)
    console_handler.setFormatter(logging.Formatter(log_format))

    logger = logging.getLogger()
    logger.setLevel(logging.DEBUG)  # Set to DEBUG so that both handlers can capture everything

    logger.addHandler(console_handler)
    logger.addHandler(file_handler)

def str_to_bool(value):
    if isinstance(value, bool):
        return value
    if value.lower() in ['true', '1', 'yes']:
        return True
    elif value.lower() in ['false', '0', 'no']:
        return False
    else:
        raise ValueError(f"Cannot proceed: {value} is not a valid boolean value")


async def main(args):
    return await index_documents(args)

if __name__ == '__main__':
    # Parse command-line arguments
    cli_args = docopt(__doc__, version='Webscraper 0.1.0')

    # Set up logging
    setup_logging(cli_args)

    # Check args
    for file_path in ['input_csv', 'embeddings_json_config', 'vector_store_json_config']:
        if not Path(cli_args[f'<{file_path}>']).exists():
            logging.error(f"Cannot proceed: this file {cli_args[f'<{file_path}>']} does not exist")
            sys.exit(1)

    for json_file_path in ['embeddings_json_config', 'vector_store_json_config']:
        try:
            with open(Path(cli_args[f'<{json_file_path}>'])) as file:
                json.load(file)
        except json.JSONDecodeError:
            logging.error(f"Cannot proceed: this file '{cli_args[f'<{json_file_path}>']}' is not a valid JSON file")
            sys.exit(1)

    try:
        int(cli_args['<chunks_size>'])
    except ValueError:
        logging.error(f"Cannot proceed: chunks size ({cli_args['<chunks_size>']}) is not a number")
        sys.exit(1)

    try:
        cli_args['<ignore_source>'] = str_to_bool(cli_args['<ignore_source>'])
    except ValueError as e:
        logging.error(e)
        logging.error(f"Cannot proceed: ignore source ({cli_args['<ignore_source>']}) is not a boolean")
        sys.exit(1)

    # Main func
    details = asyncio.run(index_documents(cli_args))

    # Print indexation session's unique id
    logging.info(details.format_indexing_details())

