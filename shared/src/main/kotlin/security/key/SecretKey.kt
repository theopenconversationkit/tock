/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

package ai.tock.shared.security.key


import ai.tock.shared.Constants
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = RawSecretKey::class, name = Constants.SECRET_KEY_RAW),
    JsonSubTypes.Type(value = AwsSecretKey::class, name = Constants.SECRET_KEY_AWS),
    JsonSubTypes.Type(value = GcpSecretKey::class, name = Constants.SECRET_KEY_GCP)
)
abstract class SecretKey(
    val type: SecretKeyType,
)

interface NamedSecretKey {
    val secretName: String
}

interface HasSecretKey<T>{
    val secretKey: T
}

