
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

package ai.tock.bot.connector.iadvize

import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.iadvize.model.payload.TextPayload
import ai.tock.bot.connector.iadvize.model.response.conversation.Duration
import ai.tock.bot.connector.iadvize.model.response.conversation.QuickReply
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeAwait
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeClose
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeMessage
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeMultipartReply
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeReply
import ai.tock.bot.connector.iadvize.model.response.conversation.reply.IadvizeTransfer
import ai.tock.bot.engine.Bus

// Used by bot api, and story script
val millis: Duration.TimeUnit = Duration.TimeUnit.millis
val seconds: Duration.TimeUnit = Duration.TimeUnit.seconds
val minutes: Duration.TimeUnit = Duration.TimeUnit.minutes

internal const val IADVIZE_CONNECTOR_TYPE_ID = "iadvize"

/**
 * The iAdvize connector type.
 */
internal val iadvizeConnectorType = ConnectorType(IADVIZE_CONNECTOR_TYPE_ID)

/**
 * Create an iadvize message's provider
 * Used by Story script and bot api (external use)
 */
fun <T : Bus<T>> T.withIadvize(messageProvider: () -> Any): T {
    val iadvizeMessageProvider: () -> IadvizeConnectorMessage = {
        val iadvizeReply = messageProvider.invoke()
        when (iadvizeReply) {
            is IadvizeMultipartReply -> IadvizeConnectorMessage(iadvizeReply)
            is IadvizeReply -> IadvizeConnectorMessage(iadvizeReply)
            else -> IadvizeConnectorMessage()
        }
    }
    return withMessage(iadvizeConnectorType, iadvizeMessageProvider)
}

/**
 * Creates an iAdvize multipart replies sentence
 * Used by Story script and bot api (external use)
 */
fun <T : Bus<T>> T.iadvizeMultipartReplies(vararg replies: IadvizeReply): IadvizeMultipartReply = IadvizeMultipartReply(replies.toList())

/**
 * Creates an iAdvize quickreply sentence
 * Used by Story script and bot api (external use)
 */
fun <T : Bus<T>> T.iadvizeQuickReply(title: CharSequence): QuickReply = QuickReply(translate(title).toString())

/**
 * Creates an iAdvize instant transfer
 * Used by Story script and bot api (external use)
 */
fun <T : Bus<T>> T.iadvizeTransfer(): IadvizeTransfer = IadvizeTransfer(0)

/**
 * Creates an iAdvize transfer
 * Used by Story script and bot api (external use)
 */
fun <T : Bus<T>> T.iadvizeTransfer(timeoutSeconds: Long): IadvizeTransfer = IadvizeTransfer(timeoutSeconds)

/**
 * Creates an iAdvize quickreply sentence
 * Used by Story script and bot api (external use)
 */
fun <T : Bus<T>> T.iadvizeTransfer(
    timeout: Long,
    unit: Duration.TimeUnit,
): IadvizeTransfer = IadvizeTransfer(Duration(timeout, unit))

/**
 * Creates an iAdvize message sentence
 * Used by Story script and bot api (external use)
 */
fun <T : Bus<T>> T.iadvizeMessage(title: CharSequence): IadvizeMessage = IadvizeMessage(translate(title).toString())

/**
 * Creates an iAdvize message with quickreplies sentence
 * Used by Story script and bot api (external use)
 */
fun <T : Bus<T>> T.iadvizeMessage(
    title: CharSequence,
    vararg quickReplies: QuickReply,
): IadvizeMessage = IadvizeMessage(TextPayload(translate(title).toString()), quickReplies.toMutableList())

/**
 * Creates an iAdvize await
 * Used by Story script and bot api (external use)
 */
fun <T : Bus<T>> T.iadvizeAwait(
    timeout: Long,
    unit: Duration.TimeUnit,
): IadvizeAwait = IadvizeAwait(Duration(timeout, unit))

/**
 * Creates an iAdvize close dialog
 * Used by Story script and bot api (external use)
 */
fun <T : Bus<T>> T.iadvizeClose(): IadvizeClose = IadvizeClose()
