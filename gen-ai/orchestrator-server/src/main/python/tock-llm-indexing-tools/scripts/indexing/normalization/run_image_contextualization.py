"""
TODO MASS : à reécrire
Run an evaluation on LangFuse dataset experiment.
Usage:
        run_image_contextualization.py [-v] --json-config-file=<jcf>
        run_image_contextualization.py -h | --help
        run_image_contextualization.py --version

Options:
    -v          Verbose output
    -h --help   Show this screen
    --version   Show version
"""

import json
import base64
import json
import os
import re
import time
from datetime import datetime

import backoff
from docopt import docopt
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import get_llm_factory
from langchain_core.messages import SystemMessage, HumanMessage
from langchain_core.output_parsers import StrOutputParser
from langchain_core.prompts import ChatPromptTemplate
from markdown_it import MarkdownIt
from openai import RateLimitError

from scripts.common.img_tools import encode_image
from scripts.common.logging_config import configure_logging
from scripts.common.models import StatusWithReason, ActivityStatus
from scripts.indexing.normalization.models import RunImageContextualizationInput, RunImageContextualizationOutput


# load_dotenv()
#
# AZURE_OPENAI_API_KEY = os.getenv("AZURE_OPENAI_API_KEY")
# AZURE_OPENAI_ENDPOINT = os.getenv("AZURE_OPENAI_ENDPOINT")
# AZURE_OPENAI_DEPLOYMENT = os.getenv("AZURE_OPENAI_DEPLOYMENT")
#
# client = AzureOpenAI(
#     api_key=AZURE_OPENAI_API_KEY,
#     api_version="2023-09-01-preview",
#     azure_endpoint=AZURE_OPENAI_ENDPOINT
# )

def extract_images_with_context_from_file(md_file_path):
    with open(md_file_path, "r", encoding="utf-8") as _md_file:
        md = MarkdownIt().parse(_md_file.read())

    current_header = None
    elements = []

    # Process tokens to build elements list
    i = 0
    while i < len(md):
        token = md[i]
        if token.type == "heading_open" and i + 1 < len(md) and md[i + 1].type == "inline":
            current_header = md[i + 1].content.strip()
            elements.append({"type": "heading", "content": current_header})
            i += 3  # Skip heading_open, inline and heading_close
        else:
            if token.type == "inline" and token.children:
                for child in token.children:
                    if child.type == "image":
                        elements.append({
                            "type": "image",
                            "src": child.attrs.get("src", ""),
                            "header": current_header
                        })
                    elif child.type == "text" and child.content.strip():
                        elements.append({"type": "text", "content": child.content.strip()})
            elif token.content.strip() and token.type not in ["paragraph_open", "paragraph_close"]:
                elements.append({"type": "text", "content": token.content.strip()})
            i += 1

    # Collect context for images
    results = []
    for idx, element in enumerate(elements):
        if element["type"] != "image":
            continue

        # Find previous texts
        prev_texts = []
        for prev in reversed(elements[:idx]):
            if prev["type"] == "heading":
                break
            if prev["type"] == "text":
                prev_texts.append(prev["content"])
        prev_texts.reverse()

        # Find next texts
        next_texts = []
        for next_ in elements[idx + 1:]:
            if next_["type"] == "heading":
                break
            if next_["type"] == "text":
                next_texts.append(next_["content"])

        results.append({
            "src": element["src"],
            "prev_header": element["header"],
            "prev_text": " ".join(prev_texts).strip(),
            "next_text": " ".join(next_texts).strip()
        })

    return results


def generate_image_description(llm, base64_image, context):
    system_prompt = """You are an AI assistant specialized in analyzing financial and documentary images for accessibility.
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

    user_prompt = f"""Analyze the CONTENT AND MEANING of this image, considering the following documentary context:

    Header: {context.get('prev_header', 'Non disponible')}
    Text preceding the image: {context.get('prev_text', 'Non disponible')}
    Text following the image: {context.get('next_text', 'Non disponible')}

    IMPORTANT: If this is a financial chart, focus on the information it communicates rather than its appearance.

    If it is a simple company or brand logo, respond "LOGO: [Name]" or "ILLUSTRATION" if purely decorative.

    Do not start your response with "L'image montre/représente/contient..." - go straight to the essential information."""

    @backoff.on_exception(wait_gen=backoff.expo, exception=RateLimitError, max_tries=0)
    def invoke_llm():
        return llm.invoke([
            SystemMessage(content=system_prompt),
            HumanMessage(
                content=[
                    {"type": "text", "text": user_prompt},
                    {"type": "image_url", "image_url": {"url": f"data:image/jpeg;base64,{base64_image}"}}
                ]
            )
        ])

    response = invoke_llm()

    return response.content


def main():
    start_time = datetime.now()
    formatted_datetime = start_time.strftime('%Y-%m-%d_%Hh%Mm%S')
    cli_args = docopt(__doc__, version='Run Image Contextualization 1.0.0')
    logger = configure_logging(cli_args)

    nb_discovered_images: int = 0
    nb_tested_images: int = 0
    markdown_output: str = ""
    try:
        logger.info("Loading input data...")
        input_config = RunImageContextualizationInput.from_json_file(cli_args["--json-config-file"])
        logger.debug(f"\n{input_config.format()}")

        location = f"{input_config.bot.file_location}/{input_config.bot.namespace}-{input_config.bot.bot_id}"
        reference_document_directory_full_path = f"{location}/input/{input_config.reference_document_directory}"
        reference_document_path = f"{reference_document_directory_full_path}/{input_config.reference_document_name}"
        output_path = f"{location}/output/{input_config.reference_document_name}"

        images_with_context = extract_images_with_context_from_file(reference_document_path)
        nb_discovered_images = len(images_with_context)
        images_with_context_filename = f"{output_path}-image-context-{formatted_datetime}.json"

        json_data = json.dumps(images_with_context, indent=4, ensure_ascii=False)
        with open(images_with_context_filename, "w", encoding="utf-8") as f:
            f.write(json_data)

        llm_factory = get_llm_factory(setting=input_config.llm_setting)

        image_descriptions = {}
        for i, image_info in enumerate(images_with_context):
            image_src = image_info.get('src', '')
            logger.info(f"Processing image {i + 1}/{len(images_with_context)}: {image_src}")

            base64_image = encode_image(f'{reference_document_directory_full_path}/{image_src}')

            description = generate_image_description(llm_factory.get_language_model(), base64_image, image_info)

            image_descriptions[os.path.basename(image_src)] = description
            nb_tested_images += 1
        # Save descriptions to JSON file
        desc_json_filename = f"{output_path}-image-desc-context-{formatted_datetime}.json"
        with open(desc_json_filename, "w", encoding="utf-8") as f:
            json.dump(image_descriptions, f, indent=4, ensure_ascii=False)

        markdown_output = f"{output_path}-enriched-{formatted_datetime}.md"

        with open(reference_document_path, "r", encoding="utf-8") as md_file:
            markdown_text = md_file.read()

        # Replace IMG placeholders in Markdown with generated descriptions
        for image_basename, description in image_descriptions.items():
            pattern = r'!\[\]\s*\n?\s*\(' + re.escape(image_basename) + r'\)'
            replacement = (
                f"<!-- START - DESC IMG : {image_basename} -->\n"
                f"{description}\n"
                f"<!-- END - DESC IMG : {image_basename} -->"
            )
            markdown_text = re.sub(pattern, replacement, markdown_text)

        with open(markdown_output, "w", encoding="utf-8") as md_out_file:
            md_out_file.write(markdown_text)

        activity_status = StatusWithReason(status=ActivityStatus.COMPLETED)
    except Exception as e:
        full_exception_name = f"{type(e).__module__}.{type(e).__name__}"
        activity_status = StatusWithReason(status=ActivityStatus.FAILED, status_reason=f"{full_exception_name} : {e}")
        logger.error(e, exc_info=True)


    output = RunImageContextualizationOutput(
        status=activity_status,
        output_filename=os.path.basename(markdown_output) if markdown_output else "",
        duration=datetime.now() - start_time,
        items_count=nb_discovered_images,
        success_rate=100 * (nb_tested_images / nb_discovered_images) if nb_discovered_images > 0 else 0
    )
    logger.debug(f"\n{output.format()}")


if __name__ == '__main__':
    main()

