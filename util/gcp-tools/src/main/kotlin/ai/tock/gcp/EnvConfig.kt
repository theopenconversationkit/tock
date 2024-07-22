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

package ai.tock.gcp

import ai.tock.gcp.utils.property

const val GCP_PROJECT_ID = "gcp_project_id"

const val GCP_SECRET_VERSION = "gcp_secret_manager_secret_version"
const val GCP_ASSUMED_ROLE_PROPERTY = "gcp_secret_manager_assumed_role_arn"
const val GCP_ASSUMED_ROLE_SESSION_NAME_PROPERTY =  "gcp_secret_manager_assumed_role_session_name"
const val GCP_IADVIZE_CREDENTIALS_SECRET_ID_PROPERTY = "gcp_iadvize_credentials_secret_id"

object EnvConfig {

    val gcpProjectId: String = property(GCP_PROJECT_ID)

    /**
     * IAdvize credentials GCP secret ID.
     */
    val gcpIAdvizeCredentialsSecretId: String = GCP_IADVIZE_CREDENTIALS_SECRET_ID_PROPERTY.let { property(it, "please set $it") }

    /**
     * GCP role name used, assumed when getting / reading secrets.
     */
    val gcpSecretManagerAssumedRole: String = GCP_ASSUMED_ROLE_PROPERTY.let { property(it, "please set $it") }

    /**
     * GCP session name used, assumed when getting / reading secrets.
     */
    val gcpAssumedRoleSessionName: String = GCP_ASSUMED_ROLE_SESSION_NAME_PROPERTY.let { property(it, "please set $it") }

}