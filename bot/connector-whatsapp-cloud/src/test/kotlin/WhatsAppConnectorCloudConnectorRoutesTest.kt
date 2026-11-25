package ai.tock.bot.connector.whatsapp.cloud

import ai.tock.bot.engine.ConnectorController
import ai.tock.shared.security.RequestFilter
import ai.tock.shared.sharedTestModule
import ai.tock.shared.tockInternalInjector
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjector
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.vertx.ext.web.Router
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class WhatsAppConnectorCloudConnectorRoutesTest {
    @BeforeEach
    fun inject() {
        tockInternalInjector = KodeinInjector()
        tockInternalInjector.inject(Kodein { import(sharedTestModule) })
    }

    private fun connector(path: String) =
        WhatsAppConnectorCloudConnector(
            connectorId = path,
            phoneNumberId = "phone",
            whatsAppBusinessAccountId = "account",
            metaApplicationId = null,
            path = path,
            verifyToken = null,
            requestFilter = mockk<RequestFilter>(relaxed = true),
            client = mockk(relaxed = true),
        )

    @Test
    fun `GIVEN a connector WHEN it registers its routes THEN the template creation is under its own path`() {
        val installer = slot<(Router) -> Unit>()
        val controller: ConnectorController = mockk(relaxed = true)
        every { controller.registerServices("/whatsapp-a", capture(installer)) } returns Unit
        val router: Router = mockk(relaxed = true)

        connector("/whatsapp-a").register(controller)
        installer.captured(router)

        verify { router.post("/whatsapp-a/create_template") }
        // the deprecated path is kept for the existing callers
        verify { router.post("/create_template") }
    }
}
