package fr.vsct.tock.bot.connector.slack


import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import org.junit.Before
import org.junit.Test

import kotlin.test.assertEquals


class UtilsTest {

    private val context: RoutingContext = mock()
    private val urlEncodedString = "arg1=val1&arg2=val2&arg3=val3"

    @Before
    fun before() {
        whenever(context.bodyAsString).thenReturn(urlEncodedString)
    }

    @Test
    fun testConvertUrlEncodedStringToJson() {
        val expectedJson = JsonObject().put("arg1", "val1").put("arg2", "val2").put("arg3", "val3").toString()
        assertEquals(expectedJson, context.convertUrlEncodedStringToJson())
    }
}