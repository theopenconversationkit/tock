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

import ai.tock.nlp.core.merge.ValueDescriptor
import ai.tock.nlp.entity.date.DateEntityGrain
import ai.tock.nlp.entity.date.DateEntityGrain.day
import ai.tock.nlp.entity.date.DateEntityRange
import ai.tock.nlp.entity.date.DateEntityValue
import ai.tock.nlp.entity.date.DateIntervalEntityValue
import ai.tock.nlp.model.EntityCallContextForEntity
import ai.tock.shared.defaultZoneId
import ai.tock.shared.error
import ai.tock.shared.injector
import com.github.salomonbrys.kodein.instance
import mu.KotlinLogging
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * For now only [java.util.Locale.FRENCH] is supported for "additional" merge, with a *very* hacky support.
 * Better support is on the way.
 */
internal object DatesMerge {

    private val logger = KotlinLogging.logger {}

    private val removeDuplicateSpaceRegexp = "\\s+".toRegex()
    private val frenchAddRegex =
        ".*prochaine?$|.*suivante?$|.*qui suit$|.*(d')? ?apr[eèé]s$|.*plus tard$|.*derni[èe]re?$|.*pass[ée]e?$|.*pr[eé]c[eé]dente?$|.*(d')? ?avant$|.*plus t[oô]t$|lendemain|le lendemain|la veille|ce jour|(le |la )?m[eê]me jour(n[eé]e)?".toRegex()
    private val frenchChangeHourRegex =
        (
            "(dans )?(le |la |en |(en )?fin de |(en )?d[ée]but de |(en )?milieu de )?soir[ée]?e?" +
                "|(dans )?(le |la |en |(en )?fin de |(en )?d[ée]but de |(en )?milieu de )?mat(in[ée]?e?)?" +
                "|(dans )?(l. ?|(en )?fin d. ?|(en )?d[ée]but d. ?|(en )?milieu d. ?)?apr[eéè](s?[ \\-]?midi|m)" +
                "|([aà]|vers|apr(e|è)s|[aà] partir de|avant|jusqu'[aà])? ?((([01]?\\d)|(2[0-3]))([:h]|heures?)?([0-5]\\d)?)(du|dans l[ae']? ?|au|en|l[ae'] ?|dès l?[ae']? ?|(en )?d[ée]but (de |d' ?)|(en )?fin (de |d' ?)|(en )?d[ée]but (d' ?|de ))?(mat(in[ée]?e?)|soir[ée]?e?|apr[eéè]s?[ \\-]?midi|journ[ée]e)?" +
                "|entre ?((([01]?\\d)|(2[0-3]))([:h]|heures?)?([0-5]\\d)?)(du|dans l[ae']? ?|au|en|l[ae'] ?|dès l?[ae']? ?|(en )?d[ée]but (de |d' ?)|(en )?fin (de |d' ?)|(en )?d[ée]but (d' ?|de ))?(mat(in[ée]?e?)|soir[ée]?e?|apr[eéè]s?[ \\-]?midi|journ[ée]e)? et .*"
            ).toRegex()
    private val frenchChangeDayInMonth = "le \\d?\\d".toRegex()
    private val frenchChangeDayInWeek = "(le )?(lundi|mardi|mercredi|jeudi|vendredi|samedi|dimanche)".toRegex()

    private val parser: Parser by injector.instance()

    data class MergeGrain(val additional: Boolean, val grain: DateEntityGrain)

    private fun ValueDescriptor.start(zoneId: ZoneId = defaultZoneId): ZonedDateTime =
        (value as DateEntityRange).start().withZoneSameInstant(zoneId)

    private fun ValueDescriptor.end(zoneId: ZoneId = defaultZoneId): ZonedDateTime =
        (value as DateEntityRange).end().withZoneSameInstant(zoneId)

    private fun ValueDescriptor.grain(): DateEntityGrain =
        value.run {
            when (this) {
                is DateEntityValue -> grain
                is DateIntervalEntityValue -> date.grain
                else -> error("unsupported value $this")
            }
        }

    private fun ValueDescriptor.grainFromNow(): DateEntityGrain =
        DateEntityGrain.maxGrain(now(), start())

    fun merge(context: EntityCallContextForEntity, values: List<ValueDescriptor>): ValueDescriptor? {
        return if (context.entityType.name != DucklingDimensions.datetimeEntityType) {
            logger.warn { "merge not supported for $context" }
            null
        } else {
            val concatenated = concatEntityValues(context.language, context.referenceDate, values)
            val initial = values.firstOrNull { it.initial }
            if (initial == null) concatenated
            else mergeDateEntityValue(context.language, context.referenceDate, initial, concatenated)
        }
    }

    private fun concatEntityValues(
        language: Locale,
        referenceDateTime: ZonedDateTime,
        values: List<ValueDescriptor>
    ): ValueDescriptor {
        return values.filter { !it.initial }
            .run {
                when (size) {
                    0 -> error("at least one non initial value should be present")
                    1 -> first()
                    else -> {
                        val differentGrain = map { it.grain() }.distinct().size == size
                        if (differentGrain) {
                            parseDate(
                                language,
                                referenceDateTime,
                                sortedBy { it.position }.map { it.content }.joinToString(" ")
                            )
                                ?: maxByOrNull { it.probability }!!
                        } else {
                            maxByOrNull { it.probability }!!
                        }
                    }
                }
            }
    }

    private fun mergeDateEntityValue(
        language: Locale,
        referenceDateTime: ZonedDateTime,
        oldValue: ValueDescriptor,
        newValue: ValueDescriptor
    ): ValueDescriptor {
        try {
            val zoneId = referenceDateTime.zone
            if (hasToChangeDayInMonth(language, newValue)) {
                val newValueContent = normalize(newValue.content!!)
                oldValue.start(zoneId).apply {
                    val newResult = ValueDescriptor(
                        DateEntityValue(
                            ZonedDateTime.of(
                                year,
                                monthValue,
                                newValueContent.substring("le ".length).toInt(),
                                0,
                                0,
                                0,
                                0,
                                zone
                            ),
                            day
                        ),
                        newValue.content
                    )
                    if (newResult.start(zoneId).truncatedTo(DAYS) >= referenceDateTime.truncatedTo(DAYS)) {
                        return newResult
                    }
                }
            }
            if (hasToChangeDayInWeek(language, newValue)) {
                val newValueContent = normalize(newValue.content!!)
                oldValue.start(zoneId).apply {
                    val oldDayOfWeek = dayOfWeek.value
                    val newDayOfWeek = when {
                        newValueContent.contains("lundi") -> 1
                        newValueContent.contains("mardi") -> 2
                        newValueContent.contains("mercredi") -> 3
                        newValueContent.contains("jeudi") -> 4
                        newValueContent.contains("vendredi") -> 5
                        newValueContent.contains("samedi") -> 6
                        newValueContent.contains("dimanche") -> 7
                        else -> oldDayOfWeek
                    }

                    val previous = DateEntityValue(
                        oldValue
                            .start(zoneId)
                            .with(TemporalAdjusters.previous(DayOfWeek.of(newDayOfWeek)))
                            .truncatedTo(DAYS),
                        day
                    )
                    val newResult = ValueDescriptor(
                        if (oldDayOfWeek == newDayOfWeek)
                            DateEntityValue(
                                oldValue
                                    .start(zoneId)
                                    .truncatedTo(DAYS),
                                day
                            )
                        else if (oldDayOfWeek < newDayOfWeek || newDayOfWeek == 7 || previous.start().withZoneSameInstant(zoneId).truncatedTo(DAYS) < referenceDateTime.truncatedTo(DAYS))
                            DateEntityValue(
                                oldValue
                                    .start(zoneId)
                                    .with(TemporalAdjusters.next(DayOfWeek.of(newDayOfWeek)))
                                    .truncatedTo(DAYS),
                                day
                            )
                        else previous,
                        newValue.content
                    )

                    if (newResult.start(zoneId).truncatedTo(DAYS) >= referenceDateTime.truncatedTo(DAYS)) {
                        return newResult
                    }
                }
            }
            val mergeGrain = hasToAdd(language, newValue) ?: mergeGrain(language, oldValue, newValue)
            if (mergeGrain != null) {
                return parseDate(language, referenceDateTime, oldValue, newValue, mergeGrain)
            }
        } catch (e: Exception) {
            logger.error(e)
        }

        return newValue
    }

    private fun normalize(s: String): String = s.trim().replace(removeDuplicateSpaceRegexp, " ").lowercase()

    fun isChangeHourPattern(content: String?): Boolean =
        content != null && frenchChangeHourRegex.matches(normalize(content))

    fun mergeGrain(language: Locale, oldValue: ValueDescriptor, newValue: ValueDescriptor): MergeGrain? {
        return if (oldValue.end() < now()) {
            null
        } else if (language.language == "fr" && isChangeHourPattern(newValue.content)) {
            MergeGrain(false, day)
        } else if (oldValue.grain() > newValue.grain() &&
            oldValue.grain().calculateEnd(newValue.start(), defaultZoneId) >= newValue.end()
        ) {
            // MergeGrain(false, oldValue.grainFromNow())
            null
        } else {
            null
        }
    }

    private fun hasToChangeDayInMonth(language: Locale, newValue: ValueDescriptor): Boolean {
        return language.language == "fr" &&
            newValue.content != null &&
            frenchChangeDayInMonth.matches(normalize(newValue.content!!))
    }

    private fun hasToChangeDayInWeek(language: Locale, newValue: ValueDescriptor): Boolean {
        return language.language == "fr" &&
            newValue.content != null &&
            frenchChangeDayInWeek.matches(normalize(newValue.content!!))
    }

    private fun hasToAdd(language: Locale, newValue: ValueDescriptor): MergeGrain? {
        val basicSupport = if (language.language != "fr") {
            logger.warn { "only fr supported for add merge" }
            false
        } else {
            true
        }
        return if (basicSupport && newValue.content != null && frenchAddRegex.matches(normalize(newValue.content!!))) {
            MergeGrain(true, newValue.grain())
        } else {
            null
        }
    }

    private fun parseDate(
        language: Locale,
        referenceDateTime: ZonedDateTime,
        oldValue: ValueDescriptor,
        newValue: ValueDescriptor,
        mergeGrain: MergeGrain
    ): ValueDescriptor {
        fun ValueDescriptor.startWithZone(): ZonedDateTime = start().withZoneSameInstant(referenceDateTime.zone)

        val dateText = newValue.content
        val start = oldValue.startWithZone()
        return if (dateText != null) {
            val referenceDate = if (mergeGrain.additional) {
                start
            } else {
                mergeGrain.grain.truncate(start)
            }
            parseDate(language, referenceDate, dateText) ?: newValue
        } else {
            newValue
        }
    }

    private fun parseDate(language: Locale, referenceDateTime: ZonedDateTime, text: String): ValueDescriptor? {
        return parser.parse(language.language, DucklingDimensions.TIME_DIMENSION, referenceDateTime, text)
            .firstOrNull()
            ?.run { ValueDescriptor(value, text) }
    }
}
