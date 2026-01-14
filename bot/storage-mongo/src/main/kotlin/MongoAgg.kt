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
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.bot.mongo

import org.bson.Document
import org.bson.conversions.Bson
import java.time.ZonedDateTime

/**
 * Lightweight DSL for building MongoDB aggregation expressions.
 *
 * This DSL provides simple functions to construct MongoDB aggregation operators
 * in a more readable way than using raw strings, while producing identical BSON output.
 *
 * **Type Design**: Parameters are intentionally typed `Any` to support:
 * - BSON expressions (Document, Bson)
 * - MongoDB field paths ("$field")
 * - Aggregation variables ("$$value", "$$this")
 * - Primitive values (String, Int, null, etc.)
 *
 * Usage example:
 * ```
 * val minDate = MongoAgg.min(MongoAgg.reduce(
 *     input = MongoAgg.field("stories"),
 *     initialValue = null,
 *     `in` = MongoAgg.cond(
 *         MongoAgg.eq(MongoAgg.value(), null),
 *         MongoAgg.ifNull(MongoAgg.min("${MongoAgg.thisVar()}.actions.date"), null),
 *         MongoAgg.min(listOf(MongoAgg.value(), MongoAgg.ifNull(MongoAgg.min("${MongoAgg.thisVar()}.actions.date"), MongoAgg.value())))
 *     )
 * ))
 * ```
 */
object MongoAgg {
    /**
     * Creates a $min aggregation expression.
     *
     * @param expr the expression to find the minimum of (can be null)
     * @return Document representing $min operator
     */
    fun min(expr: Any?): Document = Document("\$min", expr)

    /**
     * Creates a $max aggregation expression.
     *
     * @param expr the expression to find the maximum of (can be null)
     * @return Document representing $max operator
     */
    fun max(expr: Any?): Document = Document("\$max", expr)

    /**
     * Creates an $eq (equals) aggregation expression.
     *
     * @param a first operand
     * @param b second operand (can be null)
     * @return Document representing $eq operator
     */
    fun eq(
        a: Any,
        b: Any?,
    ): Document = Document("\$eq", listOf(a, b))

    /**
     * Creates a $gte (greater than or equal) aggregation expression.
     *
     * @param a first operand
     * @param b second operand (can be null)
     * @return Document representing $gte operator
     */
    fun gte(
        a: Any,
        b: Any?,
    ): Document = Document("\$gte", listOf(a, b))

    /**
     * Creates a $lte (less than or equal) aggregation expression.
     *
     * @param a first operand
     * @param b second operand (can be null)
     * @return Document representing $lte operator
     */
    fun lte(
        a: Any,
        b: Any?,
    ): Document = Document("\$lte", listOf(a, b))

    /**
     * Creates a $gt (greater than) aggregation expression.
     *
     * @param a first operand
     * @param b second operand (can be null)
     * @return Document representing $gt operator
     */
    fun gt(
        a: Any,
        b: Any?,
    ): Document = Document("\$gt", listOf(a, b))

    /**
     * Creates a $lt (less than) aggregation expression.
     *
     * @param a first operand
     * @param b second operand (can be null)
     * @return Document representing $lt operator
     */
    fun lt(
        a: Any,
        b: Any?,
    ): Document = Document("\$lt", listOf(a, b))

    /**
     * Creates a $cond (conditional) aggregation expression.
     *
     * @param ifExpr the condition expression
     * @param thenExpr the expression to evaluate if condition is true (can be null)
     * @param elseExpr the expression to evaluate if condition is false (can be null)
     * @return Document representing $cond operator
     */
    fun cond(
        ifExpr: Any,
        thenExpr: Any?,
        elseExpr: Any?,
    ): Document = Document("\$cond", listOf(ifExpr, thenExpr, elseExpr))

    /**
     * Creates an $ifNull aggregation expression.
     *
     * @param expr the expression to evaluate
     * @param replacement the replacement value if expr is null (can be null)
     * @return Document representing $ifNull operator
     */
    fun ifNull(
        expr: Any,
        replacement: Any?,
    ): Document = Document("\$ifNull", listOf(expr, replacement))

    /**
     * Creates a $reduce aggregation expression.
     *
     * @param input the array to reduce
     * @param initialValue the initial value for the accumulator (often null)
     * @param `in` the expression to apply to each element
     * @return Document representing $reduce operator
     */
    fun reduce(
        input: Any,
        initialValue: Any?,
        `in`: Any,
    ): Document =
        Document(
            "\$reduce",
            Document("input", input)
                .append("initialValue", initialValue)
                .append("in", `in`),
        )

    /**
     * Creates an $expr aggregation expression for use in queries.
     *
     * @param expr the expression to evaluate (can be Document, Bson, or any BSON-compatible value, can be null)
     * @return Bson representing $expr operator
     */
    fun expr(expr: Any?): Bson = Document("\$expr", expr)

    /**
     * Creates a MongoDB field path reference.
     *
     * @param path the field path (e.g., "stories" becomes "$stories")
     * @return String representing the MongoDB field path
     */
    fun field(path: String): String = "\$$path"

    /**
     * Returns the MongoDB aggregation variable "$$value" (used in $reduce, $map, etc.)
     */
    fun value(): String = "\$\$value"

    /**
     * Returns the MongoDB aggregation variable "$$this" (used in $reduce, $map, etc.)
     */
    fun thisVar(): String = "\$\$this"

    /**
     * Returns the MongoDB aggregation variable "$$root" (reference to the root document)
     */
    fun root(): String = "\$\$root"

    /**
     * Creates a $and aggregation expression.
     *
     * @param exprs the expressions to combine with AND logic
     * @return Document representing $and operator
     */
    fun and(vararg exprs: Any): Document = Document("\$and", exprs.toList())

    /**
     * Creates an $or aggregation expression.
     *
     * @param exprs the expressions to combine with OR logic
     * @return Document representing $or operator
     */
    fun or(vararg exprs: Any): Document = Document("\$or", exprs.toList())

    /**
     * Builds a MongoDB aggregation expression to calculate the oldest (earliest) date from a nested array field.
     * Uses $reduce with $min to find the minimum date across all elements.
     *
     * @param inputField the input array field (e.g., "stories")
     * @param datePath the path to the date field within each element (e.g., "actions.date")
     * @return Document representing the min(date) expression
     */
    private fun oldestDateInArray(
        inputField: String,
        datePath: String,
    ): Document {
        return dateInArray(inputField, datePath, ::min)
    }

    /**
     * Generic function to build a MongoDB aggregation expression for date aggregation in nested arrays.
     *
     * @param inputField the input array field (e.g., "stories")
     * @param datePath the path to the date field within each element (e.g., "actions.date")
     * @param aggregationFn the aggregation function to use (min for oldest, max for youngest)
     * @return Document representing the aggregation expression
     */
    private fun dateInArray(
        inputField: String,
        datePath: String,
        aggregationFn: (Any?) -> Document,
    ): Document {
        return aggregationFn(
            reduce(
                input = field(inputField),
                initialValue = null,
                `in` =
                    cond(
                        ifExpr = eq(value(), null),
                        thenExpr = ifNull(aggregationFn("${thisVar()}.$datePath"), null),
                        elseExpr =
                            aggregationFn(
                                listOf(
                                    value(),
                                    ifNull(aggregationFn("${thisVar()}.$datePath"), value()),
                                ),
                            ),
                    ),
            ),
        )
    }

    /**
     * Filters documents where the oldest date (from array) is within a period.
     *
     * Condition: fromDate <= oldestDate <= toDate
     *
     * This is typically used for filtering by creation date, where we want to check
     * if the first action date falls within the specified period.
     *
     * @param inputField the input array field (e.g., "stories")
     * @param datePath the path to the date field within each element (e.g., "actions.date")
     * @param fromDate optional start date filter (inclusive)
     * @param toDate optional end date filter (inclusive)
     * @return Bson filter expression, or null if both dates are null
     */
    fun filterByOldestDateInPeriod(
        inputField: String,
        datePath: String,
        fromDate: ZonedDateTime?,
        toDate: ZonedDateTime?,
    ): Bson? {
        val oldestDateExpr = oldestDateInArray(inputField, datePath)
        val conditionBuilders =
            listOfNotNull(
                fromDate?.let { { gte(oldestDateExpr, it.toInstant()) } },
                toDate?.let { { lte(oldestDateExpr, it.toInstant()) } },
            )
        return buildExprFromConditionBuilders(conditionBuilders)
    }

    /**
     * Builds a MongoDB $expr expression from a list of condition builders.
     *
     * @param conditionBuilders list of functions that build condition documents
     * @return Bson expression wrapping the conditions, or null if empty
     */
    private fun buildExprFromConditionBuilders(conditionBuilders: List<() -> Document>): Bson? {
        if (conditionBuilders.isEmpty()) {
            return null
        }
        val conditions = conditionBuilders.map { it() }
        return when {
            conditions.size == 1 -> expr(conditions.first())
            else -> expr(and(*conditions.toTypedArray()))
        }
    }
}
