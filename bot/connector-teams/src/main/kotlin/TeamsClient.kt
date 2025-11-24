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

package ai.tock.bot.connector.teams

import ai.tock.bot.connector.teams.messages.MarkdownHelper.activeLink
import ai.tock.bot.connector.teams.messages.TeamsBotMessage
import ai.tock.bot.connector.teams.messages.TeamsCardAction
import ai.tock.bot.connector.teams.messages.TeamsCarousel
import ai.tock.bot.connector.teams.messages.TeamsHeroCard
import ai.tock.bot.connector.teams.token.TokenHandler
import ai.tock.shared.addJacksonConverter
import ai.tock.shared.create
import ai.tock.shared.longProperty
import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import com.microsoft.bot.schema.Activity
import com.microsoft.bot.schema.ActivityTypes
import com.microsoft.bot.schema.Attachment
import com.microsoft.bot.schema.AttachmentLayoutTypes
import com.microsoft.bot.schema.HeroCard
import com.microsoft.bot.schema.TextFormatTypes
import com.microsoft.bot.schema.ThumbnailCard
import mu.KotlinLogging
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url

internal class TeamsClient(private val tokenHandler: TokenHandler) {
    private val connectorApi: ConnectorMicrosoftApi
    private val logger = KotlinLogging.logger {}
    private val customInterceptor = CustomInterceptor()

    init {

        connectorApi =
            retrofitBuilderWithTimeoutAndLogger(
                longProperty("tock_whatsapp_request_timeout_ms", 30000),
                logger,
                interceptors = listOf(customInterceptor),
            )
                .baseUrl("https://smba.trafficmanager.net/emea/")
                .addJacksonConverter(tokenHandler.teamsMapper)
                .build()
                .create()
    }

    fun sendMessage(
        callbackActivity: Activity,
        event: TeamsBotMessage,
    ) {
        // construct request
        val url =
            "${callbackActivity.serviceUrl}/v3/conversations/${callbackActivity.conversation.id}/activities/${callbackActivity.id}"

        // construct callbackActivity
        val activity =
            Activity(ActivityTypes.MESSAGE).apply {
                text = activeLink(event.text)
                textFormat = TextFormatTypes.MARKDOWN
                recipient = callbackActivity.from
                attachments = getAttachment(event)
                from = callbackActivity.recipient
                conversation = callbackActivity.conversation
                replyToId = callbackActivity.id
            }

        if (event is TeamsCarousel) {
            activity.attachmentLayout = AttachmentLayoutTypes.CAROUSEL
        }
        // send the message
        val messageResponse =
            connectorApi.postResponse(
                url,
                activity,
            ).execute()
        if (!messageResponse.isSuccessful) {
            logger.warn {
                "Microsoft Login Api Error : ${messageResponse.code()} // ${messageResponse.errorBody()}"
            }
        }
    }

    private fun getAttachment(event: TeamsBotMessage): MutableList<Attachment> {
        val attachments = mutableListOf<Attachment>()

        when (event) {
            is TeamsCardAction -> {
                val card =
                    ThumbnailCard().apply {
                        title = event.actionTitle
                        buttons = event.buttons
                    }
                attachments.add(
                    Attachment().apply {
                        contentType = "application/vnd.microsoft.card.thumbnail"
                        content = card
                    },
                )
            }
            is TeamsCarousel -> {
                val listElement = mutableListOf<TeamsBotMessage>()
                listElement.addAll(event.listMessage)
                while (listElement.isNotEmpty()) {
                    attachments.addAll(
                        getAttachment(
                            listElement.removeAt(0),
                        ),
                    )
                }
            }
            is TeamsHeroCard -> {
                val card =
                    HeroCard().apply {
                        title = event.title
                        subtitle = event.subtitle
                        text = event.attachmentContent
                        images = event.images
                        buttons = event.buttons
                        tap = event.tap
                    }
                attachments.add(
                    Attachment().apply {
                        contentType = "application/vnd.microsoft.card.hero"
                        content = card
                    },
                )
            }
        }

        return attachments
    }

    private data class MessageResponse(val id: String)

    private interface ConnectorMicrosoftApi {
        @POST
        @Headers("Content-Type: application/json")
        fun postResponse(
            @Url url: String,
            @Body activity: Activity,
        ): Call<MessageResponse>
    }

    private inner class CustomInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            tokenHandler.checkToken()

            var request = chain.request()
            request =
                request.newBuilder()
                    .addHeader("Authorization", "Bearer ${tokenHandler.token}")
                    .build()
            val response = chain.proceed(request)
            logger.debug { "Response sent to Teams : ${response.code} - ${response.message}" }

            return response
        }
    }
}
