#   Copyright (C) 2023-2026 Credit Mutuel Arkea
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
"""Model for creating BaseObservabilitySetting."""

from pydantic import BaseModel, Field

from gen_ai_orchestrator.models.observability.observability_provider import (
    ObservabilityProvider,
)


class BaseObservabilitySetting(BaseModel):
    """A base class for Langfuse Observability Setting."""

    provider: ObservabilityProvider = Field(description='The Observability Provider.')

