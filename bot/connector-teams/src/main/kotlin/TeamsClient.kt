package fr.vsct.tock.bot.connector.teams

//import retrofit2.create
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE
import com.microsoft.bot.schema.models.Activity
import com.microsoft.bot.schema.models.ActivityTypes
import fr.vsct.tock.bot.engine.action.SendSentence
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
import retrofit2.http.Path


class TeamsClient {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val loginApi: LoginMicrosoftOnline
    private val connectorApi: AnswerTheQuestionPlease
    private val customInterceptor = CustomInterceptor()
    //copy root mapper and set property naming strategy
    private val teamsMapper = mapper.copy().setPropertyNamingStrategy(SNAKE_CASE)

    init {
        loginApi = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_teams_request_timeout_ms", 30000),
            logger
        )
            .baseUrl("https://login.microsoftonline.com")
            .addJacksonConverter(teamsMapper)
            .build()
            .create()

        connectorApi = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_teams_request_timeout_ms", 30000),
            logger,
            interceptors = listOf(customInterceptor)
        )
            .baseUrl("https://smba.trafficmanager.net")
            .addJacksonConverter(teamsMapper)
            .build()
            .create()
    }

    fun sendMessage(callbackActivity: Activity, event: SendSentence) {
        //construct request
        var url = "/v3/conversations/${callbackActivity.conversation().id()}/activities/${callbackActivity.id()}"

        //get token
        val response = loginApi.login(
            clientId = "92427410-7ddc-4112-b2b8-e5a4f9b7fd7c", clientSecret = "bdiwPM7:;]ysgNSEYK9182("
        ).execute()
        val token = response.body()?.accessToken

        logger.debug { token }
        //TODO: intercept error
        //TODO: check token validity before asking for a new one

        //construct callbackActivity
        val activity = Activity()
            .withType(ActivityTypes.MESSAGE)
            .withText(event.stringText)
            .withRecipient(callbackActivity.from())
            .withFrom(callbackActivity.recipient())
            .withConversation(callbackActivity.conversation())
            .withReplyToId(callbackActivity.id())

        //send the message
        customInterceptor.token = token!!
        customInterceptor.url = callbackActivity.serviceUrl()
        val messageResponse =
            connectorApi.postResponse(callbackActivity.conversation().id(), callbackActivity.id(), activity).execute()
        logger.debug { "MESSAGE SENT" }
    }


    data class LoginResponse(val tokenType: String, val expiresIn: Int, val extExpiresIn: Int, val accessToken: String)
    data class MessageResponse(val id: String)


    private interface LoginMicrosoftOnline {

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

    private interface AnswerTheQuestionPlease {

        @POST("/emea/v3/conversations/{conversationId}/activities/{activityId}")
        @Headers("Content-Type: application/json")
        fun postResponse(
            @Path("conversationId") conversationId: String,
            @Path("activityId") activityId: String,
            @Body activity: Activity
        ): Call<MessageResponse>

    }

    private class CustomInterceptor : Interceptor {

        var token: String = ""
        var url: String = ""

        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()
            println("Before : " + request.url().toString())
//            if (!request.url().toString().contains(url)) {
//                println("Not the same url")
//                val newUrl = HttpUrl.parse(url)
//                request = request.newBuilder()
//                        .addHeader("Authorization", "Bearer $token")
//                        .url(newUrl)
//                        .build()
//            }
            logger.debug { "After : " + request.url() }
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            //Here you can rewrite/modify the response
            val response = chain.proceed(request)
            logger.debug { response.code() }
            logger.debug { response.message() }
            return response
        }
    }
}