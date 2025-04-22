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
import re
from datetime import datetime

from docopt import docopt
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import get_llm_factory, \
    get_callback_handler_factory
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

    contextual_retrieval_system_prompt = """
<document>
{WHOLE_DOCUMENT_WITH_PAGE_MARKERS}
</document>

The full document above describes a single financial product.

Now, focus only on **page {N_PAGE}**, which is delimited in the document using this marker: `{N_PAGE}------------------------------------------------`.

From this page:
- **Extract key information** and express it in the form of **small, standalone, contextualized chunks**.
- Each chunk must be **autonomous**, meaning it should contain **all the information needed to understand it without referring to other parts of the document**.
- You can and should use the rest of the document to **resolve references**, like:
  - Footnotes (e.g., "(1)", "(2)", etc.).
  - Terms like "see above", "the level", "the observation date", etc.
  - Any abbreviation or terminology that appears earlier or is explained elsewhere in the document.

Follow this reasoning for each chunk:
1. Identify ambiguous terms or references.
2. Resolve them using the rest of the document if needed.
3. Rewrite the chunk clearly, explicitly, and naturally.
4. Mention the financial product name in each chunk if it can be inferred.
5. Maintain the language of the document (French).

Return your response in the following format:

```json
{{
  \"product_name\": \"DETECTED_PRODUCT_NAME\",
  \"page\": {N_PAGE},
  \"chunks\": [
    {{\"id\": \"1\", \"text": \"First autonomous and contextualized chunk from page {N_PAGE}.\"}},
    {{\"id\": \"2\", \"text": \"Second autonomous and contextualized chunk from page {N_PAGE}.\"}}
  ]
}}
            """

    prompt_template = PromptTemplate(
        input_variables=['WHOLE_DOCUMENT_WITH_PAGE_MARKERS', 'N_PAGE'],
        template=contextual_retrieval_system_prompt
    )
    human_message_prompt = HumanMessagePromptTemplate(prompt=prompt_template)
    return ChatPromptTemplate(input_variables=['WHOLE_DOCUMENT_WITH_PAGE_MARKERS', 'N_PAGE'], messages=[human_message_prompt])

def extraire_page_numbers(document_content: str):
    pattern = r"\{(\d+)\}-+"
    return [int(num) for num in re.findall(pattern, document_content)]

def process_chunks(chunks, contextual_chunk_creation, reference_document_content, observability_handler):
    """Processes chunks in batches and applies contextualization."""
    result, csv_rows = [], []

    page_numbers = extraire_page_numbers(reference_document_content)
    print(f"Detected pages: {page_numbers}")

    # for page in page_numbers:
    for page in [8, 9 , 10]:

        response = contextual_chunk_creation.invoke(
            {"WHOLE_DOCUMENT_WITH_PAGE_MARKERS": reference_document_content, "N_PAGE": page},
            config={'callbacks': [observability_handler] if observability_handler else []}
        )
        print(response["chunks"])

        # content_dict = {chunk.id: chunk.content for chunk in chunks_group}
        #
        # for ctx in response["contexts"]:
        #     if ctx["id"] in content_dict:
        #         ctx["content"] = content_dict[ctx["id"]]
        #         csv_rows.append([
        #             response['product_name'],
        #             "",
        #             f"---\nproduct_name:{response['product_name']}\ncontext:{ctx['context']}\n---\n\n{content_dict[ctx['id']]}"
        #         ])
        #
        # result.append(response)

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

        llm_model_instance = get_llm_factory(input_config.llm_setting).get_language_model()
        observability_callback_handler = None
        if input_config.observability_setting:
            observability_callback_handler = get_callback_handler_factory(
                setting=input_config.observability_setting
            ).get_callback_handler(
                trace_name="Chunk contextualization"
            )

        contextual_chunk_creation = (
                create_prompt() | llm_model_instance | JsonOutputParser()
        )

        result, csv_rows = process_chunks(
            input_config.chunks, contextual_chunk_creation, reference_document_content, observability_callback_handler
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
