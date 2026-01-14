/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.bot.mongo

import org.bson.Document
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for MongoAgg DSL functions.
 *
 * These tests verify that the DSL correctly generates MongoDB aggregation expressions
 * and that filterByOldestDateInPeriod produces the expected BSON structure.
 */
internal class MongoAggregationDSLTest {
    @Test
    fun `min should create correct $min document`() {
        val result = MongoAgg.min(5)
        assertEquals(Document("\$min", 5), result)
    }

    @Test
    fun `max should create correct $max document`() {
        val result = MongoAgg.max(10)
        assertEquals(Document("\$max", 10), result)
    }

    @Test
    fun `eq should create correct $eq document`() {
        val result = MongoAgg.eq("value", 5)
        assertEquals(Document("\$eq", listOf("value", 5)), result)
    }

    @Test
    fun `gte should create correct $gte document`() {
        val result = MongoAgg.gte("\$field", 10)
        assertEquals(Document("\$gte", listOf("\$field", 10)), result)
    }

    @Test
    fun `lte should create correct $lte document`() {
        val result = MongoAgg.lte("\$field", 20)
        assertEquals(Document("\$lte", listOf("\$field", 20)), result)
    }

    @Test
    fun `gt should create correct $gt document`() {
        val result = MongoAgg.gt("\$field", 5)
        assertEquals(Document("\$gt", listOf("\$field", 5)), result)
    }

    @Test
    fun `lt should create correct $lt document`() {
        val result = MongoAgg.lt("\$field", 15)
        assertEquals(Document("\$lt", listOf("\$field", 15)), result)
    }

    @Test
    fun `cond should create correct $cond document`() {
        val condition = MongoAgg.eq("\$value", null)
        val thenExpr = MongoAgg.min("\$\$this.date")
        val elseExpr = MongoAgg.max("\$\$this.date")
        val result = MongoAgg.cond(condition, thenExpr, elseExpr)
        assertEquals(
            Document("\$cond", listOf(condition, thenExpr, elseExpr)),
            result,
        )
    }

    @Test
    fun `ifNull should create correct $ifNull document`() {
        val expr = MongoAgg.min("\$\$this.date")
        val replacement = null
        val result = MongoAgg.ifNull(expr, replacement)
        assertEquals(Document("\$ifNull", listOf(expr, replacement)), result)
    }

    @Test
    fun `reduce should create correct $reduce document`() {
        val input = MongoAgg.field("stories")
        val initialValue: Any? = null
        val inExpr = MongoAgg.min("\$\$this.date")
        val result = MongoAgg.reduce(input, initialValue, inExpr)
        val expected =
            Document("\$reduce", Document("input", input).append("initialValue", initialValue).append("in", inExpr))
        assertEquals(expected, result)
    }

    @Test
    fun `expr should create correct $expr document`() {
        val condition = MongoAgg.gte("\$field", 10)
        val result = MongoAgg.expr(condition)
        assertEquals(Document("\$expr", condition), result)
    }

    @Test
    fun `field should create correct field path`() {
        val result = MongoAgg.field("stories")
        assertEquals("\$stories", result)
    }

    @Test
    fun `value should return correct aggregation variable`() {
        val result = MongoAgg.value()
        assertEquals("\$\$value", result)
    }

    @Test
    fun `thisVar should return correct aggregation variable`() {
        val result = MongoAgg.thisVar()
        assertEquals("\$\$this", result)
    }

    @Test
    fun `root should return correct aggregation variable`() {
        val result = MongoAgg.root()
        assertEquals("\$\$root", result)
    }

    @Test
    fun `and should create correct $and document`() {
        val expr1 = MongoAgg.gte("\$field1", 10)
        val expr2 = MongoAgg.lte("\$field2", 20)
        val result = MongoAgg.and(expr1, expr2)
        assertEquals(Document("\$and", listOf(expr1, expr2)), result)
    }

    @Test
    fun `or should create correct $or document`() {
        val expr1 = MongoAgg.gte("\$field1", 10)
        val expr2 = MongoAgg.lte("\$field2", 20)
        val result = MongoAgg.or(expr1, expr2)
        assertEquals(Document("\$or", listOf(expr1, expr2)), result)
    }

    @Test
    fun `filterByOldestDateInPeriod should return null when both dates are null`() {
        val result = MongoAgg.filterByOldestDateInPeriod("stories", "actions.date", null, null)
        assertNull(result)
    }

    @Test
    fun `filterByOldestDateInPeriod should create $expr with single condition when only fromDate is set`() {
        val fromDate = ZonedDateTime.parse("2025-01-10T10:00:00Z")
        val result = MongoAgg.filterByOldestDateInPeriod("stories", "actions.date", fromDate, null)
        assertNotNull(result)
        assertTrue(result is Document)
        val doc = result as Document
        assertTrue(doc.containsKey("\$expr"))
        val exprValue = doc.get("\$expr")
        assertTrue(exprValue is Document)
        val exprDoc = exprValue as Document
        assertTrue(exprDoc.containsKey("\$gte"))
    }

    @Test
    fun `filterByOldestDateInPeriod should create $expr with single condition when only toDate is set`() {
        val toDate = ZonedDateTime.parse("2025-01-20T10:00:00Z")
        val result = MongoAgg.filterByOldestDateInPeriod("stories", "actions.date", null, toDate)
        assertNotNull(result)
        assertTrue(result is Document)
        val doc = result as Document
        assertTrue(doc.containsKey("\$expr"))
        val exprValue = doc.get("\$expr")
        assertTrue(exprValue is Document)
        val exprDoc = exprValue as Document
        assertTrue(exprDoc.containsKey("\$lte"))
    }

    @Test
    fun `filterByOldestDateInPeriod should create $expr with $and when both dates are set`() {
        val fromDate = ZonedDateTime.parse("2025-01-10T10:00:00Z")
        val toDate = ZonedDateTime.parse("2025-01-20T10:00:00Z")
        val result = MongoAgg.filterByOldestDateInPeriod("stories", "actions.date", fromDate, toDate)
        assertNotNull(result)
        assertTrue(result is Document)
        val doc = result as Document
        assertTrue(doc.containsKey("\$expr"))
        val exprValue = doc.get("\$expr")
        assertTrue(exprValue is Document)
        val exprDoc = exprValue as Document
        assertTrue(exprDoc.containsKey("\$and"))
        val andValue = exprDoc.get("\$and")
        assertTrue(andValue is List<*>)
        val andList = andValue as List<*>
        assertEquals(2, andList.size)
    }

    @Test
    fun `filterByOldestDateInPeriod should use inclusive bounds`() {
        val fromDate = ZonedDateTime.parse("2025-01-10T10:00:00Z")
        val toDate = ZonedDateTime.parse("2025-01-20T10:00:00Z")
        val result = MongoAgg.filterByOldestDateInPeriod("stories", "actions.date", fromDate, toDate)
        assertNotNull(result)
        val doc = result as Document
        val exprDoc = doc.get("\$expr") as Document
        val andList = exprDoc.get("\$and") as List<*>
        val gteCondition = andList[0] as Document
        val lteCondition = andList[1] as Document
        assertTrue(gteCondition.containsKey("\$gte"))
        assertTrue(lteCondition.containsKey("\$lte"))
    }

    @Test
    fun `filterByOldestDateInPeriod should generate correct date comparison with Instant`() {
        val fromDate = ZonedDateTime.parse("2025-01-10T10:00:00Z")
        val toDate = ZonedDateTime.parse("2025-01-20T10:00:00Z")
        val result = MongoAgg.filterByOldestDateInPeriod("stories", "actions.date", fromDate, toDate)
        assertNotNull(result)
        val doc = result as Document
        val exprDoc = doc.get("\$expr") as Document
        val andList = exprDoc.get("\$and") as List<*>
        val gteCondition = andList[0] as Document
        val lteCondition = andList[1] as Document
        val gteList = gteCondition.get("\$gte") as List<*>
        val lteList = lteCondition.get("\$lte") as List<*>
        assertEquals(fromDate.toInstant(), gteList[1])
        assertEquals(toDate.toInstant(), lteList[1])
    }

    @Test
    fun `filterByOldestDateInPeriod should generate correct nested aggregation structure`() {
        val fromDate = ZonedDateTime.parse("2025-01-10T10:00:00Z")
        val result = MongoAgg.filterByOldestDateInPeriod("stories", "actions.date", fromDate, null)
        assertNotNull(result)
        val doc = result as Document
        val exprDoc = doc.get("\$expr") as Document
        val gteCondition = exprDoc.get("\$gte") as List<*>
        val minExpr = gteCondition[0] as Document
        assertTrue(minExpr.containsKey("\$min"))
        val reduceExpr = minExpr.get("\$min") as Document
        assertTrue(reduceExpr.containsKey("\$reduce"))
        val reduceDoc = reduceExpr.get("\$reduce") as Document
        assertEquals("\$stories", reduceDoc.get("input"))
        assertNull(reduceDoc.get("initialValue"))
        assertTrue(reduceDoc.containsKey("in"))
    }
}
