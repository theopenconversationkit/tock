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

package ai.tock.iadvize.client.graphql

import ai.tock.iadvize.client.*
import ai.tock.iadvize.client.authentication.IadvizeAuthenticationClient
import ai.tock.iadvize.client.graphql.models.GraphQLResponse
import ai.tock.iadvize.client.graphql.models.customData.CustomDataRequest
import ai.tock.iadvize.client.graphql.models.routingrule.RoutingRuleRequest
import ai.tock.iadvize.client.graphql.models.sendproactivemessage.*
import com.expediagroup.graphql.client.serializer.defaultGraphQLSerializer
import com.expediagroup.graphql.client.types.GraphQLClientRequest
import mu.KotlinLogging
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call

/**
 * GraphQL client for iAdvize.
 */
class IadvizeGraphQLClient {

    companion object {
        val logger = KotlinLogging.logger { }
    }

    private val authenticationClient = IadvizeAuthenticationClient()
    internal var iadvizeApi: IadvizeApi = createSecuredApi(logger) { authenticationClient.getAccessToken() }

    /**
     * Check if a rule is available for distribution
     * @param distributionRule the distribution rule identifier
     */
    fun isAvailable(distributionRule: String): Boolean = execute(
        RoutingRuleRequest(RoutingRuleRequest.Variables(distributionRule)),
        { checkAvailability(it) },
        { it.routingRule?.availability?.chat?.isAvailable }
    )


    /**
     * Check if a custom data (key/value) exist for a given conversation
     * @param conversationId the conversation identifier
     * @param customData the looked for custom data key/value pair
     */
    fun isCustomDataExist(conversationId: String, customData: Pair<String, String>): Boolean = this.execute(
        CustomDataRequest(CustomDataRequest.Variables(conversationId)),
        { getCustomData(it) },
        {
            it.visitorConversationCustomData?.customData
                ?.find { data -> data.key == customData.first }
                ?.value == customData.second
        },
        false
    )

    /**
     * Proactively send actions or messages to the visitor
     * https://developers.iadvize.com/documentation/building-your-bot-on-iadvize#proactively-send-messages-to-the-visitor
     * https://graphql.iadvize.dev/mutations/chatbotMessageSend
     * @param conversationId the conversation identifier
     * @param chatBotId the id of your external bot at iAdvize in integer format
     * @param actionOrMessage the action or message to send
     */
    fun sendProactiveActionOrMessage(conversationId: String, chatBotId: Int, actionOrMessage: ChatbotActionOrMessageInput): Boolean = this.execute(
        SendProactiveActionOrMessageRequest(SendProactiveActionOrMessageRequest.Variables(conversationId, chatBotId, actionOrMessage)),
        { sendProactiveMessage(it) },
        {
            it.userErrors == null
        },
        false
    )

    /**
     * Execute a GraphQL request.
     *
     * @param request the request to execute
     * @param apiCall the [IadvizeApi] call linked to the request execution
     * @param responseMapper the function to map the graphql response to the expected type
     * @param defaultResult the default result to return if the mapping result is null
     */
    private fun <T : Any, R> execute(
        request: GraphQLClientRequest<T>,
        apiCall: IadvizeApi.(RequestBody) -> Call<GraphQLResponse<T>>,
        responseMapper: (T) -> R?,
        defaultResult: R? = null
    ): R = serializeRequest(request)
        .let { iadvizeApi.apiCall(it) }
        .execute()
        .let {
            with(it.body()) body@{
                when (this@body) {
                    null -> graphQlDataNotFoundError()
                    else -> {
                        if (this@body.hasErrors()) {
                            graphQlNotSuccessResponseError(
                                errors?.joinToString(",\n ") { err -> err.toString() },
                                it.code()
                            )
                        } else {
                            (responseMapper(this@body.data) ?: defaultResult) ?: graphQlDataNotFoundError()
                        }
                    }
                }
            }
        }

    /**
     * Serialize a [GraphQLClientRequest] using the defaultGraphQLSerializer
     * provided by the [com.expediagroup.graphql.client] library.
     */
    private fun serializeRequest(request: GraphQLClientRequest<*>) =
        defaultGraphQLSerializer().serialize(request).toRequestBody(APPLICATION_JSON.toMediaTypeOrNull())

}
