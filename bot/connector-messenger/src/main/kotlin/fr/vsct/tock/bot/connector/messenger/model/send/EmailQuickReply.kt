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

package fr.vsct.tock.bot.connector.messenger.model.send

class EmailQuickReply : QuickReply(QuickReplyContentType.user_email) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is QuickReply) return false

        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        return contentType.hashCode()
    }
}