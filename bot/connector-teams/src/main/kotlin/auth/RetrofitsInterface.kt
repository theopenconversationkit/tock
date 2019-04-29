package fr.vsct.tock.bot.connector.teams.auth

import com.google.gson.Gson
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url
import java.io.Serializable


data class MicrosoftOpenidMetadata(
    val issuer: String,
    val authorizationEndpoint: String,
    val jwksUri: String,
    val idTokenSigningAlgValuesSupported: ArrayList<String>,
    val tokenEndpointAuthMethodsSupported: List<String>
)

internal interface MicrosoftOpenIdMetadataApi {

    @GET(".well-known/openidconfiguration/")
    fun getMicrosoftOpenIdMetadata(): Call<MicrosoftOpenidMetadata>

    @GET(".well-known/openid-configuration/")
    fun getMicrosoftOpenIdMetadataForBotFwkEmulator(): Call<MicrosoftOpenidMetadata>
}

data class MicrosoftValidSigningKeys(
    val keys: List<MicrosoftValidSigningKey>
) : Serializable

data class MicrosoftValidSigningKey(
    val kty: String,
    val use: String,
    val kid: String,
    val x5t: String?,
    val n: String,
    val e: String,
    val x5c: List<String>?,
    val endorsements: List<String>?
) : Serializable {

    override fun toString(): String {
        return Gson().toJson(this)
    }
}

internal interface MicrosoftJwksApi {

    @GET
    fun getJwk(
        @Url url: String
    ): Call<MicrosoftValidSigningKeys>
}
