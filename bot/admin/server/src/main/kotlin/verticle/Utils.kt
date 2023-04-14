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

package ai.tock.bot.admin.verticle

import ai.tock.nlp.front.client.FrontClient
import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.monitoring.UserActionLog
import ai.tock.shared.Executor
import ai.tock.shared.error
import ai.tock.shared.exception.ToRestException
import ai.tock.shared.injector
import ai.tock.shared.provide
import ai.tock.shared.vertx.RequestLogger
import ai.tock.shared.vertx.WebVerticle
import io.vertx.ext.web.FileUpload
import io.vertx.ext.web.RoutingContext
import mu.KLogger
import org.litote.kmongo.Id
import org.litote.kmongo.toId

fun <R> measureTimeMillis(logger: KLogger, context: RoutingContext, function: () -> R): R {
    val before = System.currentTimeMillis()
    val result = function()
    logger.debug { "${context.normalizedPath()} took ${System.currentTimeMillis() - before} ms." }
    return result
}


inline fun <T, E: ToRestException> WebVerticle<E>.requestLogger(
    actionType: String,
    noinline dataProvider: (RoutingContext) -> Any? = { null },
    crossinline applicationIdProvider: (RoutingContext, T?) -> Id<ApplicationDefinition>? = { context, _ ->
        context.pathParam("applicationId")?.toId()
    }
): RequestLogger =
    object : RequestLogger {
        override fun log(context: RoutingContext, data: Any?, error: Boolean) {
            try {
                @Suppress("UNCHECKED_CAST")
                val log = UserActionLog(
                    context.organization,
                    applicationIdProvider.invoke(context, data as? T),
                    context.userLogin,
                    actionType,
                    (dataProvider(context) ?: data)?.takeUnless { it is FileUpload },
                    error
                )
                injector.provide<Executor>().executeBlocking { FrontClient.save(log) }
            } catch (e: Exception) {
                logger.error(e)
            }
        }
    }

fun < E: ToRestException> WebVerticle<E>.simpleLogger(
    actionType: String,
    dataProvider: (RoutingContext) -> Any? = { null },
    applicationIdProvider: (RoutingContext, Any?) -> Id<ApplicationDefinition>? = { context, _ ->
        context.pathParam("applicationId")?.toId()
    }

): RequestLogger = this.requestLogger<Any, E>(actionType, dataProvider, applicationIdProvider)