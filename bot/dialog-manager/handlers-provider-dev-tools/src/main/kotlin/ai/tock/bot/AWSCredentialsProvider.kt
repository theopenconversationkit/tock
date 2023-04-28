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

package ai.tock.bot

import ai.tock.aws.secretmanager.provider.IAdvizeCredentialsProvider
import ai.tock.iadvize.client.authentication.credentials.Credentials
import ai.tock.iadvize.client.authentication.credentials.CredentialsProvider
import ai.tock.shared.injector
import ai.tock.shared.provide

class AWSCredentialsProvider : CredentialsProvider {

    private val iAdvizeCredentialsProvider: IAdvizeCredentialsProvider get() = injector.provide()

    override val type: String
        get() = "AWS"

    override fun getCredentials(): Credentials =
        iAdvizeCredentialsProvider.getIAdvizeCredentials().let {
            Credentials(it.username, it.password)
        }
}