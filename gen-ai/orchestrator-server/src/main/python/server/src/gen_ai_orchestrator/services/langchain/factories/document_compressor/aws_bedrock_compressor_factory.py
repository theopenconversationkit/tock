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

from langchain_core.documents import Document

from gen_ai_orchestrator.configurations.environment.settings import (
    application_settings,
)
from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIAuthenticationException,
)
from gen_ai_orchestrator.models.document_compressor.awsbedrock.aws_bedrock_compressor_setting import (
    AwsBedrockCompressorSetting,
)
from gen_ai_orchestrator.models.errors.errors_models import ErrorInfo
from gen_ai_orchestrator.services.langchain.factories.document_compressor.document_compressor_factory import (
    DocumentCompressorFactory,
)
from gen_ai_orchestrator.services.langchain.impls.document_compressor.aws_bedrock_rerank import (
    AwsBedrockRerank,
)


class AwsBedrockCompressorFactory(DocumentCompressorFactory):
    setting: AwsBedrockCompressorSetting

    def get_compressor(self) -> AwsBedrockRerank:
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

        return AwsBedrockRerank(
            model_arn=self.setting.model_arn,
            credentials_profile_name=profile_name,
            min_score=self.setting.min_score,
            max_documents=self.setting.max_documents,
            fill_to_max_documents=self.setting.fill_to_max_documents,
            is_fault_tolerant=self.is_fault_tolerant,
        )

    def check_document_compressor_setting(self) -> bool:
        self.get_compressor().compress_documents(
            documents=[Document(page_content='Hello, world!')], query='Hi!'
        )

        return True
