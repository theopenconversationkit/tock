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

import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.security.SecretManagerService
import ai.tock.shared.security.genAISecretManagerProvider
import ai.tock.shared.security.key.NamedSecretKey
import ai.tock.shared.security.key.RawSecretKey
import ai.tock.shared.security.key.SecretKey
import mu.KLogger
import mu.KotlinLogging

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
            // If the secret is a raw value, it is recovered as is.
            if(secret is RawSecretKey) return  secret.value

            // Check SecretManagerProvider if it is defined
            if (genAISecretManagerProvider == null) {
                throw IllegalArgumentException("No Gen AI secret manager provider has been defined.")
            }

            // Check whether the SecretManagerProvider supports secret
            if(secretMangerService.isSecretTypeSupported(secret)) {
                return secretMangerService.getAIProviderSecret((secret as NamedSecretKey).secretName).secret
            }else{
                throw IllegalArgumentException("The secret manager provider type '${secret.type}' is not supported by " +
                        "the instantiated service ${secretMangerService::class.simpleName}.")
            }
        } catch (e: Exception) {
            logger.warn("The secret has not been recovered.", e)
            // If this fails, return an empty string as the secret value, so as not to block processing.
            return ""
        }
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
        genAISecretManagerProvider?.let {
            secretMangerService.createOrUpdateSecretKey(namespace, botId, feature, secretValue)
        } ?: RawSecretKey(secretValue)

    /**
     * Delete a secret
     * @param secret the secret key
     */
    fun deleteSecret(secret: SecretKey) {
        try {
            // If the secret is a raw value, there's nothing to be done
            if(secret is RawSecretKey) return

            // Check SecretManagerProvider if it is defined
            if (genAISecretManagerProvider == null) {
                throw IllegalArgumentException("No Gen AI secret manager provider has been defined to delete the secret. Type=${secret.type}")
            }

            // Check whether the SecretManagerProvider supports secret
            if(secretMangerService.isSecretTypeSupported(secret)) {
                return secretMangerService.deleteSecret((secret as NamedSecretKey).secretName)
            }else{
                throw IllegalArgumentException("The secret manager provider type '${secret.type}' is not supported by " +
                        "the instantiated service ${secretMangerService::class.simpleName}.")
            }
        } catch (e: Exception) {
            logger.warn("The secret has not been deleted.", e)
            // Do not block treatment if it fails.
        }
    }

}