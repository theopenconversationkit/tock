"""
Index a CSV file (line format: 'title'|'source'|'text') into a vector database.

Usage:
    run_vectorisation.py [-v] --json-config-file=<jcf>

Description:
    This script indexes the contents of a CSV file into a vector database.
    The CSV must contain 'title', 'source', and 'text' columns. The 'text' will be chunked
    according to the specified chunk size and embedded using settings described json configuration file.
    Document chunks will then be indexed into a vector store specified on the configuration file .
    The index name is automatically generated based on the namespace, bot ID, and a unique identifier
    (UUID). For example, in OpenSearch: ns-{namespace}-bot-{bot_id}-session-{uuid4}.
    Indexing details will be displayed on the console at the end of the operation,
    and saved in a specific log file in ./logs

Arguments:
    --json-config-file=<jcf>   Path to the input config file. This is a required argument.

Options:
    -v                         Enable verbose output for debugging purposes. If not set, the script runs silently except for errors.
    -h, --help                 Display this help message and exit.
    --version                  Display the version of the script.

Examples:
    python run_vectorisation.py --json-config-file=path/to/config-file.json
"""
import copy
import csv
import json
import logging
import re
import sys
from datetime import datetime
from logging.handlers import RotatingFileHandler
from pathlib import Path
from typing import List
from uuid import uuid4

import pandas as pd
from docopt import docopt
from dotenv import load_dotenv
from gen_ai_orchestrator.models.em.azureopenai.azure_openai_em_setting import AzureOpenAIEMSetting
from gen_ai_orchestrator.models.em.bloomz.bloomz_em_setting import BloomzEMSetting
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

from models import IndexingDetails
from scripts.common.logging_config import configure_logging
from scripts.common.models import StatusWithReason, ActivityStatus
from scripts.indexing.vectorisation.models import RunVectorisationOutput, RunVectorisationInput

# Define the size of the csv field -> Set to maximum to process large csvs
csv.field_size_limit(sys.maxsize)

def index_documents() -> IndexingDetails:
    """
    Read a CSV file, then index its contents to a Vector Store DB.

    Returns:
        The indexing details.
    """

    start_time = datetime.now()
    formatted_datetime = start_time.strftime('%Y-%m-%d %H:%M:%S')
    session_uuid = str(uuid4())
    logging.debug(f"Beginning indexation session {session_uuid} at '{formatted_datetime}'")

    logging.debug(f"Read input CSV file {input_csv}")
    df = pd.read_csv(input_csv, delimiter='|', quotechar='"', header=0, dtype=str)
    # Replace NaN values with empty strings in all columns to avoid type issues
    df = df.fillna('')
    # Filter rows where 'text' is not empty or just whitespace
    df_filtered = df[df['text'].str.strip().astype(bool)]
    df_filtered_clone = copy.deepcopy(df_filtered)
    # Set 'source' column to None based on the `ignore_source` option
    if ignore_source:
        df_filtered['source'] = None
    else:
        # Replace any empty strings in 'source' with None
        df_filtered['source'] = df_filtered['source'].replace('', None)

    loader = DataFrameLoader(df_filtered, page_content_column='text')
    docs = loader.load()

    # Add metadata to each document
    for doc, (_, row) in zip(docs, df_filtered_clone.iterrows()):
        doc.metadata['index_session_id'] = session_uuid
        doc.metadata['index_datetime'] = formatted_datetime
        doc.metadata['id'] = str(uuid4())  # An uuid for the doc (will be used by TOCK)
        # Add source metadata regardless of ignore_source
        doc.metadata['reference'] = row['source']

    logging.debug(f"Split texts in {chunks_size} characters-sized chunks")
    text_splitter = RecursiveCharacterTextSplitter(chunk_size=chunks_size)
    splitted_docs = text_splitter.split_documents(docs)
    for doc in splitted_docs:
        doc.page_content = f"```markdown\n{doc.page_content}\n```"

    # Add chunk id ('n/N') metadata to each chunk
    splitted_docs = generate_ids_for_each_chunks(splitted_docs)

    if append_title:
        # Add title to text (for better semantic search)
        splitted_docs = add_title_to_text(splitted_docs)

    logging.debug(f"Get embeddings model from {embeddings_json_config} config file")
    with open(Path(embeddings_json_config), 'r') as json_file:
        config_dict = json.load(json_file)
    em_settings = load_setting(
        data=config_dict,
        provider_mapping={
            EMProvider.OPEN_AI: OpenAIEMSetting,
            EMProvider.AZURE_OPEN_AI_SERVICE: AzureOpenAIEMSetting,
            EMProvider.OLLAMA: OllamaEMSetting,
            EMProvider.BLOOMZ: BloomzEMSetting,
        },
        base_class=BaseEMSetting
    )

    logging.debug(f"Get vector store from {vector_store_json_config} config file")
    with open(Path(vector_store_json_config), 'r') as json_file:
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
    em_factory.check_embedding_model_setting()
    embeddings = em_factory.get_embedding_model()

    # generating index name
    index_name = normalize_index_name(vector_store_settings.provider, namespace, bot_id, session_uuid)

    vector_store_factory = get_vector_store_factory(
        setting=vector_store_settings,
        index_name=index_name,
        embedding_function=embeddings
    )
    vector_store_factory.check_vector_store_connection()
    vector_store = vector_store_factory.get_vector_store(async_mode=False)

    embedding_and_indexing(splitted_docs, vector_store)

    return IndexingDetails(
        index_name = index_name,
        indexing_session_uuid = session_uuid,
        documents_count = len(docs),
        chunks_count = len(splitted_docs),
        chunk_size = chunks_size,
        em_settings = em_settings,
        vector_store_settings = vector_store_settings,
        ignore_source = ignore_source,
        input_csv = input_csv,
        duration = datetime.now() - start_time
    )

def validate_positive_integer(args, option_name) -> int:
    """
    Validate that a given value is a positive integer.
    If invalid, log an error and exit the program.

    :param args: The script args.
    :param option_name: The name of the option being validated (for error messages).
    :return: The value as an integer if valid.
    """
    try:
        int_value = int(args[option_name])
        if int_value <= 0:
            raise ValueError
        return int_value
    except ValueError:
        logging.error(f"{option_name} must be a valid positive integer.")
        sys.exit(1)

def validate_boolean(args, option_name):
    """
    Validate that a given value can be interpreted as a boolean.
    If invalid, log an error and exit the program.

    :param args: The script args.
    :param option_name: The name of the option being validated (for error messages).
    :return: The value as a boolean if valid.
    """
    truthy_values = {"true", "1", "yes", "y"}
    falsy_values = {"false", "0", "no", "n"}

    if args[option_name].lower() in truthy_values:
        return True
    elif args[option_name].lower() in falsy_values:
        return False
    else:
        logging.error(f"{option_name} must be a valid boolean (e.g., 'true' or 'false').")
        sys.exit(1)

def validate_file(file_path, allowed_extension):
    """
    Validate that a file exists, has a single allowed extension, and if it's a JSON file, is valid JSON.

    :param file_path: Path to the file to validate.
    :param allowed_extension: A single allowed file extension (e.g., "csv" or "json").
    :return: The file path if valid, else exit.
    """
    # Check if file exists
    if not Path(file_path).exists():
        logging.error(f"Cannot proceed: the file '{file_path}' does not exist.")
        sys.exit(1)

    # Check if the file has the allowed extension
    file_extension = Path(file_path).suffixes[-1],  # Only the last part of the suffix
    file_extension = file_extension[0].lstrip(".").lower()
    if file_extension != allowed_extension.lower():
        logging.error(f"Cannot proceed: '{file_path}' must have the '{allowed_extension}' extension.")
        sys.exit(1)

    # If it's a JSON file, validate the JSON format
    if file_extension == "json":
        try:
            with open(Path(file_path), "r") as file:
                json.load(file)
        except json.JSONDecodeError:
            logging.error(f"Cannot proceed: '{file_path}' is not a valid JSON file.")
            sys.exit(1)

    return file_path

def embedding_and_indexing(splitted_docs: List[Document], vector_store):
    # Index all chunks in vector DB
    logging.debug('Index document chunks in DB')

    for i in range(0, len(splitted_docs), embedding_bulk_size):
        logging.info(f'i={i}, splitted_docs={len(splitted_docs)}')
        vector_store.add_documents(documents=splitted_docs[i: i + embedding_bulk_size], bulk_size=embedding_bulk_size)

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
def setup_logging():
    log_format = '%(levelname)s:%(module)s:%(message)s'
    # Create log directory if it doesn't exist
    log_dir = Path('logs')
    log_dir.mkdir(exist_ok=True)

    log_file_name = log_dir / f"index_documents_{datetime.now().strftime('%Y%m%d_%H%M%S')}.log"
    file_handler = RotatingFileHandler(log_file_name, maxBytes=10 * 1024 * 1024, backupCount=5)
    file_handler.setLevel(logging.DEBUG if verbose else logging.INFO)
    file_handler.setFormatter(logging.Formatter(log_format))

    console_handler = logging.StreamHandler()
    console_handler.setLevel(logging.DEBUG if verbose else logging.INFO)
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

def main():
    start_time = datetime.now()
    cli_args = docopt(__doc__, version='Run Vectorisation 1.0.0')
    logger = configure_logging(cli_args)

    index_name: str = ""
    session_uuid: str = ""
    documents_count: int = 0
    chunks_count: int = 0
    try:
        logger.info("Loading input data...")
        input_config = RunVectorisationInput.from_json_file(cli_args['--json-config-file'])
        logger.debug(f"\n{input_config.format()}")

        location = f"{input_config.bot.file_location}/{input_config.bot.namespace}-{input_config.bot.bot_id}"
        data_csv_path = f"{location}/input/{input_config.data_csv_path}"

        activity_status = StatusWithReason(status=ActivityStatus.COMPLETED)
    except Exception as e:
        full_exception_name = f"{type(e).__module__}.{type(e).__name__}"
        activity_status = StatusWithReason(status=ActivityStatus.FAILED, status_reason=f"{full_exception_name} : {e}")
        logger.error(e, exc_info=True)

    output = RunVectorisationOutput(
        status = activity_status,
        index_name=index_name,
        session_uuid=session_uuid,
        chunks_count=documents_count,
        items_count=chunks_count,
        duration=datetime.now() - start_time,
        success_rate=100
    )
    logger.debug(f"\n{output.format()}")

if __name__ == '__main__':
    main()

    exit(1)
    # Parse command-line arguments
    args = docopt(__doc__, version='Index Documents 1.0')

    # Access arguments, using defaults where applicable
    verbose = args['-v']  # Boolean flag
    input_csv = validate_file(args['--input-csv'], allowed_extension='csv')
    namespace = args['--namespace']
    bot_id = args['--bot-id']
    embeddings_json_config = validate_file(args['--embeddings-json-config'], allowed_extension='json')
    vector_store_json_config = validate_file(args['--vector-store-json-config'], allowed_extension='json')
    chunks_size = validate_positive_integer(args, option_name='--chunks-size')
    ignore_source = validate_boolean(args, option_name='--ignore-source')
    append_title = validate_boolean(args, option_name='--append-title')
    embedding_bulk_size = validate_positive_integer(args, option_name='--embedding-bulk-size')

    # Load .env file if provided
    env_file = args['--env-file']
    if env_file:
        print(f"Loading environment variables from: {env_file}")
        load_dotenv(env_file)

    # Set up logging
    setup_logging()

    # Main func
    details = index_documents()

    # Print indexation session's unique id
    logging.info(details.format_indexing_details())
