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

package ai.tock.bot

import ai.tock.bot.handler.ActionHandler
import ai.tock.bot.handler.ActionHandlersProvider
import ai.tock.iadvize.client.graphql.IadvizeGraphQLClient
import ai.tock.shared.propertyOrNull

const val DATA_KEY = "iadvize_custom_data_key"
const val DATA_VALUE = "iadvize_custom_data_key"

val customDataKey = propertyOrNull(DATA_KEY)
val customDataValue = propertyOrNull(DATA_VALUE)

/**
 * The [IAdvizeHandlersProvider] is a set of developer handlers made available to speed up scenario design
 */
class IAdvizeHandlersProvider(private val iadvizeGraphQLClient: IadvizeGraphQLClient = IadvizeGraphQLClient()) : ActionHandlersProvider {

    override fun getNameSpace() = HandlerNamespace.DEV_TOOLS

    enum class HandlerId {
        CHECK_CLIENT_CONNECTED
    }

    enum class ContextName {
        CONVERSATION_ID,
        CLIENT_CONNECTED,
        CLIENT_DISCONNECTED
    }

    override fun getActionHandlers(): Set<ActionHandler> =
        if (!customDataKey.isNullOrBlank() && !customDataValue.isNullOrBlank()) {
            setOf(createIadvizeHandler())
        }else{
            emptySet()
        }

    private fun createIadvizeHandler(): ActionHandler {
        return createActionHandler(
            id = HandlerId.CHECK_CLIENT_CONNECTED.name,
            description = "Check if the client is connected or not",
            inputContexts = setOf(ContextName.CONVERSATION_ID.name),
            outputContexts = setOf(ContextName.CLIENT_CONNECTED.name, ContextName.CLIENT_DISCONNECTED.name),
            handler = {
                with(mutableMapOf<String, String?>()) {
                    it[ContextName.CONVERSATION_ID.name]?.let { conversationId ->
                        iadvizeGraphQLClient.isCustomDataExist(conversationId, customDataKey!! to customDataValue!!)
                            .let { connected ->
                                if (connected) put(ContextName.CLIENT_CONNECTED.name, null)
                                else put(ContextName.CLIENT_DISCONNECTED.name, null)
                            }
                    }
                    // FIXME : what is expected when no conversation id was provided ?
                    this
                }
            }
        )
    }

}