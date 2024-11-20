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

package ai.tock.gcp.secretmanager

import ai.tock.gcp.EnvConfig
import ai.tock.gcp.GCP_SECRET_VERSION
import ai.tock.shared.security.SecretManagerProviderType
import ai.tock.shared.security.SecretManagerService
import ai.tock.shared.security.credentials.AIProviderSecret
import ai.tock.shared.security.credentials.Credentials
import ai.tock.shared.security.genAISecretPrefix
import ai.tock.shared.security.key.GcpSecretKey
import ai.tock.shared.security.key.SecretKey
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.api.gax.rpc.AlreadyExistsException
import com.google.cloud.secretmanager.v1.*
import com.google.protobuf.ByteString
import kotlinx.serialization.json.Json
import mu.KLogger
import mu.KotlinLogging

/**
 * Implementation of the AWS Secret Manager Service
 */
class GcpSecretManagerService: SecretManagerService {

    override val type: SecretManagerProviderType
        get() = SecretManagerProviderType.GCP_SECRET_MANAGER

    private val client: SecretManagerServiceClient = SecretManagerServiceClient.create()
    private val logger: KLogger = KotlinLogging.logger { }

    // Get an existing secret.
    private fun getGcpSecret(secretId: String): String  {
        // Access the secret version.
        val secretVersionName: SecretVersionName = SecretVersionName.of(EnvConfig.gcpProjectId, secretId, GCP_SECRET_VERSION)
        val response: AccessSecretVersionResponse = client.accessSecretVersion(secretVersionName)
        logger.debug { "GCP Secret Manager - The secret '$secretVersionName' has been fetched." }

        return response.payload.data.toByteArray().decodeToString()
    }


    private fun createOrUpdateGcpSecret(secretId: String, secretObject: Any) {
        // Create secret if it does not exist
        createGcpSecret(secretId)

        val secretValue = jacksonObjectMapper().writeValueAsString(secretObject)
        // Create the secret payload.
        val payload: SecretPayload =
            SecretPayload.newBuilder()
                .setData(ByteString.copyFrom(secretValue.toByteArray()))
                .build()

        // Add the secret version.
        val secretName: SecretName = SecretName.of(EnvConfig.gcpProjectId, secretId)
        client.addSecretVersion(secretName, payload)
        logger.info { "A new secret value/version is pushed successfully for '$secretName'" }
    }

    private fun createGcpSecret(secretId: String) {
        try {
            // Build the secret to create with manual replication
            val secretToCreate: Secret = Secret.newBuilder()
                .setReplication(
                    Replication.newBuilder()
                        .setUserManaged(
                            Replication.UserManaged.newBuilder()
                                .addReplicas(
                                    Replication.UserManaged.Replica.newBuilder()
                                        .setLocation(EnvConfig.gcpRegion)
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build()

            val projectName: ProjectName = ProjectName.of(EnvConfig.gcpProjectId)
            val createdSecret = client.createSecret(projectName, secretId, secretToCreate)
            logger.info { "The secret ${createdSecret.name} is successfully created." }

        } catch (e: AlreadyExistsException) {
            logger.info { "The secret $secretId is already exists." }
        }
    }

    override fun getCredentials(secretName: String): Credentials =
        Json.decodeFromString(getGcpSecret(secretName))

    override fun getAIProviderSecret(secretName: String): AIProviderSecret =
        Json.decodeFromString(getGcpSecret(secretName))

    override fun createOrUpdateAIProviderSecret(secretName: String, secretValue: AIProviderSecret) =
        createOrUpdateGcpSecret(secretName, secretValue)

    override fun generateSecretName(namespace: String, botId: String, feature: String): String {
        // Define allowed characters
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789/_+=.@-"

        val secretPath = "$genAISecretPrefix/$namespace/$botId/$feature"

        // Replace underscores and space with hyphens
        val normalized = secretPath.trim().replace('/', '-').replace(' ', '-')

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

    override fun createSecretKeyInstance(secretName: String) = GcpSecretKey(secretName)

    override fun isSecretTypeSupported(secret: SecretKey): Boolean = secret is GcpSecretKey

    override fun deleteSecret(secretName: String) {
        try {
            client.deleteSecret(SecretName.of(EnvConfig.gcpProjectId, secretName))
            logger.info { "The secret '$secretName' has been successfully deleted." }
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete the secret '$secretName'." }
            throw e
        }
    }
}
