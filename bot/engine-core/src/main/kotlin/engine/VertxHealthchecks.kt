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

package ai.tock.bot.engine

import ai.tock.shared.Executor
import ai.tock.shared.error
import ai.tock.shared.injector
import ai.tock.shared.provide
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging

/**
 * The healthcheck handlers of [BotRepository]. They are Vert.x handlers: this object is only loaded
 * when the Vert.x server reads [BotRepository.healthcheckHandler], so that the bot engine runs without Vert.x.
 */
internal object VertxHealthchecks {
    // the messages keep the category of BotRepository, where these handlers used to be declared
    private val logger = KotlinLogging.logger(BotRepository::class.java.name)

    private val executor: Executor get() = injector.provide()

    /**
     * Answers 500 until the bots are installed, then the result of the NLP healthcheck.
     */
    fun defaultHandler(): (RoutingContext) -> Unit =
        {
            if (!BotRepository.botsInstalled) {
                it.response().setStatusCode(500).end()
            } else {
                executor.executeBlocking {
                    it.response().setStatusCode(if (BotRepository.nlpClient.healthcheck()) 200 else 500).end()
                }
            }
        }

    /**
     * Answers 500 until the bots are installed, or when [handler] fails.
     */
    fun wrap(handler: (RoutingContext) -> Unit): (RoutingContext) -> Unit =
        {
            if (!BotRepository.botsInstalled) {
                it.response().setStatusCode(500).end()
            } else {
                try {
                    handler.invoke(it)
                } catch (t: Throwable) {
                    logger.error(t)
                    try {
                        it.response().setStatusCode(500).end()
                    } catch (t: Throwable) {
                        logger.error(t)
                    }
                }
            }
        }
}
