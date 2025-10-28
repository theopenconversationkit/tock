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
from langchain.prompts import (
    ChatPromptTemplate,
    HumanMessagePromptTemplate,
    PromptTemplate,
)
from langchain_core.output_parsers import JsonOutputParser
from more_itertools.recipes import batched
from scripts.common.logging_config import configure_logging
from scripts.common.models import ActivityStatus, StatusWithReason
from scripts.indexing.normalization.models import (
    RunChunkContextualizationInput,
    RunChunkContextualizationOutput,
)

from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_callback_handler_factory,
    get_llm_factory,
)


def load_input_data(cli_args, logger):
    """Loads and parses the configuration JSON file."""

    logger.info('Loading input data...')
    input_config = RunChunkContextualizationInput.from_json_file(cli_args['--json-config-file'])
    logger.debug(f"\n{input_config.format()}")
    return input_config


def create_prompt():
    """Create and configure the prompt template."""

    contextual_retrieval_system_prompt = """
[DOCUMENT START]
{WHOLE_DOCUMENT_WITH_PAGE_MARKERS}
[DOCUMENT END]

The full document above describes a single financial product.

Now, focus only on **page {N_PAGE}**, which is delimited in the document using this marker: `{N_PAGE}------------------------------------------------`.

Your task is to extract well-structured, standalone information from **page {N_PAGE}**.
You must analyze the entire document for context and terminology, but only extract and chunk content from the specified page.
Decompose the "Content" into clear and simple propositions, ensuring they are interpretable out of context.
1. Split compound sentence into simple sentences. Maintain the original phrasing from the input whenever possible.
2. For any named entity that is accompanied by additional descriptive information, separate this information into its own distinct proposition.
3. Decontextualize the proposition by adding necessary modifier to nouns or entire sentences and replacing pronouns (e.g., "it", "he", "she", "they", "this", "that") with the full name of the entities they refer to.

There are two possible layouts for a page:

---

### 1. If the page contains regular text:

- Split the content into **small, self-contained chunks** (phrases or short paragraphs).
- Each chunk must be:
  - **Fully understandable on its own**, without referring to the rest of the page or document.
  - **Contextualized**: mention the product name (if detected), clarify dates or financial terms, avoid vague expressions like "see above".
  - **Rewritten clearly**, using information from the entire document to resolve references or footnotes (e.g. "(1)", "(2)").
  - **Formulated in the same language** as the document (French).

---

### 2. If the page contains a table:

- Extract **each row** of the table as a **distinct, contextualized sentence**.
- For each row, write:
  - A **full sentence** describing the meaning of that row.
  - Mention the **financial product name** if available.
  - Make sure the sentence is **autonomous** and includes both the row label and its value, reformulated naturally.

> Example:
> If the row is `Durée de placement | 6 ans`, write:
> *Le produit [Nom du produit] propose une durée de placement de 6 ans.*

---

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
    pattern = r'\{(\d+)\}-+'
    return [int(num) for num in re.findall(pattern, document_content)]

def process_chunks(chunks, contextual_chunk_creation, reference_document_content, observability_handler):
    """Processes chunks in batches and applies contextualization."""
    result, csv_rows = [], []

    page_numbers = extraire_page_numbers(reference_document_content)
    print(f"Detected pages: {page_numbers}")

    for page in page_numbers:
    # for page in [10]:

        print(f"Page: {page} - Detected pages: {page_numbers}")
        response = contextual_chunk_creation.invoke(
            {'WHOLE_DOCUMENT_WITH_PAGE_MARKERS': reference_document_content, 'N_PAGE': page},
            config={'callbacks': [observability_handler] if observability_handler else []}
        )

        for chunk in response['chunks']:
            csv_rows.append([
                response['product_name'],
                '',
                f"---\nProduct Name: {response['product_name']}\nPage: {response['page']}\n---\n\n{chunk['text']}"
            ])

        result.append(response)

    return result, csv_rows


def save_results(result, csv_rows, output_path, formatted_datetime):
    """Saves results in CSV and JSON format."""
    csv_filename = f"{output_path.rsplit('.', 1)[0]}-{formatted_datetime}.csv"
    json_filename = f"{output_path.rsplit('.', 1)[0]}-chunk-context-{formatted_datetime}.json"

    with open(csv_filename, 'w', newline='', encoding='utf-8') as file:
        writer = csv.writer(file, delimiter='|')
        writer.writerow(['title', 'source', 'text'])
        writer.writerows(csv_rows)

    merged_data = {
        'product_name': result[0]['product_name'] if result else 'Unknown',
        'chunks': [chunk for item in result for chunk in item['chunks']]
    }

    with open(json_filename, 'w', encoding='utf-8') as json_file:
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

        with open(reference_document_path, 'r', encoding='utf-8') as f:
            reference_document_content = f.read()

        llm_model_instance = get_llm_factory(input_config.llm_setting).get_language_model()
        observability_callback_handler = None
        if input_config.observability_setting:
            observability_callback_handler = get_callback_handler_factory(
                setting=input_config.observability_setting
            ).get_callback_handler(
                trace_name='Chunk contextualization'
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
        logger.error('Invalid JSON format in config file.', exc_info=True)
        activity_status = StatusWithReason(status=ActivityStatus.FAILED, status_reason='Invalid JSON format.')
    except Exception as e:
        logger.error('An unexpected error occurred', exc_info=True)
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
