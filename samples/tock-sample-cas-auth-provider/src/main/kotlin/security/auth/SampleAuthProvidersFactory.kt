package security.auth

import ai.tock.shared.security.auth.CASAuthProvider
import ai.tock.shared.security.auth.spi.CASAuthProviderFactory
import io.vertx.core.Vertx
import mu.KotlinLogging

/**
 * Factory for our custom CAS auth provider
 */
class SampleAuthProvidersFactory : CASAuthProviderFactory {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    constructor() {
        // Ease SPI
    }

    override fun getCasAuthProvider(vertx: Vertx): CASAuthProvider {

        return SampleCASAuthProvider(vertx)
    }
}
