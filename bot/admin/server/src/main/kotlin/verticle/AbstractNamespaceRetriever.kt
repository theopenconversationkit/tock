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

package ai.tock.bot.admin.verticle

import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.shared.exception.rest.NotFoundException
import ai.tock.shared.security.TockUser
import io.vertx.ext.web.RoutingContext

abstract class AbstractNamespaceRetriever {
    val front = FrontClient

    /**
     * Get the namespace from the context
     * @param context : the vertx routing context
     */
    fun getNamespace(context: RoutingContext): String? = ((context.user() ?: context.session()?.get("tockUser")) as? TockUser)?.namespace

    fun currentContextApp(context: RoutingContext): ApplicationDefinition? {
        val botId = context.pathParam("botId")

        return getNamespace(context)?.let { namespace ->
            front.getApplicationByNamespaceAndName(namespace, botId)
        } ?: throw NotFoundException(404, "Could not find $botId in namespace")
    }
}
