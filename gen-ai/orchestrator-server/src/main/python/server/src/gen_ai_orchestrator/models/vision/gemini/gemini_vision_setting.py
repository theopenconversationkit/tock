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
"""Model for creating GeminiVisionSetting"""

from typing import Literal

from pydantic import Field

from gen_ai_orchestrator.models.vision.vision_provider import VisionProvider
from gen_ai_orchestrator.models.vision.vision_setting import BaseVisionSetting


class GeminiVisionSetting(BaseVisionSetting):
    """A class for Gemini Vision Model Setting"""

    provider: Literal[VisionProvider.GEMINI] = Field(
        description='The Vision Model Provider', examples=[VisionProvider.GEMINI]
    )
    model: str = Field(
        description='The Gemini model you want to use',
        examples=['gemini-1.5-pro-001', 'gemini-1.5-flash-001'],
    )
    project_id: str = Field(
        description='The project ID related to the AI service request',
        examples=['my-project-id'],
    )
    location: str = Field(
        description='The location context for the AI service request',
        examples=['europe-west4'],
        default='europe-west4',
    )
