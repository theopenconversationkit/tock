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

package ai.tock.iadvize.client

import ai.tock.iadvize.client.authentication.credentials.EnvCredentialsProvider
import ai.tock.shared.security.credentials.CredentialsProvider
import com.github.salomonbrys.kodein.*

val implementationType: String = ai.tock.shared.property(
    "tock_implementation_type",
    "GCP"
)

val iAdvizeClientModule = Kodein.Module {
    bind<CredentialsProvider>(tag = "ENV") with singleton { EnvCredentialsProvider() }

    // Main factory which chooses the implementation according to the environment variable
    bind<CredentialsProvider>() with provider {
        when (implementationType) {
            "ENV" -> instance<CredentialsProvider>("ENV")
            "AWS" -> instance<CredentialsProvider>("AWS")
            "GCP" -> instance<CredentialsProvider>("GCP")
            else -> throw IllegalArgumentException("Unknown implementation type: $implementationType")
        }
    }
}