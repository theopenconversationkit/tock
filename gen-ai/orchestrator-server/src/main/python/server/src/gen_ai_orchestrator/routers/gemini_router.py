#   Copyright (C) 2023-2024 Credit Mutuel Arkea
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
"""Gemini Router Module"""


import json

from fastapi import APIRouter, Depends, File, Form, HTTPException, UploadFile

from gen_ai_orchestrator.models.observability.observability_type import (
    ObservabilitySetting,
)
from gen_ai_orchestrator.services.gemini.gemini_service import send_images

gemini_router = APIRouter(prefix='/gemini', tags=['Gemini Question Answering'])


@gemini_router.post('/images')
async def ask_gemini_with_images(
    files: list[UploadFile],
    question: str = Form(),
    model: str = Form(),
    project_id: str = Form(),
    location: str = Form(),
    temperature: float = Form(None),
    observability_setting: str = Form(None),
) -> str:
    """
    Handles a POST request to submit images and a question to the Gemini service.

    This endpoint accepts multiple image files and a question, which are then sent to
    the Gemini service for processing. The function checks that the uploaded files are
    either in JPEG or PNG format. If the files do not meet these requirements, an
    HTTPException is raised.

    Parameters:
    - files (list[UploadFile]): A list of image files to be uploaded. Only JPEG and PNG formats are allowed.
    - question (str): The question to be sent to the Gemini service, provided via form data.
    - model (str): The model identifier to be used by the Gemini service, provided via form data.
    - project_id (str): The project ID related to the request, provided via form data.
    - location (str): The location context for the request, provided via form data.
    - temperature (float, optional): An optional parameter for controlling the randomness of the model's response, provided via form data.

    Returns:
    - str: The content of the response returned by the Gemini service.

    Raises:
    - HTTPException: If any of the uploaded files are not in the allowed formats (JPEG or PNG).
    """
    for file in files:
        if file.content_type not in ['image/jpeg', 'image/png']:
            raise HTTPException(
                status_code=400,
                detail='Invalid file format. Please upload a JPEG or PNG image',
            )

    if observability_setting is not None:
        json_setting = json.loads(observability_setting)
        final_observability_setting = ObservabilitySetting(**json_setting)

    gemini_response = await send_images(
        files=files,
        question=question,
        model=model,
        project_id=project_id,
        location=location,
        temperature=temperature,
        observability_setting=final_observability_setting,
    )

    return gemini_response.content


@gemini_router.post('/pdf-files')
async def ask_gemini_with_pdf_converted_in_images(
    file: UploadFile = File(),
    question: str = Form(),
    model: str = Form(),
    project_id: str = Form(),
    location: str = Form(),
    temperature: float = Form(None),
    observability_setting: str = Form(None),
) -> str:
    """
    Handles a POST request to submit a PDF file and a question to the Gemini service.

    This endpoint accepts a PDF file and a question, which are then sent to the Gemini
    service. The function checks that the uploaded file is in PDF format. If the file
    does not meet this requirement, an HTTPException is raised.

    Parameters:
    - file (UploadFile): The PDF file to be uploaded and converted into images.
    - question (str): The question to be sent to the Gemini service, provided via form data.
    - model (str): The model identifier to be used by the Gemini service, provided via form data.
    - project_id (str): The project ID related to the request, provided via form data.
    - location (str): The location context for the request, provided via form data.
    - temperature (float, optional): An optional parameter for controlling the randomness of the model's response, provided via form data.

    Returns:
    - str: The content of the response returned by the Gemini service.

    Raises:
    - HTTPException: If the uploaded file is not in PDF format.
    """
    if file.content_type != 'application/pdf':
        raise HTTPException(
            status_code=400,
            detail='Invalid file format. Please upload a PDF file.',
        )

    if observability_setting is not None:
        json_setting = json.loads(observability_setting)
        final_observability_setting = ObservabilitySetting(**json_setting)

    gemini_response = await send_images(
        files=file,
        question=question,
        model=model,
        project_id=project_id,
        location=location,
        temperature=temperature,
        observability_setting=final_observability_setting,
    )

    return gemini_response.content
