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
from pydantic import TypeAdapter

from gen_ai_orchestrator.models.observability.observability_type import (
    ObservabilitySetting,
)
from gen_ai_orchestrator.models.vision.vision_types import VisionSetting
from gen_ai_orchestrator.services.gemini.gemini_service import send_images
from gen_ai_orchestrator.services.vision.vision_service import (
    send_images_to_model,
)

vision_router = APIRouter(prefix='/vision', tags=['Question Answering on documents'])


@vision_router.post('/images')
async def ask_model_with_pdf(
    files: list[UploadFile],
    question: str = Form(),
    temperature: float = Form(),
    vision_setting: str = Form(),
    observability_setting: str = Form(None),
):
    for file in files:
        if file.content_type not in ['image/jpeg', 'image/png']:
            raise HTTPException(
                status_code=400,
                detail='Invalid file format. Please upload a JPEG or PNG image',
            )

    vision_setting = TypeAdapter(VisionSetting).validate_python(
        json.loads(vision_setting)
    )
    if observability_setting:
        observability_setting = TypeAdapter(ObservabilitySetting).validate_python(
            json.loads(observability_setting)
        )

    return await send_images_to_model(
        files=files,
        question=question,
        temperature=temperature,
        vision_setting=vision_setting,
        observability_setting=observability_setting,
    )


@vision_router.post('/pdf-files')
async def ask_model_with_pdf(
    file: UploadFile,
    question: str = Form(),
    temperature: float = Form(),
    vision_setting: str = Form(),
    observability_setting: str = Form(None),
):
    if file.content_type != 'application/pdf':
        raise HTTPException(
            status_code=400,
            detail='Invalid file format. Please upload a PDF file.',
        )

    vision_setting = TypeAdapter(VisionSetting).validate_python(
        json.loads(vision_setting)
    )
    if observability_setting:
        observability_setting = TypeAdapter(ObservabilitySetting).validate_python(
            json.loads(observability_setting)
        )

    return await send_images_to_model(
        files=file,
        question=question,
        temperature=temperature,
        vision_setting=vision_setting,
        observability_setting=observability_setting,
    )
