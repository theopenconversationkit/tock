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
"""Model for creating BaseVisionSetting."""

from pydantic import BaseModel, Field

from gen_ai_orchestrator.models.vision.vision_provider import VisionProvider


class BaseVisionSetting(BaseModel):
    """A base class for Vision Model Setting"""

    provider: VisionProvider = Field(
        description='The Vision Model Provider', examples=[VisionProvider.GEMINI]
    )
    temperature: float = Field(description='', examples=[0.7])
