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
"""Model for creating GeminiVisionFactory"""
from langchain_google_vertexai.chat_models import ChatVertexAI

from gen_ai_orchestrator.models.vision.gemini.gemini_vision_setting import (
    GeminiVisionSetting,
)
from gen_ai_orchestrator.services.langchain.factories.vision.vision_factory import (
    VisionFactory,
)
from gen_ai_orchestrator.services.vision.gemini_vision import GeminiVision


class GeminiVisionFactory(VisionFactory):
    """A class for Gemini Vision Factory"""

    setting: GeminiVisionSetting

    def get_vision_model(self):
        return ChatVertexAI(
            model=self.setting.model,
            project_id=self.setting.project_id,
            location=self.setting.location,
            temperature=self.setting.temperature,
        )
