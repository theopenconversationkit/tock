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
from langchain_google_vertexai.chat_models import ChatVertexAI
from pdf2image import convert_from_bytes


async def send_images(
    files: Union[list[UploadFile], UploadFile],
    question: str,
    model: str,
    project_id: str,
    location: str,
    temperature: int,
):
    if type(files) == list:  # We are using /gemini/images endpoint
        images_list = await prepare_images(files=files)

    else:  # We are using /gemini/pdf-files endpoint
        images_list = await prepare_images_from_pdf(file=files)

    prompt = f"""
        You are specialized in image analysis !
        Rely on the image(s) and answer the following question in the same language it is written :
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

    ai_message = llm.invoke([message])

    return ai_message.content


async def prepare_images(files: list[UploadFile]):
    images_list = []
    for file in files:
        image_data = await file.read()
        image = base64.b64encode(image_data).decode('utf-8')
        images_list.append(image)

    return images_list


async def prepare_images_from_pdf(file: UploadFile):
    images_list = []
    pdf_content = await file.read()
    images = convert_from_bytes(pdf_file=pdf_content)

    for i, image in enumerate(images):
        img_byte_arr = io.BytesIO()
        image.save(img_byte_arr, format='PNG')
        img_base64 = base64.b64encode(img_byte_arr.getvalue()).decode('utf-8')
        images_list.append(img_base64)

    return images_list
