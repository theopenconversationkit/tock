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

package ai.tock.aws

import ai.tock.shared.property

const val AWS_SECRET_VERSION = "aws_secret_manager_secret_version"
const val AWS_ASSUMED_ROLE_PROPERTY = "aws_secret_manager_assumed_role_arn"
const val AWS_ASSUMED_ROLE_SESSION_NAME_PROPERTY = "aws_secret_manager_assumed_role_session_name"

object EnvConfig {
    /**
     * AWS role name used, assumed when getting / reading secrets.
     */
    val awsSecretManagerAssumedRole: String = AWS_ASSUMED_ROLE_PROPERTY.let { property(it, "please set $it") }

    /**
     * AWS session name used, assumed when getting / reading secrets.
     */
    val awsAssumedRoleSessionName: String = AWS_ASSUMED_ROLE_SESSION_NAME_PROPERTY.let { property(it, "please set $it") }
}
