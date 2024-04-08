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
import ai.tock.shared.security.credentials.Credentials

/**
 * The AWS Secrets Manager Service
 */
interface AWSSecretsManagerService {
    /**
     * Get credentials from AWS Secret Manager
     * @param secretName the secret name
     * @return the [Credentials]
     */
    fun getCredentials(secretName: String): Credentials

    /**
     * Get AI Provider Secret from AWS Secret Manager
     * @param secretName the secret name
     * @return the [AIProviderSecret]
     */
    fun getAIProviderSecret(secretName: String): AIProviderSecret

    /**
     * Create an AWS Secret if it doesn't exist. Else, update it
     * @param secretName the secret name
     * @param secretValue the secret value to store
     * @return the ARN of a created or updated AWS Secret.
     */
    fun createOrUpdateAIProviderSecret(secretName: String, secretValue: AIProviderSecret): String
}