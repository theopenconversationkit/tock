"""
Run chunk contextualization.

Usage:
    run_chunk_contextualization.py [-v] --json-config-file=<jcf>

Description:
    This script is used to contextualize a document extract within the document itself, giving it greater meaning.

Arguments:
    --json-config-file=<jcf>   Path to the input config file. This is a required argument.

Options:
    -v                         Enable verbose output for debugging purposes. If not set, the script runs silently except for errors.
    -h, --help                 Display this help message and exit.
    --version                  Display the version of the script.

Examples:
    python run_chunk_contextualization.py --json-config-file=path/to/config-file.json
"""

import csv
import json
from datetime import datetime

from docopt import docopt
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import get_llm_factory, \
    create_observability_callback_handler
from langchain.prompts import ChatPromptTemplate, PromptTemplate, HumanMessagePromptTemplate
from langchain_core.output_parsers import JsonOutputParser
from more_itertools.recipes import batched

from scripts.common.logging_config import configure_logging
from scripts.common.models import StatusWithReason, ActivityStatus
from scripts.indexing.normalization.models import RunChunkContextualizationInput, RunChunkContextualizationOutput


def load_input_data(cli_args, logger):
    """Loads and parses the configuration JSON file."""

    logger.info("Loading input data...")
    input_config = RunChunkContextualizationInput.from_json_file(cli_args["--json-config-file"])
    logger.debug(f"\n{input_config.format()}")
    return input_config


def create_prompt():
    """Create and configure the prompt template."""

    contextual_retrieval_system_prompt = """<document>
                    {WHOLE_DOCUMENT}
            </document>

            The following are key extracted chunks from the document:
            <chunks>
            {CHUNKS}
            </chunks>

            From the document content, determine the name of the financial product it describes.

            For each chunk, think through the following steps:
            1. **Identify references to data** (e.g., "Niveau Initial(2)", "Date d'Observation Finale(1)"). Think about what each reference could be referring to.
            2. **Locate the values** in the document that correspond to these references. Think about how the context in the document might help you find these values.
            3. **Replace the references** with their actual values. Think about the effect of this replacement on the clarity of the chunk.
            4. **Generate a succinct yet comprehensive context** for each chunk:
                - Make sure that the chunk description begins directly with its content, without using an introduction such as 'This chunk'. Rephrase to make the description more natural and integrated into the text..
                - Clearly identify the financial product it belongs to. Think about how you can deduce the product name from the document.
                - Ensure that all relevant details needed to understand the chunk are included. Think about what is necessary to contextualize the chunk.
                - Resolve any references (e.g., dates, terms, or entities) explicitly by replacing them with their corresponding values from the document. Think about how to make these references clear for future searches.
                - Maintain clarity and conciseness while preserving critical information. Think about how to summarize the chunk without losing important details.
                - Ensure that the response is in the language of the original document. If the document is in French, respond in French. If it is in English, respond in English. Think about which language the document is written in before responding.
                - Use the correct chunk Id as provided. Think about how to ensure each chunk’s context is correctly mapped to its Id.

            Now, respond with the context for each chunk in the following JSON format. Be sure to use the same chunk Id as provided:

            ```json
            {{
                \"product_name\": \"DETECTED_PRODUCT_NAME\",
                \"contexts\": [
                    {{\"id\": \"1\", \"context\": \"Generated context for chunk 1\"}},
                    {{\"id\": \"2\", \"context\": \"Generated context for chunk 2\"}}
                ]
            }}
            """

    prompt_template = PromptTemplate(
        input_variables=['WHOLE_DOCUMENT', 'CHUNKS'],
        template=contextual_retrieval_system_prompt
    )
    human_message_prompt = HumanMessagePromptTemplate(prompt=prompt_template)
    return ChatPromptTemplate(input_variables=['WHOLE_DOCUMENT', 'CHUNKS'], messages=[human_message_prompt])


def process_chunks(chunks, contextual_chunk_creation, reference_document_content, observability_handler):
    """Processes chunks in batches and applies contextualization."""
    result, csv_rows = [], []

    for chunks_group in batched(chunks, 5):
        formatted_chunks = "\n".join(
            [f"<chunk id='{chunk.id}'>\n{chunk.content}\n</chunk>" for chunk in chunks_group]
        )

        response = contextual_chunk_creation.invoke(
            {"WHOLE_DOCUMENT": reference_document_content, "CHUNKS": formatted_chunks},
            config={'callbacks': [observability_handler]}
        )

        content_dict = {chunk.id: chunk.content for chunk in chunks_group}

        for ctx in response["contexts"]:
            if ctx["id"] in content_dict:
                ctx["content"] = content_dict[ctx["id"]]
                csv_rows.append([
                    response['product_name'],
                    "",
                    f"---\nproduct_name:{response['product_name']}\ncontext:{ctx['context']}\n---\n\n{content_dict[ctx['id']]}"
                ])

        result.append(response)

    return result, csv_rows


def save_results(result, csv_rows, output_path, formatted_datetime):
    """Saves results in CSV and JSON format."""
    csv_filename = f"{output_path.rsplit('.', 1)[0]}-{formatted_datetime}.csv"
    json_filename = f"{output_path.rsplit('.', 1)[0]}-chunk-context-{formatted_datetime}.json"

    with open(csv_filename, "w", newline="", encoding="utf-8") as file:
        writer = csv.writer(file, delimiter='|')
        writer.writerow(["title", "source", "text"])
        writer.writerows(csv_rows)

    merged_data = {
        "product_name": result[0]["product_name"] if result else "Unknown",
        "contexts": [ctx for item in result for ctx in item["contexts"]]
    }

    with open(json_filename, "w", encoding="utf-8") as json_file:
        json.dump(merged_data, json_file, ensure_ascii=False, indent=2)


def main():
    start_time = datetime.now()
    formatted_datetime = start_time.strftime('%Y-%m-%d_%Hh%Mm%S')

    cli_args = docopt(__doc__, version='Run Chunk Contextualization 1.0.0')
    logger = configure_logging(cli_args)

    total_chunks: int = 0
    total_processed_chunks: int = 0

    try:
        input_config = load_input_data(cli_args, logger)
        total_chunks = len(input_config.chunks)

        location = f"{input_config.bot.file_location}/{input_config.bot.namespace}-{input_config.bot.bot_id}"
        reference_document_path = f"{location}/input/{input_config.reference_document_name}"
        output_path = f"{location}/output/{input_config.reference_document_name}"

        with open(reference_document_path, "r", encoding="utf-8") as f:
            reference_document_content = f.read()

        observability_handler = create_observability_callback_handler(
            observability_setting=input_config.observability_setting,
            trace_name="Chunk contextualization"
        )
        llm_model_instance = get_llm_factory(input_config.llm_setting).get_language_model()

        contextual_chunk_creation = (
                create_prompt() | llm_model_instance | JsonOutputParser()
        )

        result, csv_rows = process_chunks(
            input_config.chunks, contextual_chunk_creation, reference_document_content, observability_handler
        )
        total_processed_chunks = len(csv_rows)
        save_results(result, csv_rows, output_path, formatted_datetime)

        activity_status = StatusWithReason(status=ActivityStatus.COMPLETED)
    except FileNotFoundError as e:
        logger.error(f"File not found: {e.filename}", exc_info=True)
        activity_status = StatusWithReason(status=ActivityStatus.FAILED, status_reason=f"FileNotFoundError: {e}")
    except json.JSONDecodeError as e:
        logger.error("Invalid JSON format in config file.", exc_info=True)
        activity_status = StatusWithReason(status=ActivityStatus.FAILED, status_reason="Invalid JSON format.")
    except Exception as e:
        logger.error("An unexpected error occurred", exc_info=True)
        activity_status = StatusWithReason(status=ActivityStatus.FAILED, status_reason=str(e))

    output = RunChunkContextualizationOutput(
        status=activity_status,
        duration=datetime.now() - start_time,
        items_count=total_chunks,
        success_rate=(100 * total_processed_chunks / total_chunks) if total_chunks > 0 else 0
    )
    logger.debug(f"\n{output.format()}")

if __name__ == '__main__':
    main()
