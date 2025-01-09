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
"""Module for the Security Service"""
import logging
from typing import Optional

from gen_ai_orchestrator.models.security.aws_secret_key.aws_secret_key import (
    AwsSecretKey,
)
from gen_ai_orchestrator.models.security.gcp_secret_key.gcp_secret_key import (
    GcpSecretKey,
)
from gen_ai_orchestrator.models.security.raw_secret_key.raw_secret_key import (
    RawSecretKey,
)
from gen_ai_orchestrator.models.security.security_types import SecretKey
from gen_ai_orchestrator.utils.aws.aws_secrets_manager_client import (
    AWSSecretsManagerClient,
)
from gen_ai_orchestrator.utils.gcp.gcp_secret_manager_client import (
    GCPSecretManagerClient,
)

logger = logging.getLogger(__name__)


def fetch_secret_key_value(secret_key: SecretKey) -> Optional[str]:
    """
    Fetch the value of the given secret key.

    Args:
        secret_key: The secret key
    """
    secret_value = None
    if isinstance(secret_key, RawSecretKey):
        secret_value = secret_key.secret
    elif isinstance(secret_key, AwsSecretKey):
        # Get secret from AWS Secrets Manager
        aws_secret = AWSSecretsManagerClient().get_ai_provider_secret(secret_key.secret_name)
        if aws_secret is not None:
            secret_value = aws_secret.secret
    elif isinstance(secret_key, GcpSecretKey):
        # Get secret from GCP Secret Manager
        gcp_secret = GCPSecretManagerClient().get_ai_provider_secret(secret_key.secret_name)
        if gcp_secret is not None:
            secret_value = gcp_secret.secret

    return secret_value
