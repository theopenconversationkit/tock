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
import ai.tock.shared.security.SecretManagerProviderType
import ai.tock.shared.security.SecretManagerService
import com.mongodb.MongoCredential

private const val defaultDatabase = "admin"
val databaseMongoDbSecretManagerProvider: String? = propertyOrNull(
    name = "tock_database_mongodb_secret_manager_provider"
)
val databaseMongoDbCredentialsSecretName: String = property(
    name = "tock_database_mongodb_credentials_secret_name",
    defaultValue = "database_mongodb_credentials",
)

/**
 * Default Mongo credential provider with no authentication
 */
internal object EnvMongoCredentialsProvider : MongoCredentialsProvider {

    /**
     * The Secrets Manager Service
     */
    /**
     * The Secrets Manager Service
     */
    private val secretMangerService: SecretManagerService by lazy {
        injector.provide(tag = SecretManagerProviderType.ENV.name)
    }

    override fun getCredentials(): MongoCredential? {
        val credentials = secretMangerService.getCredentials(databaseMongoDbCredentialsSecretName)
        return MongoCredential.createCredential(credentials.username, defaultDatabase, credentials.password.toCharArray())
    }
}
