package ai.tock.shared

import ai.tock.shared.vertx.DetailedHealthcheckResults
import ai.tock.shared.vertx.makeDetailedHealthcheck
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkClass
import io.mockk.slot
import io.vertx.core.http.HttpServerResponse
import org.junit.jupiter.api.Test
import io.vertx.ext.web.RoutingContext
import org.junit.jupiter.api.BeforeEach
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


data class Task(
    var returnValue: Boolean,
    var invoked: Boolean
)

class DetailedHealthcheckTest {

    @RelaxedMockK
    lateinit var routingContext: RoutingContext
    @RelaxedMockK
    lateinit var response: HttpServerResponse

    private val statusSlot = slot<Int>()
    private val bodySlot = slot<String>()

    private val taskA = Task(true, false)
    private val taskB = Task(false, false)
    private val taskC = Task(true, false)
    private val selfCheck = Task(true, false)

    private val healthcheck = makeDetailedHealthcheck(
        listOf(
            Pair("a", { taskA.invoked = true; taskA.returnValue }),
            Pair("b", { taskB.invoked = true; taskB.returnValue }),
            Pair("c", { taskC.invoked = true; taskC.returnValue })
        ),
        selfCheck = { selfCheck.invoked = true; selfCheck.returnValue }
    )

    @BeforeEach
    fun beforeEach() {
        statusSlot.clear()
        bodySlot.clear()
        routingContext = mockkClass(RoutingContext::class, relaxed = true)
        response = mockkClass(HttpServerResponse::class, relaxed = true)
        every { routingContext.response() } returns response
        every { response.setStatusCode(capture(statusSlot)) } answers { response }
        every { response.end(capture(bodySlot)) } answers { response }
        taskA.invoked = false
        taskB.invoked = false
        taskC.invoked = false
        selfCheck.invoked = false
    }

    @Test
    fun `All tasks should be invoked`() {
        healthcheck(routingContext)
        assertTrue(
            taskA.invoked
                && taskB.invoked
                && taskC.invoked
                && selfCheck.invoked)
    }

    @Test
    fun `Failing selfCheck should prevent tasks invocation`() {
        selfCheck.returnValue = false
        healthcheck(routingContext)
        assertTrue(
            !taskA.invoked
                    && !taskB.invoked
                    && !taskC.invoked
                    && selfCheck.invoked)
    }

    @Test
    fun `Status code should be 207 when selfCheck return true`() {
        healthcheck(routingContext)
        assertEquals(statusSlot.captured, 207)
    }

    @Test
    fun `Status code should be 503 when selfCheck return false`() {
        selfCheck.returnValue = false
        healthcheck(routingContext)
        assertEquals(statusSlot.captured, 503)
    }

    @Test
    fun `Response JSON should contains 3 results with ids "a", "b", "c"`() {
        val mapper = jacksonObjectMapper()
        healthcheck(routingContext)
        val data : DetailedHealthcheckResults = mapper.readValue(bodySlot.captured)
        assertEquals(data.results.size, 3)
        assertNotNull(data.results.find { r -> r.id == "a" })
        assertNotNull(data.results.find { r -> r.id == "b" })
        assertNotNull(data.results.find { r -> r.id == "c" })
    }

    @Test
    fun `Response JSON task result status should be "OK" when the task return true`() {
        val mapper = jacksonObjectMapper()
        healthcheck(routingContext)
        val data : DetailedHealthcheckResults = mapper.readValue(bodySlot.captured)
        val taskAResult = data.results.find { r -> r.id == "a" }
        assertEquals(taskAResult?.status, "OK")
    }

    @Test
    fun `Response JSON task result status should be "KO" when the task return true`() {
        val mapper = jacksonObjectMapper()
        healthcheck(routingContext)
        val data : DetailedHealthcheckResults = mapper.readValue(bodySlot.captured)
        val taskAResult = data.results.find { r -> r.id == "b" }
        assertEquals(taskAResult?.status, "KO")
    }

}