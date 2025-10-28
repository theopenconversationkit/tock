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
Run image contextualization.

Usage:
    run_image_contextualization.py [-v] --json-config-file=<jcf>

Description:
    This script is used to contextualize the image within the document.
    The aim is to replace the image on MD with its description.

Arguments:
    --json-config-file=<jcf>   Path to the input config file. This is a required argument.

Options:
    -v                         Enable verbose output for debugging purposes. If not set, the script runs silently except for errors.
    -h, --help                 Display this help message and exit.
    --version                  Display the version of the script.

Examples:
    python run_image_contextualization.py --json-config-file=path/to/config-file.json
"""

import os
import re
from datetime import datetime

import backoff
from docopt import docopt
from langchain_core.messages import HumanMessage, SystemMessage
from markdown_it import MarkdownIt
from openai import APITimeoutError, RateLimitError
from scripts.common.img_tools import encode_image
from scripts.common.logging_config import configure_logging
from scripts.common.models import ActivityStatus, StatusWithReason
from scripts.common.utils import read_file, save_json, write_file
from scripts.indexing.normalization.models import (
    RunImageContextualizationInput,
    RunImageContextualizationOutput,
)

from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_callback_handler_factory,
    get_llm_factory,
)


def extract_images(md_file_path):
    with open(md_file_path, 'r', encoding='utf-8') as _md_file:
        md = MarkdownIt().parse(_md_file.read())

    current_header = None
    elements = []

    # Process tokens to build elements list
    i = 0
    while i < len(md):
        token = md[i]
        if token.type == 'heading_open' and i + 1 < len(md) and md[i + 1].type == 'inline':
            current_header = md[i + 1].content.strip()
            elements.append({'type': 'heading', 'content': current_header})
            i += 3  # Skip heading_open, inline and heading_close
        else:
            if token.type == 'inline' and token.children:
                for child in token.children:
                    if child.type == 'image':
                        elements.append({
                            'type': 'image',
                            'src': child.attrs.get('src', ''),
                            'header': current_header
                        })
                    elif child.type == 'text' and child.content.strip():
                        elements.append({'type': 'text', 'content': child.content.strip()})
            elif token.content.strip() and token.type not in ['paragraph_open', 'paragraph_close']:
                elements.append({'type': 'text', 'content': token.content.strip()})
            i += 1

    # Collect context for images
    results = []
    for idx, element in enumerate(elements):
        if element['type'] != 'image':
            continue

        # Find previous texts
        prev_texts = []
        for prev in reversed(elements[:idx]):
            if prev['type'] == 'heading':
                break
            if prev['type'] == 'text':
                prev_texts.append(prev['content'])
        prev_texts.reverse()

        # Find next texts
        next_texts = []
        for next_ in elements[idx + 1:]:
            if next_['type'] == 'heading':
                break
            if next_['type'] == 'text':
                next_texts.append(next_['content'])

        results.append({
            'name': element['src'],
            'path': f'{os.path.dirname(md_file_path)}/{element["src"]}',
            'prev_header': element['header'],
            'prev_text': ' '.join(prev_texts).strip(),
            'next_text': ' '.join(next_texts).strip()
        })

    return results


def generate_image_description(llm, observability_callback_handler, image):
    base64_image = encode_image(image['path'])

    system_prompt = """
    You are an AI assistant specialized in analyzing financial and documentary images for accessibility.
    Your mission is twofold:

    1. UNDERSTAND THE MEANING AND PURPOSE of each visual element in its documentary context, not just describe what is visible.
    2. PROVIDE A USEFUL DESCRIPTION that effectively conveys the information contained in the image.

    For financial charts, tables, and diagrams:
    - First, explain the MAIN OBJECTIVE of the chart (what is it trying to show?)
    - Summarize the KEY INFORMATION and important trends
    - Mention ESSENTIAL SPECIFIC DATA (percentages, amounts, dates)
    - Avoid overly detailed descriptions of secondary visual elements

    For logos and certifications:
    - Identify the organization name and type of certification
    - If it is a simple brand or company logo, respond with "LOGO: [Organization Name]"

    For decorative images without informational content:
    - Respond only with "ILLUSTRATION"

    Your goal is to allow someone who cannot see the image to fully understand the INFORMATION it conveys, not just how it looks.

    IMPORTANT: Your response must always be in **French**."""

    user_prompt = f"""
    Analyze the CONTENT AND MEANING of this image, considering the following documentary context:

    Header: {image.get('prev_header', 'Non disponible')}
    Text preceding the image: {image.get('prev_text', 'Non disponible')}
    Text following the image: {image.get('next_text', 'Non disponible')}

    IMPORTANT: If this is a financial chart, focus on the information it communicates rather than its appearance.

    If it is a simple company or brand logo, respond "LOGO: [Name]" or "ILLUSTRATION" if purely decorative.

    Do not start your response with "L'image montre/représente/contient..." - go straight to the essential information.
"""

    @backoff.on_exception(wait_gen=backoff.expo, exception=APITimeoutError, max_tries=2)
    @backoff.on_exception(wait_gen=backoff.expo, exception=RateLimitError, max_tries=3)
    def invoke_llm():
        return llm.invoke([
            SystemMessage(content=system_prompt),
            HumanMessage(
                content=[
                    {'type': 'text', 'text': user_prompt},
                    {'type': 'image_url', 'image_url': {'url': f"data:image/jpeg;base64,{base64_image}"}}
                ]
            )
        ], config={
            'callbacks': [observability_callback_handler] if observability_callback_handler else []
        })

    return invoke_llm().content


def main():
    start_time = datetime.now()
    formatted_datetime = start_time.strftime('%Y-%m-%d_%Hh%Mm%S')
    cli_args = docopt(__doc__, version='Run Image Contextualization 1.0.0')
    logger = configure_logging(cli_args)

    nb_discovered_images = 0
    nb_described_images = 0
    markdown_output = ''

    try:
        logger.info('Loading input data...')
        input_config = RunImageContextualizationInput.from_json_file(cli_args['--json-config-file'])
        logger.debug(f"\n{input_config.format()}")

        # Define file paths
        location = f"{input_config.bot.file_location}/{input_config.bot.namespace}-{input_config.bot.bot_id}"
        output_dir = f'{location}/output/{input_config.document_directory}'
        os.makedirs(output_dir, exist_ok=True)  # Ensure output directory exists

        md_input_file_path = f"{location}/input/{input_config.document_directory}/{input_config.document_directory}.md"
        md_output_file_path = f"{output_dir}/{input_config.document_directory}.md"

        md_input_filename = md_output_file_path.rsplit('.', 1)[0]
        json_output_images_path = f"{md_input_filename}-images-{formatted_datetime}.json"
        markdown_output = f"{md_input_filename}-enriched-{formatted_datetime}.md"

        # Extract images from markdown
        images = extract_images(md_input_file_path)
        nb_discovered_images = len(images)

        # Save extracted images data as JSON
        save_json(json_output_images_path, images)

        # Initialize LLM and Observability for image descriptions
        llm_factory = get_llm_factory(setting=input_config.llm_setting)
        observability_callback_handler = None
        if input_config.observability_setting:
            observability_callback_handler = get_callback_handler_factory(
                setting=input_config.observability_setting
            ).get_callback_handler(
                trace_name='Image contextualization'
            )

        # Process each image to generate a description
        for i, image in enumerate(images):
            image['description'] = ''  # Initialize description
            if image['name']:
                logger.info(f"Processing image {i + 1}/{len(images)}: {image['name']}")
                image['description'] = generate_image_description(
                    llm_factory.get_language_model(), observability_callback_handler, image
                )
                nb_described_images += 1

        # Save updated images data with descriptions
        save_json(json_output_images_path, images)

        # Read markdown content
        markdown_text = read_file(md_input_file_path)

        # Replace image placeholders with generated descriptions
        markdown_text = replace_image_descriptions(markdown_text, images)

        # Save enriched markdown
        write_file(markdown_output, markdown_text)

        activity_status = StatusWithReason(status=ActivityStatus.COMPLETED)
    except Exception as e:
        full_exception_name = f"{type(e).__module__}.{type(e).__name__}"
        activity_status = StatusWithReason(status=ActivityStatus.FAILED, status_reason=f"{full_exception_name} : {e}")
        logger.error(e, exc_info=True)

    output = RunImageContextualizationOutput(
        status=activity_status,
        output_filename=os.path.basename(markdown_output) if markdown_output else '',
        duration=datetime.now() - start_time,
        items_count=nb_discovered_images,
        success_rate=100 * (nb_described_images / nb_discovered_images) if nb_discovered_images > 0 else 0
    )
    logger.debug(f"\n{output.format()}")


def replace_image_descriptions(markdown_text: str, images: list[dict]) -> str:
    """Replace image placeholders in markdown with generated descriptions."""
    for image in images:
        pattern = r'!\[\]\s*\n?\s*\(' + re.escape(image['name']) + r'\)'
        replacement = (
            f'<!-- START - DESC IMG : {image["name"]} -->\n'
            f'{image.get("description", "Description non générée")}\n'
            f'<!-- END - DESC IMG : {image["name"]} -->'
        )
        markdown_text = re.sub(pattern, replacement, markdown_text)
    return markdown_text


if __name__ == '__main__':
    main()

