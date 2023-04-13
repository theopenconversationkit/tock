package security.auth.cas

import ai.tock.shared.exception.ToRestException
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

    override fun <E: ToRestException>getCasAuthProvider (vertx: Vertx): CASAuthProvider<E> {
        return SampleCASAuthProvider(vertx) as CASAuthProvider<E>
    }
}
