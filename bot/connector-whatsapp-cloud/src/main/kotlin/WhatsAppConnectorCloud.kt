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

package ai.tock.bot.connector.whatsapp.cloud

import ai.tock.bot.connector.ConnectorBase
import ai.tock.bot.connector.ConnectorCallback
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.engine.ConnectorController
import ai.tock.bot.engine.event.Event
import ai.tock.shared.error
import mu.KotlinLogging

class WhatsAppConnectorCloud internal constructor(
    internal val connectorId: String,
    private val applicationId: String,
    private val phoneNumberId: String,
    private val path: String,
    private val appToken: String,
    private val token: String,
    private val verifyToken: String?,
    private val mode: String,
    internal val client: WhatsAppCloudClient,

    ) : ConnectorBase(ConnectorType("whatsapp_cloud")) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }


    override fun register(controller: ConnectorController) {
        controller.registerServices(path) { router ->
            logger.info("deploy rest whatsapp connector cloud services for root path $path ")

            router.get(path).handler { context ->
                try {
                    val queryParams = context.queryParams()
                    val modeHub = queryParams.get("hub.mode")
                    val verifyTokenMeta = queryParams.get("hub.verify_token")
                    val challenge = queryParams.get("hub.challenge")
                    if (modeHub == mode && verifyToken == verifyTokenMeta){
                        logger.info("WEBHOOK_VERIFIED")
                        context.response().setStatusCode(200).end(challenge)
                    }else{
                        context.response().end("Invalid verify token")
                    }

                } catch (e: Throwable) {
                    logger.error(e)
                    context.fail(500)
                }
            }
        }
    }

    override fun send(event: Event, callback: ConnectorCallback, delayInMs: Long) {
        TODO("Not yet implemented")
    }
}