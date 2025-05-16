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

package ai.tock.bot.connector.businesschat.model.common

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * https://developer.apple.com/documentation/businesschatapi/messages_received/receiving_messages_from_the_business_chat_service
 */
enum class MessageType {
    text,
    interactive,
    richLink,
    @JsonProperty("pass-thread-control")
    pass_thread_control,
    @JsonProperty("take-thread-control")
    take_thread_control,
    typing_start,
    typing_end,
    close
}
