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

package ai.tock.aws.secretmanager

import ai.tock.aws.AWS_ASSUMED_ROLE_PROPERTY
import ai.tock.aws.AWS_SECRET_VERSION
import ai.tock.aws.EnvConfig
import ai.tock.shared.booleanProperty
import ai.tock.shared.property
import ai.tock.shared.security.SecretManagerProviderType
import ai.tock.shared.security.SecretManagerService
import ai.tock.shared.security.credentials.AIProviderSecret
import ai.tock.shared.security.credentials.Credentials
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.amazonaws.services.secretsmanager.model.*
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.serialization.json.Json
import mu.KLogger
import mu.KotlinLogging
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Implementation of the AWS Secret Manager Service
 */
class AwsSecretManagerService : SecretManagerService {
    override val type: SecretManagerProviderType
        get() = SecretManagerProviderType.AWS_SECRET_MANAGER

    private var stsClient = AWSSecurityTokenServiceClientBuilder.standard().build()
    private var secretsManagerClient: AWSSecretsManager
    private val logger: KLogger = KotlinLogging.logger { }
    private val lockOnSecretCache: Lock = ReentrantLock()
    private var secretsCache: Cache<String, String> = Caffeine.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .maximumSize(100)
        .build()

    init {
        secretsManagerClient = initSecretsManagerWithNewCredentials()
    }

    private fun getAWSSecret(secretId: String): String {
        // Lock secret cache resource to avoid concurrent credentials refreshing
        lockOnSecretCache.withLock {
            // Check if secret is stored in cache before calling AWS
            val storedSecretValue = secretsCache.getIfPresent(secretId)
            if (storedSecretValue != null) {
                return storedSecretValue
            }

            // Retrieve secret from AWS Secrets Manager
            val getSecretValueRequest = GetSecretValueRequest()
                .withSecretId(secretId)
                .withVersionStage(property(AWS_SECRET_VERSION, "AWSCURRENT"))
            var response: GetSecretValueResult
            try {
                response = secretsManagerClient.getSecretValue(getSecretValueRequest)
            } catch (e: AWSSecretsManagerException) {
                // If the temporary credentials are no longer valid, generate new ones and call AWS Secrets Manager again
                if (e.errorCode == "ExpiredTokenException") {
                    logger.debug { "Refresh secret cache with new temporary credentials" }
                    secretsManagerClient = initSecretsManagerWithNewCredentials()
                    response = secretsManagerClient.getSecretValue(getSecretValueRequest)
                } else {
                    throw e
                }
            }
            response.secretString.let {
                secretsCache.put(secretId, it)
                return it
            }
        }
    }

    /**
     * Configure access to AWS Secrets Manager with temporary credentials
     */
    private fun initSecretsManagerWithNewCredentials(): AWSSecretsManager {
        if (booleanProperty(AWS_ASSUMED_ROLE_PROPERTY, false)) {
            getTemporaryCredentials().let {
                val awsSessionCredentials =
                    BasicSessionCredentials(it.accessKeyId, it.secretAccessKey, it.sessionToken)
                return AWSSecretsManagerClientBuilder.standard()
                    .withCredentials(AWSStaticCredentialsProvider(awsSessionCredentials)).build()
            }
        } else {
            val proxy = ClientConfiguration();
            proxy.proxyHost = ""
            proxy.proxyPort = 0;
            return AWSSecretsManagerClientBuilder.standard().withClientConfiguration(proxy).build()
            // return AWSSecretsManagerClientBuilder.standard().build()
            // TODO MASS : rollback
        }
    }


    /**
     * Get temporary credentials from STS by assuming a predefined role
     */
    private fun getTemporaryCredentials(): com.amazonaws.services.securitytoken.model.Credentials {
        val request = AssumeRoleRequest()
            .withRoleArn(EnvConfig.awsSecretManagerAssumedRole)
            .withRoleSessionName(EnvConfig.awsAssumedRoleSessionName)
            .withDurationSeconds(900)
        return stsClient.assumeRole(request).credentials
    }

    private fun createOrUpdateAWSSecret(secretName: String, secretObject: Any) {
        val secretValue = jacksonObjectMapper().writeValueAsString(secretObject)

        try {
            // Update the existing secret
            val updateRequest = UpdateSecretRequest()
                .withSecretId(secretName)
                .withSecretString(secretValue)
            secretsManagerClient.updateSecret(updateRequest)
            logger.info { "The secret '$secretName' already exists, so it has been updated with a new value." }
        }catch (exc: ResourceNotFoundException){
            logger.info { "The secret '$secretName' does not yet exist." }
            // Create a new secret
            val createRequest = CreateSecretRequest()
                .withName(secretName)
                .withSecretString(secretValue)
                .withDescription("Created from Tock.")
            secretsManagerClient.createSecret(createRequest)
            logger.info { "The secret '$secretName' has been created with the value." }
        }

    }

    override fun getCredentials(secretName: String): Credentials =
        Json.decodeFromString(getAWSSecret(secretName))

    override fun getAIProviderSecret(secretName: String): AIProviderSecret =
        Json.decodeFromString(getAWSSecret(secretName))

    override fun createOrUpdateAIProviderSecret(secretName: String, secretValue: AIProviderSecret) =
        createOrUpdateAWSSecret(secretName, secretValue)
}
