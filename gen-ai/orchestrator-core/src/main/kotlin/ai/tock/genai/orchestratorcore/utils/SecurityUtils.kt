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

import ai.tock.genai.orchestratorcore.models.security.AwsSecretKey
import ai.tock.genai.orchestratorcore.models.security.GcpSecretKey
import ai.tock.genai.orchestratorcore.models.security.RawSecretKey
import ai.tock.genai.orchestratorcore.models.security.SecretKey
import ai.tock.shared.injector
import ai.tock.shared.property
import ai.tock.shared.propertyOrNull
import ai.tock.shared.provide
import ai.tock.shared.security.SecretManagerService
import ai.tock.shared.security.SecretManagerProviderType
import ai.tock.shared.security.credentials.AIProviderSecret
import mu.KLogger
import mu.KotlinLogging

val genAISecretManagerProvider: String? = propertyOrNull(
    name = "tock_gen_ai_secret_manager_provider"
)
val genAISecretPrefix: String = property(
    name = "tock_gen_ai_secret_prefix",
    defaultValue = "LOCAL/TOCK"
)

/**
 * The security utilities class
 */
object SecurityUtils {

    private val logger: KLogger = KotlinLogging.logger {}

    /**
     * The Secrets Manager Service
     */
    private val secretMangerService: SecretManagerService by lazy {
        injector.provide(tag = genAISecretManagerProvider)
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
                is AwsSecretKey -> getAIProviderSecretValue(SecretManagerProviderType.AWS_SECRET_MANAGER, secret.secretName)
                is GcpSecretKey -> getAIProviderSecretValue(SecretManagerProviderType.GCP_SECRET_MANAGER, secret.secretName)
                else -> throw IllegalArgumentException("Unsupported secret key type")
            }
        } catch (e: Exception) {
            logger.warn("The secret has not been recovered.", e)
            // If this fails, return an empty string as the secret value, so as not to block processing.
            return ""
        }
    }

    /**
     * Get the [AIProviderSecret] value
     */
    private fun getAIProviderSecretValue(type: SecretManagerProviderType, secretName: String): String {
        if (genAISecretManagerProvider == null) {
            throw IllegalArgumentException("No Gen AI secret manager provider has been defined.")
        }
        if (type != secretMangerService.type) {
            throw IllegalArgumentException("The secret manager provider type '$type' is not compatible with " +
                    "the service type ${secretMangerService.type}.")
        }

        return secretMangerService.getAIProviderSecret(secretName).secret
    }

    /**
     * Create a secret key depending on secret manager provider.
     * If no provider has been defined, it creates [RawSecretKey].
     * @param namespace the application namespace
     * @param botId the bot ID (also known as application name)
     * @param feature the feature name
     * @param secretValue the secret value
     * @return [SecretKey]
     */
    fun createSecretKey(namespace: String, botId: String, feature: String, secretValue: String): SecretKey =
        when(genAISecretManagerProvider){
            SecretManagerProviderType.AWS_SECRET_MANAGER.name -> {
                val secretName = generateAwsSecretName(namespace, botId, feature)
                // Create or update the [AIProviderSecret] on AWS Secrets Manager
                secretMangerService.createOrUpdateAIProviderSecret(secretName, AIProviderSecret(secretValue))
                // The return the Secret Key
                AwsSecretKey(secretName)
            }
            SecretManagerProviderType.GCP_SECRET_MANAGER.name -> {
                val secretName = generateGcpSecretName(namespace, botId, feature)
                // Create or update the [AIProviderSecret] on GCP Secret Manager
                secretMangerService.createOrUpdateAIProviderSecret(secretName, AIProviderSecret(secretValue))
                // The return the Secret Key
                GcpSecretKey(secretName)
            }
            else -> RawSecretKey(secretValue)
        }

    /**
     * Generate an AWS Secret Name
     * @param namespace the application namespace
     * @param botId the bot ID (also known as application name)
     * @param feature the feature for which the secret will be created
     * @return the generate secret name
     */
    private fun generateAwsSecretName(namespace: String, botId: String, feature: String): String
        = normalizeAwsSecretName("/$genAISecretPrefix/$namespace/$botId/$feature")

    /**
     * Generate an GCP Secret Name
     * @param namespace the application namespace
     * @param botId the bot ID (also known as application name)
     * @param feature the feature for which the secret will be created
     * @return the generate secret name
     */
    private fun generateGcpSecretName(namespace: String, botId: String, feature: String): String
            = normalizeGcpSecretName("$genAISecretPrefix/$namespace/$botId/$feature")

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

    /**
     * Name standardization
     * @param input the input to be normalized
     */
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