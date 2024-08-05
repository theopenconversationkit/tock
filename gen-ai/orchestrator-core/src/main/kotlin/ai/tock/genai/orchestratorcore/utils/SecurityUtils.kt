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

import ai.tock.aws.model.AIProviderSecret
import ai.tock.aws.secretmanager.provider.AWSSecretsManagerService
import ai.tock.genai.orchestratorcore.models.Constants
import ai.tock.genai.orchestratorcore.models.security.AwsSecretKey
import ai.tock.genai.orchestratorcore.models.security.RawSecretKey
import ai.tock.genai.orchestratorcore.models.security.SecretKey
import ai.tock.shared.injector
import ai.tock.shared.property
import ai.tock.shared.provide

val secretStorageType: String = property("tock_gen_ai_orchestrator_secret_storage_type", Constants.SECRET_KEY_RAW)
val secretStoragePrefix: String = property("tock_gen_ai_orchestrator_secret_storage_prefix_name", "/DEV")

/**
 * The security utilities class
 */
object SecurityUtils {

    /**
     * The AWS Secrets Manager Service
     */
    private val awsSecretsManagerClient: AWSSecretsManagerService get() = injector.provide()

    /**
     * Fetch the secret key value. Raw Value or Aws Secret Value
     * @param secret the secret key
     * @return the secret value as String
     */
    fun fetchSecretKeyValue(secret: SecretKey): String =
        when(secret){
            is RawSecretKey -> secret.value
            is AwsSecretKey -> awsSecretsManagerClient.getAIProviderSecret(secret.secretName).secret
            else -> throw IllegalArgumentException("Unsupported secret key type")
        }

    /**
     * Create a secret key. If secret storage type is Raw, so it creates [RawSecretKey], else if it is AwsSecretsManager then it creates [AwsSecretKey]
     * @param secretValue the secret value
     * @param secretName the secret name
     * @return [SecretKey]
     */
    fun getSecretKey(secretValue: String, secretName: String): SecretKey =
        when(secretStorageType){
            Constants.SECRET_KEY_RAW -> RawSecretKey(secretValue)
            Constants.SECRET_KEY_AWS -> {
                // Create or update the [AIProviderSecret] on AWS Secrets Manager
                awsSecretsManagerClient.createOrUpdateAIProviderSecret(secretName, AIProviderSecret(secretValue))
                // The return the Secret Key
                AwsSecretKey(secretName)
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
    fun generateAwsSecretName(namespace: String, botId: String, feature: String): String
        = normalizeAwsSecretName("$secretStoragePrefix/$namespace/$botId/$feature")

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

}