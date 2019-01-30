/*
 * Copyright (C) 2019 VSCT
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

package fr.vsct.tock.bot.connector.twitter

import fr.vsct.tock.bot.connector.twitter.model.AccessToken
import fr.vsct.tock.bot.connector.twitter.model.RequestToken
import fr.vsct.tock.bot.connector.twitter.model.Webhook
import fr.vsct.tock.bot.connector.twitter.model.outcoming.OutcomingEvent
import fr.vsct.tock.shared.addJacksonConverter
import fr.vsct.tock.shared.create
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.retrofitBuilderWithTimeoutAndLogger
import mu.KotlinLogging
import oauth.signpost.http.HttpParameters
import retrofit2.Call
import retrofit2.Response
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import se.akerfeldt.okhttp.signpost.OkHttpOAuthConsumer
import se.akerfeldt.okhttp.signpost.SigningInterceptor
import java.net.URLDecoder
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.xml.crypto.dsig.SignatureMethod

enum class GrantType(val grantType: String) {
    CLIENT_CREDENTIALS("client_credentials")
}

/**
 * Twitter client
 */
internal class TwitterClient(
        val environment: String,
        val consumerKey: String,
        val consumerSecret: String,
        val token: String? = null,
        val secret: String? = null
) {

    private val BASE_URL = "https://api.twitter.com"

    /**
     * @see https://developer.twitter.com/en/docs/basics/authentication/api-reference/authenticate
     *
     */
    interface OAuthApi {

        @POST("/oauth/request_token")
        fun requestToken(): Call<String>

        @POST("/oauth/access_token")
        fun accessToken(): Call<String>

    }


    /***
     * @see https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/aaa-premium#get-account-activity-all-env-name-subscriptions
     *
     */
    interface AccountActivityApi {

        @POST("/1.1/account_activity/all/{environment}/webhooks.json")
        fun registerWebhook(@Path("environment") environment: String, @Query("url") url: String): Call<Webhook>

        @POST("/1.1/account_activity/all/{environment}/subscriptions.json")
        fun subscribe(@Path("environment") environment: String): Call<Unit>

        @GET("/1.1/account_activity/all/{environment}/subscriptions.json")
        fun subscriptions(@Path("environment") environment: String): Call<Unit>

        @DELETE("/1.1/account_activity/all/{environment}/webhooks/{webhook_id}.json")
        fun unregisterWebhook(@Path("environment") environment: String, @Path("webhook_id") webhookId: String): Call<Unit>

        @GET("/1.1/account_activity/all/{environment}/webhooks.json")
        fun webhooks(@Path("environment") environment: String): Call<List<Webhook>>


    }

    /**
     * @see https://developer.twitter.com/en/docs/direct-messages/sending-and-receiving/api-reference
     *
     */
    interface DirectMessageApi {

        @POST("/1.1/direct_messages/events/new.json")
        fun new(@Body OutcomingEvent: OutcomingEvent): Call<Unit>

        @GET("/1.1/direct_messages/events/show.json")
        fun show(): Call<Unit>

        @GET("/1.1/direct_messages/events/list.json")
        fun events(): Call<Unit>

        @DELETE("/1.1/direct_messages/events/destroy.json")
        fun destroy(): Call<Unit>

    }

    private val logger = KotlinLogging.logger {}

    private var accountActivityApi: AccountActivityApi

    private val directMessageApi: DirectMessageApi

    init {

        val accountActivityApiConsumer = OkHttpOAuthConsumer(consumerKey, consumerSecret)
        if (token != null) {
            accountActivityApiConsumer.setTokenWithSecret(token, secret)
        }

        accountActivityApi = retrofitBuilderWithTimeoutAndLogger(
                longProperty("tock_twitter_request_timeout_ms", 30000),
                logger,
                interceptors = listOf(SigningInterceptor(accountActivityApiConsumer))
        )
                .baseUrl(BASE_URL)
                .addJacksonConverter()
                .build()
                .create()

        directMessageApi = retrofitBuilderWithTimeoutAndLogger(
                longProperty("tock_twitter_request_timeout_ms", 30000),
                logger,
                interceptors = listOf(SigningInterceptor(accountActivityApiConsumer))
        )
                .baseUrl(BASE_URL)
                .addJacksonConverter()
                .build()
                .create()
    }

    private fun Response<*>.logError() {
        val error = message()
        val errorCode = code()
        logger.warn { "Twitter Error : $errorCode $error" }
        val errorBody = errorBody()?.string()
        logger.warn { "Twitter Error body : $errorBody" }
    }

    private fun splitQuery(query: String): Map<String, String> {
        val queryPairs = LinkedHashMap<String, String>()
        val pairs = query.split("&".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        for (pair in pairs) {
            val idx = pair.indexOf("=")
            queryPairs[URLDecoder.decode(pair.substring(0, idx), "UTF-8")] = URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
        }
        return queryPairs
    }

    /**
     * Allows a Consumer application to obtain an OAuth Request Token to request user authorization
     *
     * @return requestToken null if failed
     */
    fun requestToken(): RequestToken? {
        val oAuthApiConsumer = OkHttpOAuthConsumer(consumerKey, consumerSecret)
        val httpParameters = HttpParameters()
        httpParameters.put("oauth_callback", "oob")
        oAuthApiConsumer.setAdditionalParameters(httpParameters)

        val oAuthApi: OAuthApi = retrofitBuilderWithTimeoutAndLogger(
                longProperty("tock_twitter_request_timeout_ms", 30000),
                logger,
                interceptors = listOf(SigningInterceptor(oAuthApiConsumer))
        )
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create()

        return try {
            val response = oAuthApi.requestToken().execute()
            if (response.isSuccessful) {
                response.body()?.let {
                    logger.info { it }
                    val queryPairs = splitQuery(it)
                    RequestToken(
                            queryPairs.get("oauth_token") ?: "",
                            queryPairs.get("oauth_token_secret") ?: "",
                            queryPairs.get("oauth_callback_confirmed")?.toBoolean() ?: false
                    )
                }
            } else {
                response.logError()
                null
            }
        } catch (e: Exception) {
            //log and ignore
            logger.error(e)
            null
        }

    }

    /**
     * Allows a Consumer application to use an OAuth Request Token to request user authorization.
     *
     * @return url for request user authorization
     */
    fun authorizationUrl(requestToken: RequestToken): String {
        return "$BASE_URL/oauth/authorize?oauth_token=${requestToken.oauthToken}"
    }

    /**
     * Allows a Consumer application to use an OAuth Request Token to request user authorization.
     *
     * @return user accessToken null if failed
     */
    fun accessToken(requestToken: RequestToken, oauthVerifier: String): AccessToken? {

        val oAuthApiConsumer = OkHttpOAuthConsumer(consumerKey, consumerSecret)
        oAuthApiConsumer.setTokenWithSecret(requestToken.oauthToken, requestToken.oauthTokenSecret)
        val httpParameters = HttpParameters()
        httpParameters.put("oauth_verifier", oauthVerifier)
        oAuthApiConsumer.setAdditionalParameters(httpParameters)

        val oAuthApi: OAuthApi = retrofitBuilderWithTimeoutAndLogger(
                longProperty("tock_twitter_request_timeout_ms", 30000),
                logger,
                interceptors = listOf(SigningInterceptor(oAuthApiConsumer))
        )
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create()

        return try {
            val response = oAuthApi.accessToken().execute()
            if (response.isSuccessful) {
                response.body()?.let {
                    logger.info { it }
                    val queryPairs = splitQuery(it)
                    AccessToken(
                            queryPairs.get("oauth_token") ?: "",
                            queryPairs.get("oauth_token_secret") ?: "",
                            queryPairs.get("user_id") ?: "",
                            queryPairs.get("screen_name") ?: ""
                    )
                }
            } else {
                response.logError()
                null
            }
        } catch (e: Exception) {
            //log and ignore
            logger.error(e)
            null
        }

    }

    /**
     * Registers a webhook URL for all event types. The URL will be validated via CRC request before saving.
     * In case the validation failed, returns comprehensive error message to the requester.
     *
     * @param webhook endpoint
     *
     * @return webhook or null if registration failed
     *
     */
    fun registerWebhook(url: String): Webhook? {
        return try {
            val response = accountActivityApi.registerWebhook(environment, url).execute()
            if (response.isSuccessful) {
                response.body()?.let {
                    logger.info { it }
                    it
                }
            } else {
                response.logError()
                null
            }
        } catch (e: Exception) {
            //log and ignore
            logger.error(e)
            null
        }

    }

    /**
     * Subscribes the provided application to all events for the provided environment for all message types.
     * After activation, all events for the requesting user will be sent to the application’s webhook via POST request.
     *
     * Subscriptions are limited to a maximum of 15 unique users per application in the free (sandbox) tier.
     *
     * @return true if Subscription success
     */
    fun subscribe(): Boolean {
        return try {
            val response = accountActivityApi.subscribe("develop").execute()
            if (!response.isSuccessful) {
                response.logError()
                false
            } else {
                true
            }
        } catch (e: Exception) {
            //log and ignore
            logger.error(e)
            false
        }
    }

    /**
     * Provides a way to determine if a webhook configuration is subscribed to the provided user’s events.
     *
     * @return If the provided user context has an active subscription with provided application, returns true.
     * false if the user does not have an active subscription.
     *
     */
    fun subscriptions(): Boolean {
        return try {
            val response = accountActivityApi.subscriptions("develop").execute()
            response.code() == 204
        } catch (e: Exception) {
            //log and ignore
            logger.error(e)
            false
        }
    }

    /**
     * Removes the webhook from the provided application's all activities configuration.
     *
     * @param The webhook ID can be accessed by making a call to {@link TwitterClient#webhooks()} method
     *
     * @return true if success else false
     *
     */
    fun unregisterWebhook(webhookId: String): Boolean {
        return try {
            val response = accountActivityApi.unregisterWebhook(environment, webhookId).execute()
            if (!response.isSuccessful) {
                response.logError()
                false
            } else {
                true
            }
        } catch (e: Exception) {
            //log and ignore
            logger.error(e)
            false
        }

    }

    /**
     * Returns all environments, webhook URLs and their statuses for the authenticating app. Currently, only one webhook URL can be registered to each environment.
     * We mark a URL as invalid if it fails the daily validation check. In order to re-enable the URL, call the update endpoint.
     *
     * Alternatively, this endpoint can be used with an environment name to only return webhook URLS for the given environment: GET account_activity/all/:env_name/webhooks (see second example)
     *
     * @return list of webhooks
     */
    fun webhooks(): List<Webhook> {
        return try {
            val response = accountActivityApi.webhooks(environment).execute()
            if (response.isSuccessful) {
                response.body()?.let {
                    logger.info { it }
                    it
                } ?: emptyList()
            } else {
                response.logError()
                emptyList()
            }
        } catch (e: Exception) {
            //log and ignore
            logger.error(e)
            emptyList()
        }

    }

    fun sendDirectMessage(outcomingEvent: OutcomingEvent): Boolean {
        return try {
            val response = directMessageApi.new(outcomingEvent).execute()
            if (!response.isSuccessful) {
                response.logError()
                false
            } else {
                true
            }
        } catch (e: Exception) {
            //log and ignore
            logger.error(e)
            false
        }
    }

    fun b64HmacSHA256(payload: String): String {
        val signingKey = SecretKeySpec(
                consumerSecret.toByteArray(),
                "HmacSHA256"
        )

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(signingKey)

        val bytes = mac.doFinal(payload.toByteArray())

        return String(Base64.getEncoder().encode(bytes))
    }

}