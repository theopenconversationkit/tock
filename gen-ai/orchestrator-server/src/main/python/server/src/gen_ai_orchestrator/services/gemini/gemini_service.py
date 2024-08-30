#   Copyright (C) 2024 Credit Mutuel Arkea
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
import base64
import io
from typing import Union

from fastapi import UploadFile
from langchain_core.messages import AIMessage, HumanMessage
from langchain_google_vertexai.chat_models import ChatVertexAI
from pdf2image import convert_from_bytes

from gen_ai_orchestrator.models.observability.langfuse.langfuse_setting import (
    LangfuseObservabilitySetting,
)
from gen_ai_orchestrator.models.observability.observability_trace import (
    ObservabilityTrace,
)
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    create_observability_callback_handler,
)


async def send_images(
    files: Union[list[UploadFile], UploadFile],
    question: str,
    model: str,
    project_id: str,
    location: str,
    temperature: int,
    observability_setting,
) -> AIMessage:
    """
    Processes a list of image files or a single PDF file, prepares the images, and sends them
    along with a question to an AI service for analysis.

    Parameters:
    - files (Union[list[UploadFile], UploadFile]): A list of image files or a single PDF file to be processed.
    - question (str): The question to be asked to the AI, relying on the provided images.
    - model (str): The name of the AI model to be used for the request.
    - project_id (str): The project ID related to the AI service request.
    - location (str): The location context for the AI service request.
    - temperature (int): A parameter controlling the randomness of the AI model's response.
    - observability_setting (str, optional): A JSON string representing observability settings for the request.

    Returns:
    - AIMessage: The response from the AI model after processing the images and the question.
    """
    if type(files) == list:  # We are using /gemini/images endpoint
        images_list = await prepare_images(files=files)

    else:  # We are using /gemini/pdf-files endpoint
        images_list = await prepare_images_from_pdf(file=files)

    prompt = f"""
        You are specialized in image analysis !
        Look and search in all the images before answering the question.
        Answer the following question in the same language it is written :
        {question}
        """

    content = [{'type': 'text', 'text': prompt}]

    for i in range(len(images_list)):
        image_format_to_send = {
            'type': 'image_url',
            'image_url': {'url': f'data:image/jpeg;base64,{images_list[i]}'},
        }
        content.append(image_format_to_send)

    message = HumanMessage(content=content)

    llm = ChatVertexAI(
        model_name=model,
        project=project_id,
        location=location,
        temperature=temperature,
        max_output_tokens=None,
        max_retries=6,
        stop=None,
    )

    callback_handlers = []
    if observability_setting is not None:
        callback_handlers.append(
            create_observability_callback_handler(
                observability_setting=observability_setting,
                trace_name=ObservabilityTrace.RAG,
            )
        )

    return llm.invoke(input=[message], config={'callbacks': callback_handlers})


async def prepare_images(files: list[UploadFile]) -> list:
    """
    Reads a list of image files, encodes them in Base64 format, and returns them as a list.

    This function processes a list of uploaded image files by reading their content asynchronously,
    encoding each image into a Base64 string, and storing the encoded images in a list.

    Parameters:
    - files (list[UploadFile]): A list of image files to be read and encoded.

    Returns:
    - list: A list of Base64-encoded strings representing the content of the image files.
    """
    images_list = []
    for file in files:
        image_data = await file.read()
        image = base64.b64encode(image_data).decode('utf-8')
        images_list.append(image)

    return images_list


async def prepare_images_from_pdf(file: UploadFile) -> list:
    """
    Converts a PDF file into a list of images, encodes each image in Base64 format, and returns them.

    This function takes a single PDF file, converts each page of the PDF into an image,
    encodes each image into a Base64 string, and stores the encoded images in a list.

    Parameters:
    - file (UploadFile): A PDF file to be converted into images and encoded.

    Returns:
    - list: A list of Base64-encoded strings, each representing an image of a page from the PDF file.
    """
    images_list = []
    pdf_content = await file.read()
    images = convert_from_bytes(pdf_file=pdf_content)

    for i, image in enumerate(images):
        img_byte_arr = io.BytesIO()
        image.save(img_byte_arr, format='PNG')
        img_base64 = base64.b64encode(img_byte_arr.getvalue()).decode('utf-8')
        images_list.append(img_base64)

    return images_list
