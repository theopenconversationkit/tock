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
"""Model for creating BloomzEMSetting."""

from typing import Literal, Optional

from pydantic import Field

from gen_ai_orchestrator.models.em.em_provider import EMProvider
from gen_ai_orchestrator.models.em.em_setting import BaseEMSetting


class BloomzEMSetting(BaseEMSetting):
    """A class for Bloomz Embedding Model Setting."""

    provider: Literal[EMProvider.BLOOMZ] = Field(
        description='The Embedding Model provider.', examples=[EMProvider.BLOOMZ]
    )
    api_base: str = Field(
        description='The base url of the provider API.', examples=['http://doc.tock.ai']
    )
    pooling: Optional[str] = Field(
        description='Pooling method.',
        default='last',
        examples=['first', 'mean', 'last'],
    )
    space_type: Optional[str] = Field(
        description='The space type used to search vector (eg. `l2` for Bloomz, `cosin` for Ada',
        default='l2',
    )
