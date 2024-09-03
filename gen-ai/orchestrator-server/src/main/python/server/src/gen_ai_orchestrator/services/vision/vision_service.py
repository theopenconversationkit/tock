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
from langchain_core.messages import HumanMessage
from pdf2image import convert_from_bytes

from gen_ai_orchestrator.models.llm.llm_types import LLMSetting
from gen_ai_orchestrator.models.observability.observability_trace import (
    ObservabilityTrace,
)
from gen_ai_orchestrator.models.observability.observability_type import (
    ObservabilitySetting,
)
from gen_ai_orchestrator.routers.responses.responses import (
    SentenceGenerationResponse,
)
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_llm_factory,
)


async def ask_model_with_files(
    files: Union[list[UploadFile], UploadFile],
    llm_setting: LLMSetting,
    observability_setting: ObservabilitySetting,
) -> SentenceGenerationResponse:
    """
    Sends a prompt along with uploaded files to a language model for inference.

    This asynchronous function processes one or multiple uploaded files, typically images,
    and sends them along with a text prompt to a language model. The function supports
    optional observability features to track the model's behavior and performance.

    Args:
        files (Union[list[UploadFile], UploadFile]): A single uploaded file or a list of uploaded files
                                                     to be sent to the language model.
        llm_setting (LLMSetting): Settings for the language model.
        observability_setting (ObservabilitySetting): Settings for tracking observability.

    Returns:
        Any: The response from the language model after processing the prompt and the files.
    """
    model = get_llm_factory(setting=llm_setting).get_language_model()

    images_list = await prepare_images(files)

    content = [
        {'type': 'text', 'text': llm_setting.prompt},
        [
            {
                'type': 'image_url',
                'image_url': {'url': f'data:image/jpeg;base64,{image}'},
            }
            for image in images_list
        ],
    ]

    message = HumanMessage(content)

    callback_handlers = []
    if observability_setting is not None:
        from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
            create_observability_callback_handler,
        )

        callback_handlers.append(
            create_observability_callback_handler(
                observability_setting=observability_setting,
                trace_name=ObservabilityTrace.RAG,
            )
        )

    model_response = model.invoke([message], {'callbacks': callback_handlers})

    return SentenceGenerationResponse(sentences=[model_response.content])


async def prepare_images(files: Union[list[UploadFile], UploadFile]) -> list:
    """
    Reads a list of files, encodes them in Base64 format, and returns them as a list.

    This function processes a list of uploaded files by reading their content asynchronously,
    encoding each image into a Base64 string, and storing the encoded images in a list.

    Parameters:
        files (list[UploadFile]): A list of files to be read and encoded.

    Returns:
        list: A list of Base64-encoded strings representing the content of the image files.
    """
    images = []
    for file in files:
        if file.filename.endswith('.pdf'):
            converted_pdf = await convert_pdf_2_image(file)
            images.extend(
                [
                    base64.b64encode(img_bytes).decode('utf-8')
                    for img_bytes in converted_pdf
                ]
            )
        elif file.filename.endswith(('.png', '.jpg', '.jpeg')):
            img_bytes = await file.read()
            images.append(base64.b64encode(img_bytes).decode('utf-8'))
    return images


async def convert_pdf_2_image(file: UploadFile) -> list[bytes]:
    """
    Converts a PDF file into a list of images in PNG format.

    This asynchronous function takes an uploaded PDF file, reads its content,
    and converts each page of the PDF into a PNG image. The images are returned
    as a list of byte arrays, where each byte array represents a PNG image.

    Args:
        file (UploadFile): The PDF file to be converted, uploaded through an API.

    Returns:
        list[bytes]: A list of byte arrays, where each byte array represents a PNG image
                     corresponding to a page of the PDF.
    """
    content = await file.read()
    images: list[bytes] = []
    for image in convert_from_bytes(content):
        img_arr = io.BytesIO()
        image.save(img_arr, format='PNG')
        images.append(img_arr.getvalue())
    return images
