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
"""Model for creating AwsBedrockCompressorSetting."""

from typing import Literal

from pydantic import Field

from gen_ai_orchestrator.models.document_compressor.document_compressor_provider import (
    DocumentCompressorProvider,
)
from gen_ai_orchestrator.models.document_compressor.document_compressor_setting import (
    BaseDocumentCompressorSetting,
)


class AwsBedrockCompressorSetting(BaseDocumentCompressorSetting):
    """
    A class for AWS Bedrock Compressor (Rerank API) Setting.
    Usage docs: https://docs.aws.amazon.com/bedrock/latest/userguide/rerank.html

    Authentication relies on the default AWS credential chain (environment variables,
    shared credentials/config file, or an IAM role). The AWS profile name, if any, is
    configured once for the whole orchestrator-server via the
    `tock_gen_ai_orchestrator_aws_bedrock_credentials_profile_name` environment property
    (see `application_settings`), and is not part of this per-request setting.
    If that property is not set, requests fail unless the
    `tock_gen_ai_orchestrator_aws_bedrock_credentials_allow_default_profile` environment
    property is set to `true`, in which case the default AWS credential chain (e.g. IRSA,
    instance profile, environment variables) is used instead.
    The AWS region is likewise not configured here: it is resolved from the AWS
    profile/credential chain, consistently with the profile name resolution.
    No explicit access/secret keys are stored.
    """

    provider: Literal[DocumentCompressorProvider.AWS_BEDROCK] = Field(
        description='The document compressor provider.',
        examples=[DocumentCompressorProvider.AWS_BEDROCK],
        default=DocumentCompressorProvider.AWS_BEDROCK.value,
    )
    model_arn: str = Field(
        description='The ARN of the Bedrock rerank model.',
        examples=[
            'arn:aws:bedrock:us-west-2::foundation-model/amazon.rerank-v1:0',
            'arn:aws:bedrock:us-west-2::foundation-model/cohere.rerank-v3-5:0',
        ],
        min_length=1,
    )
