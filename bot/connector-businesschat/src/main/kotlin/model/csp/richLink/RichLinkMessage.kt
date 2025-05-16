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

package ai.tock.bot.connector.businesschat.model.csp.richLink

import ai.tock.bot.connector.businesschat.model.common.MessageType
import ai.tock.bot.connector.businesschat.model.csp.BusinessChatCommonModel

class RichLinkMessage(
    sourceId: String,
    destinationId: String,
    val richLinkData: RichLinkData?
) : BusinessChatCommonModel(sourceId = sourceId, destinationId = destinationId, type = MessageType.richLink)

data class RichLinkData(
    val url: String,
    val title: String,
    val assets: Assets
)

data class Assets(
    val image: Image
)

data class Image(
    val data: ByteArray,
    val mimeType: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Image

        if (!data.contentEquals(other.data)) return false
        if (mimeType != other.mimeType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }
}
