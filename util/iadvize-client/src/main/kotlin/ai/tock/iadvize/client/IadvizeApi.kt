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

package ai.tock.iadvize.client


import ai.tock.iadvize.client.authentication.models.AuthResponse
import ai.tock.iadvize.client.graphql.models.GraphQLResponse
import ai.tock.iadvize.client.graphql.models.customData.CustomDataResult
import ai.tock.iadvize.client.graphql.models.routingrule.RoutingRuleResult
import ai.tock.iadvize.client.graphql.models.sendproactivemessage.SendProactiveMessageResult
import mu.KLogger
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST
import java.net.InetSocketAddress
import java.net.Proxy


/**
 * Iadvize API.
 * client implementation used to make REST and GRAPHQL calls to Iadvize apis.
 *
 * @author Henri-Joel SEDJAME
 */
interface IadvizeApi {
    @FormUrlEncoded
    @POST(TOKEN_ENDPOINT)
    fun createToken(@Field(USERNAME) username: String,
                    @Field(PASSWORD) password: String,
                    @Field(GRANT_TYPE) grantType: String
    ): Call<AuthResponse>

    @POST(GRAPHQL_ENDPOINT)
    fun checkAvailability(@Body body: RequestBody) : Call<GraphQLResponse<RoutingRuleResult>>

    @POST(GRAPHQL_ENDPOINT)
    fun getCustomData(@Body body: RequestBody) : Call<GraphQLResponse<CustomDataResult>>

    @POST(GRAPHQL_ENDPOINT)
    @Headers("Accept: application/vnd.iadvize.automation-chatbot-conversation-preview+json")
    fun sendProactiveMessage(@Body body: RequestBody) : Call<GraphQLResponse<SendProactiveMessageResult>>

}


private const val TIMEOUT = 30000L

var proxy = ProxyConfiguration.configure(
    property(IADVIZE_PROXY_HOST),
    intProperty(IADVIZE_PROXY_PORT),
)

/**
 * Create a new Iadvize api client.
 *
 * @param logger the logger
 * @return the new client
 */
fun createApi(logger: KLogger): IadvizeApi = retrofitBuilderWithTimeoutAndLogger(TIMEOUT, logger
        ,proxy = proxy)
        .baseUrl(BASE_URL)
        .addConverterFactory(JacksonConverterFactory.create())
        .build()
        .create()

/**
 * Create a new Iadvize API client.
 * @param logger the logger
 * @param tokenProvider the token provider
 * @return the new client
 */
fun createSecuredApi(logger: KLogger, tokenProvider: () -> String): IadvizeApi = retrofitBuilderWithTimeoutAndLogger(
    TIMEOUT, logger, interceptors = listOf(tokenAuthenticationInterceptor(tokenProvider)),
    proxy = proxy)
    .baseUrl(BASE_URL)
    .addConverterFactory(JacksonConverterFactory.create())
    .build()
    .create()
