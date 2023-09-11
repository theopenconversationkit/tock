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

package ai.tock.iadvize.client.authentication.credentials

import ai.tock.iadvize.client.IADVIZE_CREDENTIALS_PROVIDER_TYPE
import ai.tock.iadvize.client.IADVIZE_PASSWORD_AUTHENTICATION
import ai.tock.iadvize.client.IADVIZE_USERNAME_AUTHENTICATION
import ai.tock.iadvize.client.property
import java.util.*


interface CredentialsProvider {
    val type : String
    fun getCredentials(): Credentials
}

class EnvCredentialsProvider : CredentialsProvider {
    override val type: String
        get() = "DEFAULT"

    override fun getCredentials(): Credentials {
        val username = property(IADVIZE_USERNAME_AUTHENTICATION)
        val password = property(IADVIZE_PASSWORD_AUTHENTICATION)
        return Credentials(username, password)
    }
}
fun credentialProviderInstance(): CredentialsProvider {
    return ServiceLoader.load(CredentialsProvider::class.java)
        .firstOrNull {
            it.type.equals(
                property(IADVIZE_CREDENTIALS_PROVIDER_TYPE), true)
        } ?: EnvCredentialsProvider()
}