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
import re
import sys
from datetime import datetime
from typing import List
from uuid import uuid4

import pandas as pd
from docopt import docopt
from gen_ai_orchestrator.models.vector_stores.vectore_store_provider import VectorStoreProvider
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_em_factory,
    get_vector_store_factory,
)
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_community.document_loaders.dataframe import DataFrameLoader
from langchain_core.documents import Document

from scripts.common.logging_config import configure_logging
from scripts.common.models import StatusWithReason, ActivityStatus
from scripts.indexing.vectorisation.models import RunVectorisationOutput, RunVectorisationInput

# Define the size of the csv field -> Set to maximum to process large csvs
csv.field_size_limit(sys.maxsize)


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


def main():
    start_time = datetime.now()
    formatted_datetime = start_time.strftime('%Y-%m-%d %H:%M:%S')
    cli_args = docopt(__doc__, version='Run Vectorisation 1.0.0')
    logger = configure_logging(cli_args)

    index_name: str = ""
    session_uuid: str = str(uuid4())
    documents_count: int = 0
    chunks_count: int = 0
    try:
        logger.info("Loading input data...")
        input_config = RunVectorisationInput.from_json_file(cli_args['--json-config-file'])
        logger.debug(f"\n{input_config.format()}")

        location = f"{input_config.bot.file_location}/{input_config.bot.namespace}-{input_config.bot.bot_id}"
        data_csv_file = f"{location}/input/{input_config.data_csv_file}"


        logger.debug(f"Beginning indexation session {session_uuid} at '{formatted_datetime}'")

        logger.debug(f"Read input CSV file {data_csv_file}")
        df = pd.read_csv(data_csv_file, delimiter='|', quotechar='"', header=0, dtype=str)
        # Replace NaN values with empty strings in all columns to avoid type issues
        df = df.fillna('')
        # Filter rows where 'text' is not empty or just whitespace
        df_filtered = df[df['text'].str.strip().astype(bool)]
        df_filtered_clone = copy.deepcopy(df_filtered)
        # Set 'source' column to None based on the `ignore_source` option
        if input_config.ignore_source:
            df_filtered['source'] = None
        else:
            # Replace any empty strings in 'source' with None
            df_filtered['source'] = df_filtered['source'].replace('', None)

        loader = DataFrameLoader(df_filtered, page_content_column='text')
        docs = loader.load()
        documents_count = len(docs)

        # Add metadata to each document
        for doc, (_, row) in zip(docs, df_filtered_clone.iterrows()):
            doc.metadata['index_session_id'] = session_uuid
            doc.metadata['index_datetime'] = formatted_datetime
            doc.metadata['id'] = str(uuid4())  # An uuid for the doc (will be used by TOCK)
            # Add source metadata regardless of ignore_source
            doc.metadata['reference'] = row['source']

        logger.debug(f"Split texts in {input_config.chunk_size} characters-sized chunks")
        text_splitter = RecursiveCharacterTextSplitter(chunk_size=input_config.chunk_size)
        splitted_docs = text_splitter.split_documents(docs)
        for doc in splitted_docs:
            doc.page_content = f"```markdown\n{doc.page_content}\n```"

        # Add chunk id ('n/N') metadata to each chunk
        splitted_docs = generate_ids_for_each_chunks(splitted_docs)

        if input_config.append_doc_title_and_chunk:
            # Add title to text (for better semantic search)
            for doc in splitted_docs:
                # Add title to page_content
                if 'title' in doc.metadata:
                    title = doc.metadata['title']
                    doc.page_content = f'{title}\n\n{doc.page_content}'

        em_factory = get_em_factory(input_config.em_setting)

        # generating index name
        index_name = normalize_index_name(input_config.vector_store_setting.provider, input_config.bot.namespace, input_config.bot.bot_id, session_uuid)

        vector_store_factory = get_vector_store_factory(
            setting=input_config.vector_store_setting,
            index_name=index_name,
            embedding_function=em_factory.get_embedding_model()
        )
        vector_store = vector_store_factory.get_vector_store(async_mode=False)

        # Index all chunks in vector DB
        chunks_count = len(splitted_docs)
        for i in range(0, len(splitted_docs), input_config.embedding_bulk_size):
            vector_store.add_documents(documents=splitted_docs[i: i + input_config.embedding_bulk_size],
                                       bulk_size=input_config.embedding_bulk_size)

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