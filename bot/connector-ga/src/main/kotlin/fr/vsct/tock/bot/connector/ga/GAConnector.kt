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

package fr.vsct.tock.bot.connector.ga

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kodein.instance
import com.google.api.client.auth.openidconnect.IdToken
import com.google.api.client.auth.openidconnect.IdTokenVerifier
import com.google.api.client.json.jackson2.JacksonFactory
import fr.vsct.tock.bot.connector.ConnectorBase
import fr.vsct.tock.bot.connector.ConnectorCallback
import fr.vsct.tock.bot.connector.ConnectorCallbackBase
import fr.vsct.tock.bot.connector.ConnectorData
import fr.vsct.tock.bot.connector.ga.model.request.GARequest
import fr.vsct.tock.bot.engine.BotRepository
import fr.vsct.tock.bot.engine.ConnectorController
import fr.vsct.tock.bot.engine.action.Action
import fr.vsct.tock.bot.engine.event.Event
import fr.vsct.tock.bot.engine.event.LoginEvent
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.engine.user.PlayerType
import fr.vsct.tock.bot.engine.user.UserPreferences
import fr.vsct.tock.shared.Executor
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.provide
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import kotlin.LazyThreadSafetyMode.PUBLICATION


/**
 *
 */
class GAConnector internal constructor(
    val applicationId: String,
    val path: String,
    val allowedProjectIds: Set<String>
) : ConnectorBase(GAConnectorProvider.connectorType) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val executor: Executor get() = injector.provide()
    private val verifier: IdTokenVerifier by lazy(PUBLICATION) { IdTokenVerifier.Builder().build() }


    override fun register(controller: ConnectorController) {
        controller.registerServices(path, { router ->
            logger.info("deploy rest google assistant services for root path $path ")

            router.post(path).handler { context ->
                try {
                    if (isValidToken(context)) {
                        executor.executeBlocking {
                            handleRequest(controller, context, context.bodyAsString)
                        }
                    } else {
                        context.fail(400)
                    }
                } catch (e: Throwable) {
                    context.fail(e)
                }
            }
        })
    }

    //internal for tests
    internal fun handleRequest(
        controller: ConnectorController,
        context: RoutingContext,
        body: String
    ) {
        val timerData = BotRepository.requestTimer.start("ga_webhook")
        try {
            logger.debug { "Google Assistant request input : $body" }
            val request: GARequest = mapper.readValue(body)
            val callback = GAConnectorCallback(applicationId, controller, context, request)
            try {
                val event = WebhookActionConverter.toEvent(request, applicationId)
                fun sendRequest() {
                    controller.handle(event, ConnectorData(callback))
                }

                if (request.user.accessToken != null && event !is LoginEvent) {
                    controller.handle(
                        LoginEvent(
                            PlayerId(request.user.userId, PlayerType.user),
                            PlayerId(applicationId, PlayerType.bot),
                            request.user.accessToken,
                            applicationId,
                            true
                        ),
                        ConnectorData(object : ConnectorCallbackBase(applicationId, gaConnectorType) {

                            // send 401 for revoke token and logout google assistant user
                            override fun eventSkipped(event: Event) {
                                context.fail(401)
                            }

                            override fun eventAnswered(event: Event) {
                                sendRequest()
                            }
                        })
                    )
                } else {
                    sendRequest()
                }

            } catch (t: Throwable) {
                BotRepository.requestTimer.throwable(t, timerData)
                callback.sendTechnicalError(t, body, request)
            }
        } catch (t: Throwable) {
            BotRepository.requestTimer.throwable(t, timerData)
            context.fail(t)
        } finally {
            BotRepository.requestTimer.end(timerData)
        }
    }

    private fun isValidToken(context: RoutingContext): Boolean {
        return if (allowedProjectIds.isNotEmpty()) {
            try {
                val jwt = context.request().getHeader("authorization")
                IdToken.parse(JacksonFactory.getDefaultInstance(), jwt).let { token ->
                    verifier.verify(token) && allowedProjectIds.any { token.verifyAudience(listOf(it)) }
                }
            } catch (e: Exception) {
                logger.warn { "invalid signature" }
                false
            }
        } else {
            true
        }
    }


    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        val c = callback as? GAConnectorCallback
        c?.addAction(event, delayInMs)
        if (event is Action) {
            if (event.metadata.lastAnswer) {
                c?.sendResponse()
            }
        } else {
            logger.trace { "unsupported event: $event" }
        }
    }


    override fun loadProfile(callback: ConnectorCallback, userId: PlayerId): UserPreferences? {
        val c = callback as? GAConnectorCallback
        return c?.request?.user?.profile?.run {
            if (givenName != null) {
                UserPreferences(givenName, familyName)
            } else {
                null
            }
        }
    }
}