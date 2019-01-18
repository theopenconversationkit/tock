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
import retrofit2.http.*
import java.io.IOException
import java.time.Instant
import java.time.temporal.ChronoUnit


class TeamsClient {


    @Volatile
    private var token: String? = null
    @Volatile
    private var instantTokenValidity: Instant? = null

    private val loginApi: LoginMicrosoftOnline
    private val connectorApi: ConnectorMicrosoftApi
    private val logger = KotlinLogging.logger {}
    private val customIntercpetor = CustomInterceptor()
    //copy root mapper and set property naming strategy
    private val teamsMapper = mapper.copy().setPropertyNamingStrategy(SNAKE_CASE)

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
            interceptors = listOf(customIntercpetor)
        )
            .baseUrl("https://smba.trafficmanager.net/emea/")
            .addJacksonConverter(teamsMapper)
            .build()
            .create()
    }

    fun sendMessage(callbackActivity: Activity, event: SendSentence) {
        //construct request
        val url = "${callbackActivity.serviceUrl()}/v3/conversations/${callbackActivity.conversation().id()}/activities/${callbackActivity.id()}"

        //get token
        checkToken()

        logger.debug { token }
        //TODO: intercept error

        //construct callbackActivity
        val activity = Activity()
            .withType(ActivityTypes.MESSAGE)
            .withText(event.stringText)
            .withRecipient(callbackActivity.from())
            .withFrom(callbackActivity.recipient())
            .withConversation(callbackActivity.conversation())
            .withReplyToId(callbackActivity.id())

        //send the message
        customIntercpetor.token = this.token!!
        customIntercpetor.url = callbackActivity.serviceUrl()
        val messageResponse = connectorApi.postResponse(
                url,
                activity).execute()
        logger.debug { "response sent !" }
    }

    private fun isTokenExpired() : Boolean {
        if (Instant.now().isAfter(instantTokenValidity?.minus(10, ChronoUnit.SECONDS))) {
            return true
        }
        return false
    }

    fun checkToken() {
        if (this.token == null || isTokenExpired()) {
            println("token not valid, need a new one")
            val response = loginApi.login(
                    //TODO: Move those parameters to Admin responsability
                    clientId = "92427410-7ddc-4112-b2b8-e5a4f9b7fd7c", clientSecret = "bdiwPM7:;]ysgNSEYK9182("
            ).execute()
            token = response.body()?.accessToken
            //instantTokenValidity = Instant.now().plus(response.body()?.expiresIn!!, ChronoUnit.SECONDS)
            instantTokenValidity = Instant.now().plus(60, ChronoUnit.SECONDS)
        } else {
            println("same old token")
        }
    }

    data class LoginResponse(val tokenType: String, val expiresIn: Long, val extExpiresIn: Long, val accessToken: String)
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
        ) : Call<LoginResponse>
    }

    private interface ConnectorMicrosoftApi {

        @POST
        @Headers("Content-Type: application/json")
        fun postResponse(
            @Url url: String,
            @Body activity: Activity
        ) : Call<MessageResponse>

    }


    inner class CustomInterceptor : Interceptor {

        var token: String = ""
        var url: String = ""

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()
            request = request.newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
            val response = chain.proceed(request)
            logger.debug("Response sent to Teams : ${response.code()} - ${response.message()}")

//            GlobalScope.launch {
//                println("checking token")
//                checkToken()
//            }


            return response
        }
    }
}