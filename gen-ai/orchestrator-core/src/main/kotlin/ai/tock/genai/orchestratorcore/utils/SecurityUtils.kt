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

package ai.tock.genai.orchestratorcore.utils

import ai.tock.genai.orchestratorcore.models.Constants
import ai.tock.genai.orchestratorcore.models.security.AwsSecretKey
import ai.tock.genai.orchestratorcore.models.security.GcpSecretKey
import ai.tock.genai.orchestratorcore.models.security.RawSecretKey
import ai.tock.genai.orchestratorcore.models.security.SecretKey
import ai.tock.shared.injector
import ai.tock.shared.property
import ai.tock.shared.provide
import ai.tock.shared.security.SecretMangerService
import ai.tock.shared.security.SecretManagerProviderType
import ai.tock.shared.security.credentials.AIProviderSecret
import mu.KLogger
import mu.KotlinLogging

val secretStorageType: String = property("tock_gen_ai_orchestrator_secret_storage_type", Constants.SECRET_KEY_GCP) // TODO MASS Constants.SECRET_KEY_RAW
val secretStoragePrefix: String = property("tock_gen_ai_orchestrator_secret_storage_prefix_name", "LOCAL-TOCK") // TODO MASS : DEV-TOCK
val genAICredentialsProvider: String = property(
    "tock_gen_ai_credentials_provider",
    SecretManagerProviderType.GCP_SECRET_MANAGER.name // TODO MASS: ENV
)


/**
 * The security utilities class
 */
object SecurityUtils {

    private val logger: KLogger = KotlinLogging.logger {}

    /**
     * The Secrets Manager Service
     */
    private val secretMangerService: SecretMangerService by lazy {
        injector.provide(tag = genAICredentialsProvider)
    }

    /**
     * Fetch the secret key value.
     * @param secret the secret key
     * @return the secret value as String
     */
    fun fetchSecretKeyValue(secret: SecretKey): String {
        try {
            return when (secret) {
                is RawSecretKey -> secret.value
                is AwsSecretKey -> getAIProviderSecret(SecretManagerProviderType.AWS_SECRET_MANAGER, secret.secretName)
                is GcpSecretKey -> getAIProviderSecret(SecretManagerProviderType.GCP_SECRET_MANAGER, secret.secretName)
                else -> throw IllegalArgumentException("Unsupported secret key type")
            }
        } catch (e: Exception) {
            logger.warn("The secret has not been recovered.", e)
            return ""
        }
    }

    private fun getAIProviderSecret(type: SecretManagerProviderType, secretName: String): String {
        if (type != secretMangerService.type) {
            throw IllegalArgumentException("The secret manager provider type '$type' is is not compatible with the service type ${secretMangerService.type}.")
        }

        return secretMangerService.getAIProviderSecret(secretName).secret
    }

    /**
     * Create a secret key. If secret storage type is Raw, so it creates [RawSecretKey], else if it is AwsSecretsManager then it creates [AwsSecretKey]
     * @param secretValue the secret value
     * @return [SecretKey]
     */
    fun getSecretKey(namespace: String, botId: String, feature: String, secretValue: String): SecretKey =
        when(secretStorageType){
            Constants.SECRET_KEY_RAW -> RawSecretKey(secretValue)
            Constants.SECRET_KEY_AWS -> {
                val secretName = generateAwsSecretName(namespace, botId, feature)
                // Create or update the [AIProviderSecret] on AWS Secrets Manager
                secretMangerService.createOrUpdateAIProviderSecret(secretName, AIProviderSecret(secretValue))
                // The return the Secret Key
                AwsSecretKey(secretName)
            }
            Constants.SECRET_KEY_GCP -> {
                val secretName = generateGcpSecretName(namespace, botId, feature)
                // Create or update the [AIProviderSecret] on GCP Secret Manager
                secretMangerService.createOrUpdateAIProviderSecret(secretName, AIProviderSecret(secretValue))
                // The return the Secret Key
                GcpSecretKey(secretName)
            }
            else -> throw IllegalArgumentException("Unsupported secret key type")
        }

    /**
     * Generate an AWS Secret Name
     * @param namespace the bot namespace
     * @param botId the bot id
     * @param feature the feature for which the secret will be created
     * @return the generate secret name
     */
    private fun generateAwsSecretName(namespace: String, botId: String, feature: String): String
        = normalizeAwsSecretName("$secretStoragePrefix/$namespace/$botId/$feature")

    /**
     * Generate an GCP Secret Name
     * @param namespace the bot namespace
     * @param botId the bot id
     * @param feature the feature for which the secret will be created
     * @return the generate secret name
     */
    private fun generateGcpSecretName(namespace: String, botId: String, feature: String): String
            = normalizeGcpSecretName("$secretStoragePrefix/$namespace/$botId/$feature")

    /**
     * Name standardization
     * @param input the input to be normalized
     */
    private fun normalizeAwsSecretName(input: String): String {
        // Define allowed characters
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789/_+=.@-"

        // Replace underscores and space with hyphens
        var normalized = input.replace('_', '-').replace(' ', '-')

        // Filter the input string to only include allowed characters
        normalized = normalized.filter { it in allowedChars }

        // Ensure the length constraints
        if (normalized.length > 512) {
            normalized = normalized.substring(0, 512)
        }

        // Remove ending hyphen followed by six characters if it exists
        val hyphenSixPattern = Regex("-.{6}$")
        if (normalized.length > 7 && hyphenSixPattern.containsMatchIn(normalized)) {
            normalized = normalized.substring(0, normalized.length - 7)
        }

        // Ensure at least one character
        if (normalized.isEmpty()) {
            throw IllegalArgumentException("Normalized AWS secret name must be at least one character long.")
        }

        return normalized
    }

    private fun normalizeGcpSecretName(input: String): String {
        // Replace underscores and space with hyphens
        val normalized = input.trim().replace('/', '-').replace(' ', '-')

        // Filter authorised characters: letters, numbers, hyphens and underscores
        val filteredInput = normalized.filter { it.isLetterOrDigit() || it == '-' || it == '_' }

        // Limit length to 255 characters
        val normalizedName = if (filteredInput.length > 255) {
            filteredInput.substring(0, 255)
        } else {
            filteredInput
        }

        return normalizedName
    }

}