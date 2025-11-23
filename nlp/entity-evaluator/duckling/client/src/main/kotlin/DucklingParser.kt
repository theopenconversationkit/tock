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

package ai.tock.duckling.client

import ai.tock.duckling.client.DucklingDimensions.TIME_DIMENSION
import ai.tock.nlp.core.EntityType
import ai.tock.nlp.core.merge.ValueDescriptor
import ai.tock.nlp.core.service.entity.EntityTypeClassifier
import ai.tock.nlp.core.service.entity.EntityTypeEvaluator
import ai.tock.nlp.core.service.entity.EntityTypeRecognition
import ai.tock.nlp.core.service.entity.EntityTypeValue
import ai.tock.nlp.core.service.entity.EvaluationResult
import ai.tock.nlp.entity.AmountOfMoneyValue
import ai.tock.nlp.entity.DistanceValue
import ai.tock.nlp.entity.DurationValue
import ai.tock.nlp.entity.EmailValue
import ai.tock.nlp.entity.NumberValue
import ai.tock.nlp.entity.OrdinalValue
import ai.tock.nlp.entity.PhoneNumberValue
import ai.tock.nlp.entity.UrlValue
import ai.tock.nlp.entity.Value
import ai.tock.nlp.entity.VolumeValue
import ai.tock.nlp.entity.date.DateEntityGrain
import ai.tock.nlp.entity.date.DateEntityValue
import ai.tock.nlp.entity.date.DateIntervalEntityValue
import ai.tock.nlp.entity.temperature.TemperatureUnit
import ai.tock.nlp.entity.temperature.TemperatureValue
import ai.tock.nlp.model.EntityCallContext
import ai.tock.nlp.model.EntityCallContextForEntity
import ai.tock.nlp.model.EntityCallContextForIntent
import ai.tock.nlp.model.EntityCallContextForSubEntities
import mu.KotlinLogging
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 *
 */
internal object DucklingParser : EntityTypeEvaluator, EntityTypeClassifier, Parser {
    private val logger = KotlinLogging.logger {}
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

    private const val DUCKLING_AVERAGE_PERTINENCE: Double = 0.8

    override fun classifyEntities(
        context: EntityCallContext,
        text: String,
    ): List<EntityTypeRecognition> {
        return classify(context, text)
    }

    private fun classify(
        context: EntityCallContext,
        text: String,
    ): List<EntityTypeRecognition> {
        return when (context) {
            is EntityCallContextForIntent -> classifyForIntent(context, text)
            is EntityCallContextForEntity -> emptyList() // TODO
            is EntityCallContextForSubEntities -> emptyList() // TODO
        }
    }

    private fun classifyForIntent(
        context: EntityCallContextForIntent,
        text: String,
    ): List<EntityTypeRecognition> {
        val matchedEntities =
            context.intent
                .entities
                .filter { DucklingDimensions.entityTypes.contains(it.entityType.name) }
        return if (matchedEntities.isEmpty()) {
            emptyList()
        } else {
            matchedEntities
                .groupBy { DucklingDimensions.tockTypeToDucklingType(it.entityType) }
                .map { it.key to it.value.first() }
                .toMap()
                .let {
                    classify(
                        it.mapValues { it.value.entityType },
                        context.language.language,
                        it.keys,
                        context.referenceDate,
                        text,
                    )
                }
        }
    }

    private fun classify(
        entityTypeMap: Map<String, EntityType>,
        language: String,
        dimensions: Set<String>,
        referenceDate: ZonedDateTime,
        textToParse: String,
    ): List<EntityTypeRecognition> {
        val parseResult =
            DucklingClient.parse(language, dimensions.toList(), referenceDate, referenceDate.zone, textToParse)

        return if (parseResult == null) {
            logger.warn { "parsing error for $language $dimensions $textToParse" }
            emptyList()
        } else {
            dimensions
                .flatMap { parseDimension(parseResult, it) }
                .map {
                    EntityTypeRecognition(
                        EntityTypeValue(
                            it.start,
                            it.end,
                            entityTypeMap.getValue(it.type),
                            it.value,
                            true,
                        ),
                        DUCKLING_AVERAGE_PERTINENCE,
                    )
                }
        }
    }

    override fun evaluate(
        context: EntityCallContextForEntity,
        text: String,
    ): EvaluationResult {
        val values =
            parse(
                context.language.language,
                DucklingDimensions.tockTypeToDucklingType(context.entityType),
                context.referenceDate,
                text,
            )
        val v = values.firstOrNull()
        return if (v == null) {
            EvaluationResult(false)
        } else {
            EvaluationResult(true, v.value, if (v.start == 0 && v.end == text.length) 1.0 else 0.5)
        }
    }

    override fun parse(
        language: String,
        dimension: String,
        referenceDate: ZonedDateTime,
        textToParse: String,
    ): List<ValueWithRange> {
        val parseResult =
            DucklingClient.parse(language, listOf(dimension), referenceDate, referenceDate.zone, textToParse)

        return if (parseResult == null) {
            logger.warn { "parse error for $language $dimension $textToParse" }
            emptyList()
        } else {
            parseDimension(parseResult, dimension)
        }
    }

    private fun parseDimension(
        parseResult: JSONValue,
        dimension: String,
    ): List<ValueWithRange> {
        return when (dimension) {
            TIME_DIMENSION -> parseDate(parseResult)
            "number" -> parseSimple(parseResult, dimension) { NumberValue(it[":value"].number()) }
            "ordinal" -> parseSimple(parseResult, dimension) { OrdinalValue(it[":value"].number()) }
            "distance" ->
                parseSimple(
                    parseResult,
                    dimension,
                ) { DistanceValue(it[":value"].number(), it[":unit"].string()) }
            "temperature" -> {
                parseSimple(
                    parseResult,
                    dimension,
                ) { TemperatureValue(it[":value"].number(), TemperatureUnit.valueOf(it[":unit"].string())) }
            }
            "volume" ->
                parseSimple(
                    parseResult,
                    dimension,
                ) { VolumeValue(it[":value"].number(), it[":unit"].string()) }
            "amount-of-money" -> {
                parseSimple(
                    parseResult,
                    dimension,
                ) { AmountOfMoneyValue(it[":value"].number(), it[":unit"].string()) }
            }
            "url" -> parseSimple(parseResult, dimension) { UrlValue(it[":value"].string()) }
            "email" -> parseSimple(parseResult, dimension) { EmailValue(it[":value"].string()) }
            "phone-number" -> parseSimple(parseResult, dimension) { PhoneNumberValue(it[":value"].string()) }
            "duration" -> parseDuration(parseResult)
            else -> error("Not yet supported yet : $dimension")
        }
    }

    private fun parseDuration(parseResult: JSONValue): List<ValueWithRange> {
        var start = Integer.MAX_VALUE
        var end = Integer.MIN_VALUE
        return parseResult.iterable().mapNotNull {
            if (it[":dim"].string() == "duration") {
                val n = it[":value"][":normalized"]
                val v = n[":value"].number().toLong()
                val u =
                    if (n[":unit"].string() == "second") ChronoUnit.SECONDS else error("unknown unit: ${n[":unit"]}")

                start = Math.min(start, it[":start"].int())
                end = Math.max(end, it[":end"].int())
                Duration.of(v, u)
            } else {
                null
            }
        }
            .takeUnless { it.isEmpty() }
            ?.reduce { a, b -> a + b }
            ?.let {
                listOf(ValueWithRange(start, end, DurationValue(it), "duration"))
            }
            ?: emptyList()
    }

    private fun parseSimple(
        parseResult: JSONValue,
        dim: String,
        parseFunction: (JSONValue) -> Value,
    ): List<ValueWithRange> {
        return parseResult.iterable().mapNotNull {
            if (it[":dim"].string() == dim) {
                val value = parseFunction.invoke(it[":value"])
                val start = it[":start"].int()
                val end = it[":end"].int()
                ValueWithRange(start, end, value, dim)
            } else {
                null
            }
        }
    }

    private fun parseDate(parseResult: JSONValue): List<ValueWithRange> {
        var result = mutableListOf<ValueWithRange>()
        try {
            if (!parseResult.isEmpty()) {
                for (a in parseResult.iterable()) {
                    if (a[":dim"].string() == TIME_DIMENSION) {
                        val start = a[":start"].int()
                        val end = a[":end"].int()

                        val valueMap = a[":value"]

                        val grain = valueMap[":grain"]
                        if (grain.isNotNull()) {
                            result.add(
                                ValueWithRange(
                                    start,
                                    end,
                                    DateEntityValue(
                                        ZonedDateTime.parse(valueMap[":value"].string(), formatter),
                                        DateEntityGrain.valueOf(grain.string()),
                                    ),
                                    TIME_DIMENSION,
                                ),
                            )
                        } else {
                            // type interval
                            val fromMap = valueMap[":from"]
                            val toMap = valueMap[":to"]
                            var entityValue: ValueWithRange? = null
                            if (toMap.isNotNull() && fromMap.isNotNull()) {
                                val toGrain = toMap[":grain"]
                                if (toGrain.isNotNull()) {
                                    entityValue =
                                        ValueWithRange(
                                            start,
                                            end,
                                            DateIntervalEntityValue(
                                                DateEntityValue(
                                                    ZonedDateTime.parse(fromMap[":value"].string(), formatter),
                                                    DateEntityGrain.valueOf(fromMap[":grain"].string()),
                                                ),
                                                DateEntityValue(
                                                    ZonedDateTime.parse(toMap[":value"].string(), formatter),
                                                    DateEntityGrain.valueOf(toMap[":grain"].string()),
                                                ),
                                            ),
                                            TIME_DIMENSION,
                                        )
                                }
                            }

                            if (entityValue == null) {
                                val vMap = if (fromMap.isNotNull()) fromMap else toMap
                                if (vMap.isNotNull()) {
                                    entityValue =
                                        ValueWithRange(
                                            start,
                                            end,
                                            DateEntityValue(
                                                ZonedDateTime.parse(vMap[":value"].string(), formatter),
                                                DateEntityGrain.valueOf(vMap[":grain"].string()),
                                            ),
                                            TIME_DIMENSION,
                                        )
                                }
                            }

                            if (entityValue != null) {
                                result.add(entityValue)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(e) { e.message }
        }

        // merge
        result.sort()
        if (result.size > 1) {
            var skipNext = false
            val result2 = mutableListOf<ValueWithRange>()
            for (i in result.indices) {
                if (!skipNext) {
                    if (i < result.size - 1) {
                        // overlap, try to mergeDate
                        if (result[i].end > result[i + 1].start) {
                            result2.add(mergeDate(result[i], result[i + 1]))
                            skipNext = true
                        } else {
                            result2.add(result[i])
                        }
                    } else {
                        result2.add(result[i])
                    }
                } else {
                    skipNext = false
                }
            }
            result = result2
        }

        return result
    }

    private fun mergeDate(
        r1: ValueWithRange,
        r2: ValueWithRange,
    ): ValueWithRange {
        // overlap, try to merge
        if (r1.value is DateEntityValue && r2.value is DateEntityValue) {
            if (r1.value.grain == r2.value.grain) {
                return ValueWithRange(
                    r1.start,
                    r2.end,
                    DateIntervalEntityValue(r1.value, r2.value),
                    TIME_DIMENSION,
                )
            } else {
                val dateGrain = if (r1.value.grain.time) r2.value else r1.value
                val timeGrain = if (r1.value.grain.time) r2.value else r1.value
                return ValueWithRange(
                    r1.start,
                    r2.end,
                    DateEntityValue(
                        dateGrain.date.plus(Duration.ofSeconds(timeGrain.date.toLocalTime().toSecondOfDay().toLong())),
                        timeGrain.grain,
                    ),
                    TIME_DIMENSION,
                )
            }
        }
        // return the first for now
        return r1
    }

    override fun merge(
        context: EntityCallContextForEntity,
        values: List<ValueDescriptor>,
    ): ValueDescriptor? {
        return DatesMerge.merge(context, values)
    }
}
