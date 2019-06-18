package fr.vsct.tock.bot.connector.teams

import com.microsoft.bot.schema.models.*
import fr.vsct.tock.bot.connector.teams.messages.MarkdownHelper.activeLink
import fr.vsct.tock.bot.connector.teams.messages.TeamsBotMessage
import fr.vsct.tock.bot.connector.teams.messages.TeamsCardAction
import fr.vsct.tock.bot.connector.teams.messages.TeamsCarousel
import fr.vsct.tock.bot.connector.teams.messages.TeamsHeroCard
import fr.vsct.tock.bot.connector.teams.token.TokenHandler
import fr.vsct.tock.shared.addJacksonConverter
import fr.vsct.tock.shared.create
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.retrofitBuilderWithTimeoutAndLogger
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


        connectorApi = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_whatsapp_request_timeout_ms", 30000),
            logger,
            interceptors = listOf(customInterceptor)
        )
            .baseUrl("https://smba.trafficmanager.net/emea/")
            .addJacksonConverter(tokenHandler.teamsMapper)
            .build()
            .create()
    }

    fun sendMessage(callbackActivity: Activity, event: TeamsBotMessage) {
        //construct request
        val url = "${callbackActivity.serviceUrl()}/v3/conversations/${callbackActivity.conversation().id()}/activities/${callbackActivity.id()}"

        //construct callbackActivity
        val activity = Activity()
            .withType(ActivityTypes.MESSAGE)
            .withText(activeLink(event.text))
            .withTextFormat(TextFormatTypes.MARKDOWN)
            .withRecipient(callbackActivity.from())
            .withAttachments(getAttachment(event))
            .withFrom(callbackActivity.recipient())
            .withConversation(callbackActivity.conversation())
            .withReplyToId(callbackActivity.id())

        if (event is TeamsCarousel) {
            activity.withAttachmentLayout(AttachmentLayoutTypes.CAROUSEL)
        }
        //send the message
        val messageResponse = connectorApi.postResponse(
            url,
            activity
        ).execute()
        if (!messageResponse.isSuccessful) {
            logger.warn {
                "Microsoft Login Api Error : ${messageResponse.code()} // ${messageResponse.errorBody()}"
            }
        }
    }

    private fun getAttachment(event: TeamsBotMessage): MutableList<Attachment>? {
        val attachments = mutableListOf<Attachment>()

        when (event) {
            is TeamsCardAction -> {
                val card = ThumbnailCard().withTitle(event.actionTitle).withButtons(event.buttons)
                attachments.add(Attachment()
                    .withContentType("application/vnd.microsoft.card.thumbnail")
                    .withContent(card)
                )
            }
            is TeamsCarousel -> {
                val listElement = mutableListOf<TeamsBotMessage>()
                listElement.addAll(event.listMessage)
                while (listElement.isNotEmpty()) {
                    attachments.addAll(
                            getAttachment(
                                    listElement.removeAt(0)
                            ) as MutableList<Attachment>
                    )
                }
            }
            is TeamsHeroCard -> {
                val card = HeroCard()
                    .withTitle(event.title)
                    .withSubtitle(event.subtitle)
                    .withText(event.attachmentContent)
                    .withImages(event.images)
                    .withButtons(event.buttons)
                    .withTap(event.tap)
                attachments.add(Attachment()
                        .withContentType("application/vnd.microsoft.card.hero")
                        .withContent(card)
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
            @Body activity: Activity
        ): Call<MessageResponse>

    }

    private inner class CustomInterceptor : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            tokenHandler.checkToken()

            var request = chain.request()
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer ${tokenHandler.token}")
                .build()
            val response = chain.proceed(request)
            logger.debug { "Response sent to Teams : ${response.code()} - ${response.message()}" }

            return response
        }
    }
}
