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


from fastapi import APIRouter, Form, HTTPException, UploadFile
from pydantic import TypeAdapter
from vertexai.generative_models._generative_models import GenerationConfig

from gen_ai_orchestrator.services.gemini.gemini_service import (  # send_images_from_pdf
    send_images,
)

gemini_router = APIRouter(prefix='/gemini', tags=['Gemini Question Answering'])


@gemini_router.post('/images')
async def ask_gemini_with_images(
    files: list[UploadFile],
    question: str = Form(),
    model: str = Form(),
    project_id: str = Form(),
    location: str = Form(),
    temperature: float = Form(None),
):
    for file in files:
        if file.content_type not in ['image/jpeg', 'image/png']:
            raise HTTPException(
                status_code=400,
                detail='Invalid file format. Please upload a JPEG or PNG image',
            )

    return await send_images(
        files=files,
        question=question,
        model=model,
        project_id=project_id,
        location=location,
        temperature=temperature,
    )


@gemini_router.post('/pdf-files')
async def ask_gemini_with_pdf_converted_in_images(
    file: UploadFile,
    question: str = Form(),
    model: str = Form(),
    project_id: str = Form(),
    location: str = Form(),
    temperature: float = Form(None),
):
    if file.content_type != 'application/pdf':
        raise HTTPException(
            status_code=400,
            detail='Invalid file format. Please upload a PDF file.',
        )

    return await send_images(
        files=file,
        question=question,
        model=model,
        project_id=project_id,
        location=location,
        temperature=temperature,
    )
