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

package ai.tock.bot.connector.iadvize.model.response.conversation.reply

import ai.tock.bot.connector.iadvize.model.response.conversation.Duration
import ai.tock.bot.connector.iadvize.model.response.conversation.ReplyType

data class IadvizeTransfer(
    val distributionRule: String?,
    val transferOptions: TransferOptions) : IadvizeReply {
    override val type: ReplyType = ReplyType.transfer

    data class TransferOptions(val timeout: Duration)

    /**
     * When an IadvizeTransfer is created on a story, distribution rule is not known.
     * Distribution rule is added when response is built.
     */
    constructor(timeoutInSeconds: Long)
            : this(null, TransferOptions(Duration(timeoutInSeconds, Duration.TimeUnit.seconds)))

    /**
     * When an IadvizeTransfer is created on a story, distribution rule is not known.
     * Distribution rule is added when response is built.
     */
    constructor(timout: Duration)
            : this(null, TransferOptions(timout))
}