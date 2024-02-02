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
import json
import logging

import boto3

logger = logging.getLogger(__name__)


class AWSSecretManager:
    def __init__(self):
        self.client = boto3.client(service_name='secretsmanager')

    def get_credentials(self, secret_name):
        return _parse_to_credentials(self.get_secret(secret_name))

    def get_secret(self, secret_name):
        """
        Retrieve individual secrets from AWS Secrets Manager using the get_secret_value API.
        This function assumes the stack mentioned in the source code README has been successfully deployed.
        This stack includes 7 secrets, all of which have names beginning with "mySecret".

        :param secret_name: The name of the secret fetched.
        :type secret_name: str
        """
        try:
            get_secret_value_response = self.client.get_secret_value(
                SecretId=secret_name
            )
            logging.info('Secret retrieved successfully.')
            return get_secret_value_response['SecretString']
        except self.client.exceptions.ResourceNotFoundException:
            logger.error(f'The requested secret {secret_name} was not found.')
            raise
        except Exception as e:
            logger.error(f'An unknown error occurred: {str(e)}.')
            raise


def _parse_to_credentials(secret_data):
    # Parse the JSON secret data
    try:
        secret_dict = json.loads(secret_data)
        username = secret_dict.get('username')
        password = secret_dict.get('password')
        return username, password
    except json.JSONDecodeError as e:
        logger.error(f'Error parsing secret data: {str(e)}')
        return None, None
