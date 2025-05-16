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

package ai.tock.bot.connector.businesschat

import ai.tock.bot.connector.businesschat.model.common.ReceivedModel
import ai.tock.bot.engine.event.Event
import okhttp3.Interceptor

internal class DefaultBusinessChatIntegrationService : BusinessChatIntegrationService {

    override val baseUrl: String = "https://mspgw.push.apple.com/v1/"

    override fun parseThreadControl(message: ReceivedModel, connectorId: String): Event? = null

    override fun authInterceptor(): Interceptor? = null

    override fun passControl(sourceId: String, recipient: String) = Unit

    override fun takeControl(sourceId: String, recipient: String) = Unit
}
