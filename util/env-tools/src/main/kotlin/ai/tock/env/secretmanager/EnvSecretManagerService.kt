/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

package ai.tock.env.secretmanager

import ai.tock.shared.property
import ai.tock.shared.security.SecretManagerProviderType
import ai.tock.shared.security.SecretManagerService
import ai.tock.shared.security.credentials.AIProviderSecret
import ai.tock.shared.security.credentials.Credentials
import ai.tock.shared.security.key.SecretKey
import kotlinx.serialization.json.Json

/**
 * Implementation of the Secret Manager Service based on environment variables
 */
class EnvSecretManagerService : SecretManagerService {
    override val type: SecretManagerProviderType
        get() = SecretManagerProviderType.ENV

    override fun getCredentials(secretName: String): Credentials =
        Json.decodeFromString(
            property(
                name = secretName,
                defaultValue = "{\"username\": \"\", \"password\": \"\"}",
            ),
        )

    override fun getAIProviderSecret(secretName: String): AIProviderSecret =
        Json.decodeFromString(
            property(
                name = secretName,
                defaultValue = "{\"secret\": \"\"}",
            ),
        )

    override fun createOrUpdateAIProviderSecret(
        secretName: String,
        secretValue: AIProviderSecret,
    ) = error("Not supported")

    override fun generateSecretName(
        namespace: String,
        botId: String,
        feature: String,
    ) = error("Not supported")

    override fun createSecretKeyInstance(secretName: String) = error("Not supported")

    override fun isSecretTypeSupported(secret: SecretKey) = error("Not supported")

    override fun deleteSecret(secretName: String) = error("Not supported")
}
