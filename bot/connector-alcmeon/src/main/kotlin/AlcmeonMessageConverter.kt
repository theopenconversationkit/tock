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
package ai.tock.bot.connector.alcmeon

internal object AlcmeonMessageConverter {
    fun toMessageOut(
        actions: List<DelayedAction>,
        backend: AlcmeonBackend,
        exitReason: String? = null,
        exitDelay: Long = 0L,
    ): AlcmeonConnectorMessageResponse {
        val exit = exitReason?.let { AlcmeonConnectorMessageExit(it, exitDelay.toInt()) }
        return when (backend) {
            AlcmeonBackend.WHATSAPP ->
                AlcmeonConnectorMessageResponse.AlcmeonConnectorMessageWhatsappResponse(
                    messages =
                        actions.mapNotNull { actionWithDelay ->
                            ai.tock.bot.connector.whatsapp.SendActionConverter.toBotMessage(actionWithDelay.action)
                                ?.let { AlcmeonConnectorMessageOut(it, actionWithDelay.delay.toInt()) }
                        },
                    exit = exit,
                )
            AlcmeonBackend.FACEBOOK ->
                AlcmeonConnectorMessageResponse.AlcmeonConnectorMessageFacebookResponse(
                    messages =
                        actions.mapNotNull { actionWithDelay ->
                            ai.tock.bot.connector.messenger.SendActionConverter.toMessageRequest(
                                actionWithDelay.action,
                            )?.let { AlcmeonConnectorMessageOut(it.message, actionWithDelay.delay.toInt()) }
                        },
                    exit = exit,
                )
        }
    }
}
