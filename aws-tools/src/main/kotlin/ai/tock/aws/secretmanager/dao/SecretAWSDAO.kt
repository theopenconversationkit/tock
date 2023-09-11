/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.aws.secretmanager.dao

import ai.tock.aws.AWS_ASSUMED_ROLE_PROPERTY
import ai.tock.aws.AWS_SECRET_VERSION
import ai.tock.aws.EnvConfig
import ai.tock.aws.utils.booleanProperty
import ai.tock.aws.utils.property
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder
import com.amazonaws.services.secretsmanager.model.AWSSecretsManagerException
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest
import com.amazonaws.services.securitytoken.model.Credentials
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import mu.KLogger
import mu.KotlinLogging
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Retrieve secret from AWS Secrets Manager
 */
class SecretAWSDAO : SecretDAO {
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

    override fun getSecret(secretId: String): String {
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
        if (booleanProperty(AWS_ASSUMED_ROLE_PROPERTY)) {
            getTemporaryCredentials().let {
                val awsSessionCredentials =
                        BasicSessionCredentials(it.accessKeyId, it.secretAccessKey, it.sessionToken)
                return AWSSecretsManagerClientBuilder.standard()
                        .withCredentials(AWSStaticCredentialsProvider(awsSessionCredentials)).build()
            }
        } else {
            return AWSSecretsManagerClientBuilder.standard().build()
        }
    }


    /**
     * Get temporary credentials from STS by assuming a predefined role
     */
    private fun getTemporaryCredentials(): Credentials {
        val request = AssumeRoleRequest()
                .withRoleArn(EnvConfig.awsSecretManagerAssumedRole)
                .withRoleSessionName(EnvConfig.awsAssumedRoleSessionName)
                .withDurationSeconds(900)
        return stsClient.assumeRole(request).credentials
    }
}