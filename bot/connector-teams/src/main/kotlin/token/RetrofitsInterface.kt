package fr.vsct.tock.bot.connector.teams.token

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

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

data class LoginResponse(
    val tokenType: String,
    val expiresIn: Long,
    val extExpiresIn: Long,
    val accessToken: String
)
