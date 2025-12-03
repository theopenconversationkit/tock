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

package ai.tock.bot.xray.model

import ai.tock.shared.property
import ai.tock.translator.UserInterfaceType
import ai.tock.translator.UserInterfaceType.textAndVoiceAssistant
import ai.tock.translator.UserInterfaceType.textChat
import ai.tock.translator.UserInterfaceType.valueOf
import ai.tock.translator.UserInterfaceType.voiceAssistant

/**
 *
 */
data class XrayPrecondition(val preconditionKey: String, val condition: String?) {
    companion object {
        val textChatPrecondition: String = property("tock_bot_test_precondition_key_text_chat", "")
        val voiceAssistantPrecondition: String = property("tock_bot_test_precondition_key_voice_assistant", "")
        val textAndVoiceAssistantPrecondition: String = property("tock_bot_test_precondition_key_text_and_voice_assistant", "")

        fun getPreconditionForUserInterface(userInterface: UserInterfaceType): String? {
            return when (userInterface) {
                textChat -> textChatPrecondition
                voiceAssistant -> voiceAssistantPrecondition
                textAndVoiceAssistant -> textAndVoiceAssistantPrecondition
            }.run { if (isBlank()) null else this }
        }
    }

    fun supportConf(conf: String): Boolean {
        return condition.isNullOrBlank() || condition.split(",").contains(conf)
    }

    fun findUserInterface(): UserInterfaceType? {
        return if (preconditionKey == "user_interface") valueOf(condition ?: textChat.name) else null
    }
}
