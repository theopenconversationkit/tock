#   Copyright (C) 2025-2026 Credit Mutuel Arkea
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
"""Model for creating AwsBedrockEMFactory"""

from typing import List

from langchain.embeddings.base import Embeddings
from langchain_aws import BedrockEmbeddings

from gen_ai_orchestrator.configurations.environment.settings import (
    application_settings,
)
from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIAuthenticationException,
)
from gen_ai_orchestrator.errors.handlers.aws_bedrock.aws_bedrock_exception_handler import (
    aws_bedrock_exception_handler,
)
from gen_ai_orchestrator.models.em.awsbedrock.aws_bedrock_em_setting import (
    AwsBedrockEMSetting,
)
from gen_ai_orchestrator.models.errors.errors_models import ErrorInfo
from gen_ai_orchestrator.services.langchain.factories.em.em_factory import (
    LangChainEMFactory,
)


class AwsBedrockEMFactory(LangChainEMFactory):
    """A class for LangChain AWS Bedrock Embedding Model Factory"""

    setting: AwsBedrockEMSetting

    def get_embedding_model(self) -> Embeddings:
        profile_name = application_settings.aws_bedrock_credentials_profile_name
        if (
            not profile_name
            and not application_settings.aws_bedrock_credentials_allow_default_profile
        ):
            raise GenAIAuthenticationException(
                ErrorInfo(
                    provider='AwsBedrock',
                    error='MissingCredentialsProfileName',
                    cause=(
                        'The AWS credentials profile name is not configured. '
                        'Set the tock_gen_ai_orchestrator_aws_bedrock_credentials_profile_name '
                        'environment property, or set '
                        'tock_gen_ai_orchestrator_aws_bedrock_credentials_allow_default_profile '
                        'to true to allow falling back to the default AWS credential chain.'
                    ),
                )
            )
        return BedrockEmbeddings(
            model_id=self.setting.model,
            credentials_profile_name=profile_name,
        )

    @aws_bedrock_exception_handler(provider='AwsBedrock')
    async def embed_query(self, text: str) -> List[float]:
        return await super().embed_query(text)
