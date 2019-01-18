package fr.vsct.tock.shared.security

import fr.vsct.tock.shared.addJacksonConverter
import fr.vsct.tock.shared.create
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.property
import fr.vsct.tock.shared.retrofitBuilderWithTimeoutAndLogger
import mu.KotlinLogging
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

object RetrofitAWSPublicKeyClient {

    private interface AWSPublicKeyApi {

        @GET("/{keyID}")
        fun getPublicKey(@Path("keyID") keyID: String): Call<ResponseBody>
    }

    private val api: AWSPublicKeyApi
    private val logger = KotlinLogging.logger {}
    private val awsRegion = property("aws_region","eu-west-1")

    init {

        api = retrofitBuilderWithTimeoutAndLogger(
            longProperty("tock_aws_public_key_request_timeout_ms", 30000),
            logger
        )
            .baseUrl("https://public-keys.auth.elb.$awsRegion.amazonaws.com")
            .addJacksonConverter()
            .build()
            .create()
    }

    fun getPublicKey(keyID: String): String? {
        return api.getPublicKey(keyID).execute().body()?.string()
    }
}