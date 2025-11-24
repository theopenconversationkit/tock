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

package ai.tock.shared.security

import ai.tock.shared.property
import ai.tock.shared.propertyOrNull
import ai.tock.shared.security.credentials.AIProviderSecret
import ai.tock.shared.security.credentials.Credentials
import ai.tock.shared.security.key.SecretKey

// The expected values correspond to the names of the SecretManagerProviderType elements
val genAISecretManagerProvider: String? =
    propertyOrNull(
        name = "tock_gen_ai_secret_manager_provider",
    )
val genAISecretPrefix: String =
    property(
        name = "tock_gen_ai_secret_prefix",
        defaultValue = "LOCAL/TOCK",
    )

/**
 * The Secret Manager Service
 */
interface SecretManagerService {
    val type: SecretManagerProviderType

    /**
     * Retrieve credentials
     * @param secretName the secret name
     * @return the [Credentials]
     */
    fun getCredentials(secretName: String): Credentials

    /**
     * Retrieve AI Provider Secret
     * @param secretName the secret name
     * @return the [AIProviderSecret]
     */
    fun getAIProviderSecret(secretName: String): AIProviderSecret

    /**
     * Create an AI Provider Secret if it doesn't exist. Else, update it
     * @param secretName the secret name
     * @param secretValue the secret value to store
     */
    fun createOrUpdateAIProviderSecret(
        secretName: String,
        secretValue: AIProviderSecret,
    )

    /**
     * Generate a Secret Name
     * @param namespace the application namespace
     * @param botId the bot ID (also known as application name)
     * @param feature the feature for which the secret will be created
     * @return the generate secret name
     */
    fun generateSecretName(
        namespace: String,
        botId: String,
        feature: String,
    ): String

    /**
     * Create a [SecretKey] instance.
     * @param secretName the secret name
     */
    fun createSecretKeyInstance(secretName: String): SecretKey

    /**
     * Create or update a SecretKey
     * @param namespace the application namespace
     * @param botId the bot ID (also known as application name)
     * @param feature the feature for which the secret will be created
     * @param secretValue the secret value to store
     * @return [SecretKey]
     */
    fun createOrUpdateSecretKey(
        namespace: String,
        botId: String,
        feature: String,
        secretValue: String,
    ): SecretKey {
        // Generate a unique name for the secret based on namespace, botId, and feature
        val secretName = generateSecretName(namespace, botId, feature)
        // Create or update the secret using
        createOrUpdateAIProviderSecret(secretName, AIProviderSecret(secretValue))
        // Return a SecretKey with the generated secret name
        return createSecretKeyInstance(secretName)
    }

    /**
     * Check if the SecretType is supported
     * @secret the secret to check
     * @return true if supported. Else no.
     */
    fun isSecretTypeSupported(secret: SecretKey): Boolean

    /**
     * Delete a secret
     */
    fun deleteSecret(secretName: String)
}

enum class SecretManagerProviderType {
    ENV,
    AWS_SECRETS_MANAGER,
    GCP_SECRET_MANAGER,
}
