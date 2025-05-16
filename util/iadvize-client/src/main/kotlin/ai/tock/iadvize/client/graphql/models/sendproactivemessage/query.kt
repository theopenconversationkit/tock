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

package ai.tock.iadvize.client.graphql.models.sendproactivemessage

import ai.tock.iadvize.client.graphql.*
import com.expediagroup.graphql.client.types.GraphQLClientRequest
import kotlin.reflect.KClass

class SendProactiveActionOrMessageRequest(override val variables: Variables) : GraphQLClientRequest<SendProactiveMessageResult> {
    override val query: String = SEND_PROACTIVE_ACTION_OR_MESSAGE_MUTATION
    override val operationName: String = SEND_PROACTIVE_MESSAGE_MUTATION_NAME
    override fun responseType(): KClass<SendProactiveMessageResult> = SendProactiveMessageResult::class

    data class Variables(val conversationId: String, val chatBotId: Int, val actionOrMessage: ChatbotActionOrMessageInput)
}
