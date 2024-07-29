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

package ai.tock.gcp.secretmanager.dao

import ai.tock.gcp.EnvConfig
import ai.tock.gcp.GCP_SECRET_VERSION
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.api.gax.rpc.NotFoundException
import com.google.cloud.secretmanager.v1.*
import com.google.protobuf.ByteString
import mu.KLogger
import mu.KotlinLogging


/**
 * Retrieve secret from GCP Secret Manager
 */
class SecretGCPDAO : SecretDAO {

    private val client: SecretManagerServiceClient = SecretManagerServiceClient.create()
    private val logger: KLogger = KotlinLogging.logger { }

    // Get an existing secret.
    override fun getSecret(secretId: String): String  {
        // Access the secret version.
        val secretVersionName: SecretVersionName = SecretVersionName.of(EnvConfig.gcpProjectId, secretId, GCP_SECRET_VERSION)
        val response: AccessSecretVersionResponse = client.accessSecretVersion(secretVersionName)
        logger.debug { "GCP Secret Manager - The secret '$secretVersionName' has been fetched." }

        return response.payload.toString()
    }


    override fun createOrUpdateSecret(secretId: String, secretObject: Any): String {
        // Create secret if it does not exist
        createSecret(secretId)

        val secretValue = jacksonObjectMapper().writeValueAsString(secretObject)
        // Create the secret payload.
        val payload: SecretPayload =
            SecretPayload.newBuilder()
                .setData(ByteString.copyFrom(secretValue.toByteArray()))
                .build()

        // Add the secret version.
        val secretName: SecretName = SecretName.of(EnvConfig.gcpProjectId, secretId)
        val version: SecretVersion = client.addSecretVersion(secretName, payload)
        logger.info { "A new secret value/version is pushed successfully for '$secretName'" }

        return version.name
    }

    private fun createSecret(secretId: String) {
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

        try {
            // Try to access the secret to check if it exists
            val secretName: SecretName = SecretName.of(EnvConfig.gcpProjectId, secretId)
            client.getSecret(secretName)
            logger.info { "Secret already exists: ${secretName.secret}" }
        } catch (e: NotFoundException) {
            // If the secret does not exist, create it
            val projectName: ProjectName = ProjectName.of(EnvConfig.gcpProjectId)
            val createdSecret = client.createSecret(projectName, secretId, secretToCreate)
            logger.info { "Created secret: ${createdSecret.name}" }
        }
    }

}