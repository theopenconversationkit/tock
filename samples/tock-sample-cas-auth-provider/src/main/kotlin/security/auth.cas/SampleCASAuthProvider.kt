package security.auth.cas

import ai.tock.shared.exception.rest.ForbiddenException
import ai.tock.shared.property
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.security.auth.CASAuthProvider
import ai.tock.shared.vertx.WebVerticle
import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.core.Vertx
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.AuthenticationHandler
import mu.KotlinLogging
import org.pac4j.cas.client.CasClient
import org.pac4j.cas.config.CasConfiguration
import org.pac4j.core.config.Config
import org.pac4j.vertx.auth.Pac4jAuthProvider
import org.pac4j.vertx.auth.Pac4jUser
import org.pac4j.vertx.handler.impl.SecurityHandler
import org.pac4j.vertx.handler.impl.SecurityHandlerOptions

/**
 * Sample of CAS based Auth provider with Customer specific Role mapping
 */
class SampleCASAuthProvider(vertx: Vertx) : CASAuthProvider<ForbiddenException>(vertx) {

    private val pacConfig: Config
    private val genericErrorPage: String

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val CAS_CLIENT_PROFILE = "CasClient"
        private const val USERID = "userId"
    }

    init {
        logger.info { "init ${this::class.java.name}"}
        val casLoginUrl = property("cas_login_url", "https://casserver.herokuapp.com/cas/login")
        val casCallbackUrl = property("cas_callback_url", "http://localhost:7999/rest/callback")

        val casConfiguration = CasConfiguration(casLoginUrl)
        val casClient = CasClient(casConfiguration)

        pacConfig = Config(casCallbackUrl, casClient)
        pacConfig.sessionStore = sessionStore

        val url = this.javaClass.getResource("/generic-error-page.html")!!
        logger.debug { "read generic error page: $url" }
        genericErrorPage = url.readText(Charsets.UTF_8)
    }

    override fun getConfig(): Config {
        return pacConfig
    }

    override fun readCasLogin(user: Pac4jUser): String {
        val userProfile = user.principal().getJsonObject(CAS_CLIENT_PROFILE)
            ?: throw IllegalStateException("Pac4J user must contains a CasClient property")
        return userProfile.getString(USERID)
    }

    override fun readRolesByNamespace(user: Pac4jUser): Map<String, Set<String>> {
        val result = HashMap<String, Set<String>>()

        // In this sample every user is a bot user
        // Alternatively user.principal may be used to map specific rights to Tock roles

        val login = readCasLogin(user)
        result[login] = setOf(TockUserRole.botUser.name)
        return result
    }

    override fun handleUpgradeFailure(rc: RoutingContext, code: Int, cause: Throwable?) {
        if (null == cause) {
            logger.error { "authentication error: $code" }
        } else {
            logger.error("authentication error", cause)
        }

        rc.response()
            .putHeader(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8")
            .end(genericErrorPage)
    }


    override fun createAuthHandler(verticle: WebVerticle<ForbiddenException>): AuthenticationHandler {
        val options: SecurityHandlerOptions = SecurityHandlerOptions().setClients("CasClient")
        options.authorizers = enabledPacAuthorizers

        return SecurityHandler(vertx, sessionStore, getConfig(), Pac4jAuthProvider(), options)
    }

}
