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

package ai.tock.bot.connector.businesschat.model.input

data class BusinessChatConnectorListPickerMessage(
    override val sourceId: String,
    override val destinationId: String,
    val title: String,
    val subtitle: String?,
    val listDetails: String,
    val multipleSelection: Boolean,
    val items: List<ListPickerItem>,
) : BusinessChatConnectorMessage()

data class ListPickerItem(
    val title: String,
    val subtitle: String? = null,
    val image: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ListPickerItem

        if (title != other.title) return false
        if (subtitle != other.subtitle) return false
        if (image != null) {
            if (other.image == null) return false
            if (!image.contentEquals(other.image)) return false
        } else if (other.image != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + (subtitle?.hashCode() ?: 0)
        result = 31 * result + (image?.contentHashCode() ?: 0)
        return result
    }
}
