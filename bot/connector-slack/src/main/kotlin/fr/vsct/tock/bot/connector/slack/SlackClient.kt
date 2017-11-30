package fr.vsct.tock.bot.connector.slack

import fr.vsct.tock.bot.connector.slack.model.SlackConnectorMessage
import fr.vsct.tock.shared.jackson.mapper
import fr.vsct.tock.shared.retrofitBuilderWithTimeoutAndLogger
import mu.KotlinLogging
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

object SlackClient {

    private val logger = KotlinLogging.logger {  }


    interface SlackApi {
        @POST("/services/{outToken1}/{outToken2}/{outToken3}")
        fun sendMessage(@Path("outToken1") outToken1: String, @Path("outToken2") outToken2: String, @Path("outToken3") outToken3: String, @Body message: RequestBody): Call<Void>
    }

    private val slackApi: SlackApi = retrofitBuilderWithTimeoutAndLogger(
            30000,
            logger
            )
            .baseUrl("https://hooks.slack.com")
            .build()
            .create(SlackApi::class.java)

    fun sendMessage(outToken1: String, outToken2: String, outToken3: String, message: SlackConnectorMessage) {
        val body = RequestBody.create(MediaType.parse("application/json"), mapper.writeValueAsBytes(message))
        val response = slackApi.sendMessage(outToken1, outToken2, outToken3, body).execute()
        println(response)
    }

}