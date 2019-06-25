package fr.vsct.tock.bot.connector.ga

import com.google.auth.oauth2.ServiceAccountCredentials
import fr.vsct.tock.bot.connector.ga.model.notification.GANotification
import fr.vsct.tock.bot.connector.ga.model.notification.GAPushNotification
import fr.vsct.tock.bot.connector.ga.model.notification.GATarget
import fr.vsct.tock.bot.connector.ga.model.notification.GaPushMessage
import fr.vsct.tock.shared.addJacksonConverter
import fr.vsct.tock.shared.create
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.property
import fr.vsct.tock.shared.resourceAsStream
import fr.vsct.tock.shared.retrofitBuilderWithTimeoutAndLogger
import fr.vsct.tock.shared.tokenAuthenticationInterceptor
import mu.KotlinLogging
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.Collections

/**
 * https://developers.google.com/actions/assistant/updates/notifications
 */
class GaNotificationClient {

    interface GaNotificationApi {
        @POST("./conversations:send")
        fun push(@Body notification: GAPushNotification): Call<ResponseBody>
    }

    private val logger = KotlinLogging.logger {}
    private val gaNotificationApi: GaNotificationApi
    private val version = property("tock_ga_notification_api_version", "2")
    private val baseUrl = property("tock_ga_notification_api_url", "https://actions.googleapis.com")

    init {
        gaNotificationApi = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_ga_notification_api_timeout", 30000),
            logger,
            interceptors = listOf(tokenAuthenticationInterceptor(getAccessToken()))
        )
            .baseUrl("$baseUrl/v$version/")
            .addJacksonConverter()
            .build()
            .create()
    }

    fun push(title: String, userId: String, intent: String, locale: String): Boolean {
        return try {
            gaNotificationApi.push(
                GAPushNotification(
                    GaPushMessage(
                        GANotification(title),
                        GATarget(
                            userId,
                            intent,
                            locale
                        )
                    )
                )
            ).execute().isSuccessful
        } catch (e: Exception) {
            logger.error(e)
            false
        }
    }

    private fun getAccessToken(): String {
        val token = loadCredentials().refreshAccessToken()
        return token.tokenValue
    }

    private fun loadCredentials(): ServiceAccountCredentials {
        val actionsApiServiceAccountFile = resourceAsStream("/service-account.json")
        val serviceAccountCredentials = ServiceAccountCredentials.fromStream(actionsApiServiceAccountFile)
        return serviceAccountCredentials.createScoped(
            Collections.singleton(
                "https://www.googleapis.com/auth/actions.fulfillment.conversation"
            )
        ) as ServiceAccountCredentials
    }

}