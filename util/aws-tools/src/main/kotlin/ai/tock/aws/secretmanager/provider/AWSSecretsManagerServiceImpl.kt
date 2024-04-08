/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.aws.secretmanager.provider

import ai.tock.aws.model.AIProviderSecret
import ai.tock.aws.secretmanager.dao.SecretDAO
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.security.credentials.Credentials
import kotlinx.serialization.json.Json

/**
 * Implementation of the AWS Secrets Manager Service
 */
class AWSSecretsManagerServiceImpl: AWSSecretsManagerService {
    private val secretDAO: SecretDAO get() = injector.provide()

    override fun getCredentials(secretName: String): Credentials =
        Json.decodeFromString(secretDAO.getSecret(secretName))

    override fun getAIProviderSecret(secretName: String): AIProviderSecret =
        Json.decodeFromString(secretDAO.getSecret(secretName))

    override fun createOrUpdateAIProviderSecret(secretName: String, secretValue: AIProviderSecret): String =
        secretDAO.createOrUpdateSecret(secretName, secretValue)
}