/*
 * Copyright (C) 2017/2019 VSCT
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

package fr.vsct.tock.bot.api.service

import fr.vsct.tock.bot.api.model.BotResponse
import fr.vsct.tock.bot.api.model.UserRequest
import fr.vsct.tock.shared.addJacksonConverter
import fr.vsct.tock.shared.create
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.retrofitBuilderWithTimeoutAndLogger
import mu.KotlinLogging

internal class BotApiClient(baseUrl: String) {

    private val timeoutInSeconds = longProperty("tock_bot_api_timeout_in_ms", 5000L)
    private val logger = KotlinLogging.logger {}

    private val service: BotApiService

    init {
        service = retrofitBuilderWithTimeoutAndLogger(timeoutInSeconds, logger)
            .addJacksonConverter()
            .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
            .build()
            .create()
    }

    fun send(request: UserRequest): BotResponse? =
        service.send(request).execute().body()
}