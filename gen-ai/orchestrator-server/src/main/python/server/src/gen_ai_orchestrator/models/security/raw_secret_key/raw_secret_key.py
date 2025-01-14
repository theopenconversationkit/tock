#   Copyright (C) 2023-2025 Credit Mutuel Arkea
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
"""Model for creating RawSecretKey."""

from typing import Literal

from pydantic import Field

from gen_ai_orchestrator.models.security.secret_key import BaseSecretKey
from gen_ai_orchestrator.models.security.secret_key_type import SecretKeyType


class RawSecretKey(BaseSecretKey):
    """
    A class for Raw Secret Key.
    Used to store a secret in its raw form.
    """

    type: Literal[SecretKeyType.RAW] = Field(
        description='The Secret Key type.',
        examples=[SecretKeyType.RAW],
        default=SecretKeyType.RAW,
    )
    secret: str = Field(
        description='The secret value.', examples=['145d-**********-e4f'], min_length=1
    )
