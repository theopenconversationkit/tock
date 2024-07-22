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
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse
import com.google.cloud.secretmanager.v1.ProjectName
import com.google.cloud.secretmanager.v1.Replication
import com.google.cloud.secretmanager.v1.Secret
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretName
import com.google.cloud.secretmanager.v1.SecretPayload
import com.google.cloud.secretmanager.v1.SecretVersion
import com.google.cloud.secretmanager.v1.SecretVersionName
import com.google.protobuf.ByteString
import mu.KLogger
import mu.KotlinLogging
import java.util.zip.CRC32C




/**
 * Retrieve secret from GCP Secret Manager
 */
class SecretGCPDAO : SecretDAO {

    private val client: SecretManagerServiceClient = SecretManagerServiceClient.create()
    private val logger: KLogger = KotlinLogging.logger { }

    // Get an existing secret.
    override fun getSecret(secretId: String): String  {
        // Access the secret version.
        val secretVersionName: SecretVersionName = SecretVersionName.of(EnvConfig.gcpProjectId, secretId, "latest")
        val response: AccessSecretVersionResponse = client.accessSecretVersion(secretVersionName)
        logger.debug { "GCP Secret Manager - The secret '$secretVersionName' has been fetched." }

        return response.payload.toString()
    }


    override fun createOrUpdateSecret(secretId: String, secretObject: Any): String {

        // Create the secret.
        val secretName: SecretName = SecretName.of(EnvConfig.gcpProjectId, secretId)
        val secret: Secret = client.getSecret(secretName)
        logger.info { "GCP Secret Manager - The secret '$secretName' already exists, so it has been updated with a new value." }

        // ----------------------------------------------------------------------------------------------
        // TODO MASS : Si secret n'existe pas
        val projectName: ProjectName = ProjectName.of(EnvConfig.gcpProjectId)
        // Build the secret to create.
        val secretToCreate: Secret =
            Secret.newBuilder()
                .setReplication(
                    Replication.newBuilder()
                        .setAutomatic(Replication.Automatic.newBuilder().build())
                        .build()
                )
                .build()
        // Create the secret.
        val createdSecret: Secret = client.createSecret(projectName, secretId, secretToCreate)
        // ----------------------------------------------------------------------------------------------

        val secretValue = jacksonObjectMapper().writeValueAsString(secretObject)

        // Create the secret payload.
        val payload: SecretPayload =
            SecretPayload.newBuilder()
                .setData(ByteString.copyFrom(secretValue.toByteArray())) // Providing data checksum is optional.
                .build()

        // Add the secret version.
        val version: SecretVersion = client.addSecretVersion(secretName, payload)

        logger.info { "GCP Secret Manager - The secret '$secretName' has been created with the value." }

        return version.name
    }

}