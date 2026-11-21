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

package ai.tock.bot.connector

/**
 * Custom parameter for a [ConnectorTypeConfiguration].
 */
data class ConnectorTypeConfigurationField(
    /**
     * The label displayed in the admin interface.
     */
    val label: String,
    /**
     * The technical key of the parameter.
     */
    val key: String,
    /**
     * Is the parameter is mandatory?
     */
    val mandatory: Boolean = false,
)
