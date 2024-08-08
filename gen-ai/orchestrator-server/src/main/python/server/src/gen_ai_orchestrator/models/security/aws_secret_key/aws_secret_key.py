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
"""Model for creating AwsSecretKey."""

from typing import Literal

from pydantic import Field

from gen_ai_orchestrator.models.security.secret_key import BaseSecretKey
from gen_ai_orchestrator.models.security.secret_key_type import SecretKeyType


class AwsSecretKey(BaseSecretKey):
    """
    A class for AWS Secret Key.
    Used to store the secret name managed in AWS Secrets Manager.
    """

    type: Literal[SecretKeyType.AWS_SECRETS_MANAGER] = Field(
        description='The Secret Key type.',
        examples=[SecretKeyType.AWS_SECRETS_MANAGER],
        default=SecretKeyType.AWS_SECRETS_MANAGER,
    )
    secret_name: str = Field(
        description='The secret name managed in AWS Secrets Manager.',
        examples=['PROD/App/openaiapi_key'],
        min_length=1,
    )
