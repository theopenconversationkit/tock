/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.bot.connector.web

import ai.tock.bot.connector.web.channel.ChannelDAO
import ai.tock.bot.connector.web.channel.ChannelMongoDAO
import ai.tock.bot.connector.web.security.WebSecurityCookiesHandler
import ai.tock.bot.connector.web.security.WebSecurityPassthroughHandler
import ai.tock.shared.security.auth.spi.WebSecurityHandler
import ai.tock.shared.security.auth.spi.WebSecurityMode
import ai.tock.shared.service.BotAdditionalModulesService
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton

val webConnectorModule = Kodein.Module {
    bind<ChannelDAO>() with singleton { ChannelMongoDAO }
    bind<WebSecurityHandler>(tag = WebSecurityMode.COOKIES.name) with singleton { WebSecurityCookiesHandler() }
    bind<WebSecurityHandler>(tag = WebSecurityMode.PASSTHROUGH.name) with singleton { WebSecurityPassthroughHandler() }
}

// used in file META-INF/services/ai.tock.shared.service.BotAdditionalModulesService
class IOCModulesService : BotAdditionalModulesService {
    override fun defaultModules() = setOf(webConnectorModule)
}
