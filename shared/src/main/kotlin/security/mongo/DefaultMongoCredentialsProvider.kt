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

package ai.tock.shared.security.mongo

import ai.tock.shared.injector
import ai.tock.shared.property
import ai.tock.shared.propertyOrNull
import ai.tock.shared.provide
import ai.tock.shared.security.SecretManagerService
import com.mongodb.MongoCredential

// The expected values correspond to the names of the SecretManagerProviderType elements
val databaseMongoDbSecretManagerProvider: String? = propertyOrNull(
    name = "tock_database_mongodb_secret_manager_provider"
)
val databaseMongoDbCredentialsSecretName: String = property(
    name = "tock_database_mongodb_credentials_secret_name",
    defaultValue = "database_mongodb_credentials",
)
const val defaultDatabase = "admin"

/**
 * Default Mongo credential provider with no authentication
 */
internal object DefaultMongoCredentialsProvider : MongoCredentialsProvider {
    /**
     * The Secrets Manager Service
     */
    private val secretMangerService: SecretManagerService by lazy {
        injector.provide(tag = databaseMongoDbSecretManagerProvider)
    }

    override fun getCredentials(): MongoCredential? =
        if (databaseMongoDbSecretManagerProvider != null) {
            val credentials = secretMangerService.getCredentials(databaseMongoDbCredentialsSecretName)
            MongoCredential.createCredential(
                credentials.username,
                defaultDatabase,
                credentials.password.toCharArray()
            )
        } else null
}
