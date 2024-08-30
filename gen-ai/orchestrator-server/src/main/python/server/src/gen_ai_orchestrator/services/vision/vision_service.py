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
from typing import Union

from fastapi import Form, UploadFile

from gen_ai_orchestrator.models.observability.observability_type import (
    ObservabilitySetting,
)
from gen_ai_orchestrator.models.vision.vision_types import VisionSetting
from gen_ai_orchestrator.routers.requests.requests import VisionQuery
from gen_ai_orchestrator.services.langchain.factories.langchain_factory import (
    get_vision_factory,
)


async def send_images_to_model(
    files: Union[list[UploadFile], UploadFile],
    question: str,
    temperature: float,
    vision_setting: VisionSetting,
    observability_setting: ObservabilitySetting,
):
    model = get_vision_factory(setting=vision_setting).get_vision_model()

    response = await model.send_images(
        files=files,
        question=question,
        temperature=temperature,
        observability_setting=observability_setting,
    )

    return response.content
