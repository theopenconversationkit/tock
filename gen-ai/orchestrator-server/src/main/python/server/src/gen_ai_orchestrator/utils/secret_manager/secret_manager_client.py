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
"""API Client to consume A Secret Manager API"""

import json
import logging
from abc import ABC, abstractmethod
from typing import Optional, Type, TypeVar

import boto3
from botocore.exceptions import ClientError

from gen_ai_orchestrator.models.security.ai_provider_secret import (
    AIProviderSecret,
)
from gen_ai_orchestrator.models.security.credentials import Credentials

logger = logging.getLogger(__name__)
T = TypeVar('T')


class SecretManagerClient(ABC):
    """A Secret Manager Client."""

    def get_credentials(self, secret_name: str) -> Optional[Credentials]:
        """
        Get a user credentials
        Expected storage format of the secret : {"username":"user","password":"pass"}

        Args:
            secret_name: The Secret Name
        """

        return parse_secret_data(self.get_secret(secret_name), Credentials)


    def get_ai_provider_secret(self, secret_name: str) -> Optional[AIProviderSecret]:
        """
        Get the AI provider secret
        Expected storage format of the secret : {"secret":"op******3"}

        Args:
            secret_name: The Secret Name
        """

        return parse_secret_data(self.get_secret(secret_name), AIProviderSecret)

    @abstractmethod
    def get_secret(self, secret_name: str):
        """
        Retrieve individual secret by name, using the get_secret_value API.
        :param secret_name: The name of the secret to be fetched.
        """
        pass


def parse_secret_data(secret_data, obj_type: Type[T]) -> Optional[T]:
    """
    Parse the JSON secret data to retrieve an object of specified type.
    Args:
        secret_data: A json data
        obj_type: Type of object to return

    Returns:
        The object of specified type.

    Raises:
         JSONDecodeError: otherwise
    """
    try:
        secret_dict = json.loads(secret_data)
        return obj_type(**secret_dict)
    except Exception as e:
        logger.error(f'Error parsing secret data: {str(e)}')
        return None
