/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.bot.connector

import fr.vsct.tock.shared.mapNotNullValues

data class ConnectorConfiguration(
        val applicationId: String,
        val path: String,
        val type: ConnectorType,
        val parameters: Map<String, String>) {

    constructor(applicationId: String,
                path: String,
                type: ConnectorType,
                applicationName: String,
                baseUrl: String?,
                parameters: Map<String, String> = emptyMap())
            : this(
            applicationId,
            path,
            type,
            parameters + mapNotNullValues(
                    APPLICATION_NAME to applicationName,
                    BASE_URL to baseUrl
            ))

    companion object {
        private const val APPLICATION_NAME: String = "_name"
        private const val BASE_URL: String = "_base_url"
    }

    fun getName(): String = parameters.getOrDefault(APPLICATION_NAME, applicationId)

    fun getBaseUrl(): String = parameters.getOrDefault(BASE_URL, "http://localhost")
}