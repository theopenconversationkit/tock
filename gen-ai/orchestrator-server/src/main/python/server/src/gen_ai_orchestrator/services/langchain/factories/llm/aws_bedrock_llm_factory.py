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
"""Model for creating AwsBedrockLLMFactory"""

from typing import Optional

from langchain_aws import ChatBedrockConverse
from langchain_core.language_models import BaseLanguageModel
from langchain_core.messages import BaseMessage
from langchain_core.runnables import RunnableConfig, RunnableLambda
from langchain_core.runnables.utils import Input, Output

from gen_ai_orchestrator.configurations.environment.settings import (
    application_settings,
)
from gen_ai_orchestrator.errors.exceptions.exceptions import (
    GenAIAuthenticationException,
    GenAIGuardCheckException,
)
from gen_ai_orchestrator.errors.handlers.aws_bedrock.aws_bedrock_exception_handler import (
    aws_bedrock_exception_handler,
)
from gen_ai_orchestrator.models.errors.errors_models import ErrorInfo
from gen_ai_orchestrator.models.llm.awsbedrock.aws_bedrock_llm_setting import (
    AwsBedrockLLMSetting,
)
from gen_ai_orchestrator.services.langchain.factories.llm.llm_factory import (
    LangChainLLMFactory,
)


def _check_guardrail_intervention(message: BaseMessage) -> BaseMessage:
    """
    Raise if the inline AWS Bedrock guardrail intervened on this Converse call.

    Bedrock Converse surfaces guardrail intervention via `stopReason ==
    'guardrail_intervened'` in the raw response, exposed as-is on
    `message.response_metadata` by langchain-aws (no additional network call needed).

    Args:
        message: The AIMessage returned by ChatBedrockConverse.

    Returns:
        The message unchanged, if the guardrail did not intervene.
    """
    if message.response_metadata.get('stopReason') == 'guardrail_intervened':
        guardrail_trace = (
            message.response_metadata.get('trace', {})
            .get('guardrail', {})
            .get('actionReason')
        )
        cause = guardrail_trace or 'The AWS Bedrock guardrail intervened on the model output.'
        raise GenAIGuardCheckException(
            ErrorInfo(
                provider='AwsBedrock',
                error='GuardrailIntervened',
                cause=cause,
            )
        )
    return message


class AwsBedrockLLMFactory(LangChainLLMFactory):
    """A class for LangChain AWS Bedrock LLM Factory"""

    setting: AwsBedrockLLMSetting

    def get_language_model(self) -> BaseLanguageModel:
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

        guardrail_id = self.setting.guardrail_id
        guardrail_version = self.setting.guardrail_version
        guardrail_config = None
        if guardrail_id and guardrail_version:
            guardrail_config = {
                'guardrailIdentifier': guardrail_id,
                'guardrailVersion': guardrail_version,
                'trace': (
                    'enabled'
                    if self.setting.guardrail_trace
                    else 'disabled'
                ),
            }

        llm = ChatBedrockConverse(
            model=self.setting.model,
            credentials_profile_name=profile_name,
            temperature=self.setting.temperature,
            guardrails=guardrail_config,
        )

        if guardrail_config:
            # Wrap the model so that a guardrail intervention raises consistently
            # with the existing (Bloomz) post-hoc guardrail check, without any
            # additional network round trip.
            return llm | RunnableLambda(_check_guardrail_intervention)

        return llm

    @aws_bedrock_exception_handler(provider='AwsBedrock')
    async def invoke(
        self, _input: Input, config: Optional[RunnableConfig] = None
    ) -> Output:
        return await super().invoke(_input, config)
