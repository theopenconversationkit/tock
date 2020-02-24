/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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

package ai.tock.bot.connector.messenger.model.send

import ai.tock.bot.engine.message.GenericMessage
import ai.tock.shared.security.StringObfuscatorMode
import ai.tock.shared.security.TockObfuscatorService.obfuscate

/**
 * See [https://developers.facebook.com/docs/messenger-platform/send-messages/template/button]
 */
data class ButtonPayload(val text: String, val buttons: List<Button>) : ModelPayload(PayloadType.button) {

    override fun toGenericMessage(): GenericMessage? {
        return GenericMessage(
                texts = mapOf(ButtonPayload::text.name to text),
                choices = buttons.map { it.toChoice() }
        )
    }

    override fun obfuscate(mode: StringObfuscatorMode): Payload {
        return ButtonPayload(obfuscate(text, mode)!!, buttons)
    }
}