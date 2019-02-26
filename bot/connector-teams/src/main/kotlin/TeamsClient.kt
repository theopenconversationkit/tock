package fr.vsct.tock.bot.connector.teams

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE
import com.microsoft.bot.schema.models.ActionTypes
import com.microsoft.bot.schema.models.Activity
import com.microsoft.bot.schema.models.ActivityTypes
import com.microsoft.bot.schema.models.Attachment
import com.microsoft.bot.schema.models.CardAction
import com.microsoft.bot.schema.models.TextFormatTypes
import com.microsoft.bot.schema.models.ThumbnailCard
import fr.vsct.tock.bot.connector.teams.messages.MarkdownHelper.activeLink
import fr.vsct.tock.bot.connector.teams.messages.TeamsBotMessage
import fr.vsct.tock.bot.connector.teams.messages.TeamsCardAction
import fr.vsct.tock.shared.addJacksonConverter
import fr.vsct.tock.shared.create
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.retrofitBuilderWithTimeoutAndLogger
import mu.KotlinLogging
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url
import java.time.Instant
import java.time.temporal.ChronoUnit.SECONDS


internal class TeamsClient(
    private val appId: String,
    private val password: String
) {

    @Volatile
    var token: String? = null
    @Volatile
    private var tokenExpiration: Instant? = null

    var loginApi: LoginMicrosoftOnline
    private val connectorApi: ConnectorMicrosoftApi
    private val logger = KotlinLogging.logger {}
    private val customInterceptor = CustomInterceptor()
    //copy root mapper and set property naming strategy
    val teamsMapper: ObjectMapper = mapper.copy().setPropertyNamingStrategy(SNAKE_CASE)

    init {
        loginApi = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_whatsapp_request_timeout_ms", 30000),
            logger
        )
            .baseUrl("https://login.microsoftonline.com")
            .addJacksonConverter(teamsMapper)
            .build()
            .create()

        connectorApi = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_whatsapp_request_timeout_ms", 30000),
            logger,
            interceptors = listOf(customInterceptor)
        )
            .baseUrl("https://smba.trafficmanager.net/emea/")
            .addJacksonConverter(teamsMapper)
            .build()
            .create()
    }

    fun sendMessage(callbackActivity: Activity, event: TeamsBotMessage) {
        //construct request
        val url =
            "${callbackActivity.serviceUrl()}/v3/conversations/${callbackActivity.conversation().id()}/activities/${callbackActivity.id()}"

        //get token the first time, the next times is handle by the interceptor
        if (token == null) fetchToken()

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
        val adapativeCard = mutableListOf<Attachment>()

        when (event) {
            is TeamsCardAction -> {
                val card = ThumbnailCard().withTitle(event.actionTitle).withButtons(event.buttons)
                adapativeCard.add(Attachment()
                    .withContentType("application/vnd.microsoft.card.thumbnail")
                    .withContent(card)
                )
            }
        }

        return adapativeCard
    }

    fun isTokenExpired(): Boolean {
        logger.debug { "IS TOKEN EXPIRED" }
        if (Instant.now().isAfter(
                tokenExpiration?.minus(
                    10,
                    SECONDS
                )
            )
        ) {
            return true
        }
        return false
    }


    fun checkToken() {
        if (this.token == null || isTokenExpired()) {
            fetchToken()
        }
    }

    private fun fetchToken() {
        val response = loginApi.login(
            clientId = appId, clientSecret = password
        ).execute()
        token = response.body()?.accessToken ?: error("empty access token")
        tokenExpiration = Instant.now().plus(response.body()?.expiresIn!!, SECONDS)
    }

    data class LoginResponse(
        val tokenType: String,
        val expiresIn: Long,
        val extExpiresIn: Long,
        val accessToken: String
    )

    private data class MessageResponse(val id: String)

    interface LoginMicrosoftOnline {

        @Headers("Host:login.microsoftonline.com", "Content-Type:application/x-www-form-urlencoded")
        @POST("/botframework.com/oauth2/v2.0/token")
        @FormUrlEncoded
        fun login(
            @Field("grant_type") grantType: String = "client_credentials",
            @Field("client_id") clientId: String,
            @Field("client_secret") clientSecret: String,
            @Field("scope") scope: String = "https://api.botframework.com/.default"
        ): Call<LoginResponse>
    }

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
            checkToken()

            var request = chain.request()
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            val response = chain.proceed(request)
            logger.debug { "Response sent to Teams : ${response.code()} - ${response.message()}" }

            return response
        }
    }
}