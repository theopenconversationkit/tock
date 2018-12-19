/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.duckling.client

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.nlp.core.merge.ValueDescriptor
import fr.vsct.tock.nlp.entity.date.DateEntityGrain
import fr.vsct.tock.nlp.entity.date.DateEntityGrain.day
import fr.vsct.tock.nlp.entity.date.DateEntityRange
import fr.vsct.tock.nlp.entity.date.DateEntityValue
import fr.vsct.tock.nlp.entity.date.DateIntervalEntityValue
import fr.vsct.tock.nlp.model.EntityCallContextForEntity
import fr.vsct.tock.shared.defaultZoneId
import fr.vsct.tock.shared.error
import fr.vsct.tock.shared.injector
import mu.KotlinLogging
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * For now only [java.util.Locale.FRENCH] is supported for "additional" merge, with a *very* hacky support.
 * Better support is on the way.
 */
internal object DatesMerge {

    private val logger = KotlinLogging.logger {}

    private val remodeDuplicateSpaceRegexp = "\\s+".toRegex()
    private val frenchAddRegex =
        ".*prochaine?$|.*suivante?$|.*qui suit$|.*(d')? ?apr[eèé]s$|.*plus tard$|.*derni[èe]re?$|.*pass[ée]e?$|.*pr[eé]c[eé]dente?$|.*(d')? ?avant$|.*plus t[oô]t$|lendemain|le lendemain|la veille|ce jour|(le |la )?m[eê]me jour(n[eé]e)?".toRegex()
    private val frenchChangeHourRegex =
        ("(dans )?(le |la |en |(en )?fin de |(en )?d[ée]but de |(en )?milieu de )?soir[ée]?e?" +
                "|(dans )?(le |la |en |(en )?fin de |(en )?d[ée]but de |(en )?milieu de )?mat(in[ée]?e?)?" +
                "|(dans )?(l. ?|(en )?fin d. ?|(en )?d[ée]but d. ?|(en )?milieu d. ?)?apr[eéè](s?[ \\-]?midi|m)" +
                "|([aà]|vers|apr(e|è)s|[aà] partir de|avant|jusqu'[aà])? ?((([01]?\\d)|(2[0-3]))([:h]|heures?)?([0-5]\\d)?)(du|dans l[ae']? ?|au|en|l[ae'] ?|dès l?[ae']? ?|(en )?d[ée]but (de |d' ?)|(en )?fin (de |d' ?)|(en )?d[ée]but (d' ?|de ))?(mat(in[ée]?e?)|soir[ée]?e?|apr[eéè]s?[ \\-]?midi|journ[ée]e)?").toRegex()
    private val frenchChangeDayInMonth = "le \\d?\\d".toRegex()
    private val frenchChangeDayInWeek = "(le )?(lundi|mardi|mercredi|jeudi|vendredi|samedi|dimanche)".toRegex()

    private val parser: Parser by injector.instance()

    data class MergeGrain(val additional: Boolean, val grain: DateEntityGrain)

    private fun ValueDescriptor.start(): ZonedDateTime = (value as DateEntityRange).start()
    private fun ValueDescriptor.end(): ZonedDateTime = (value as DateEntityRange).end()
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
            logger.warn { "merge not supported for ${context}" }
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
                                    ?: maxBy { it.probability }!!
                        } else {
                            maxBy { it.probability }!!
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
            if (hasToChangeDayInMonth(language, newValue)) {
                val newValueContent = normalize(newValue.content!!)
                return oldValue.start().run {
                    ValueDescriptor(
                        DateEntityValue(
                            ZonedDateTime.of(
                                year,
                                monthValue,
                                newValueContent.substring("le ".length).toInt(),
                                0,
                                0
                                ,
                                0,
                                0,
                                zone
                            ),
                            day
                        ),
                        oldValue.content ?: newValueContent
                    )
                }
            }
            if (hasToChangeDayInWeek(language, newValue)) {
                val newValueContent = normalize(newValue.content!!)
                return oldValue.start().run {
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

                    ValueDescriptor(
                        if (oldDayOfWeek == newDayOfWeek) oldValue.value
                        else if (oldDayOfWeek < newDayOfWeek || newDayOfWeek == 7)
                            DateEntityValue(
                                oldValue.start().with(TemporalAdjusters.next(DayOfWeek.of(newDayOfWeek))),
                                day
                            )
                        else
                            DateEntityValue(
                                oldValue.start().with(TemporalAdjusters.previous(DayOfWeek.of(newDayOfWeek))),
                                day
                            )
                        ,
                        oldValue.content ?: newValueContent
                    )
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

    private fun normalize(s: String): String = s.trim().replace(remodeDuplicateSpaceRegexp, " ").toLowerCase()

    fun mergeGrain(language: Locale, oldValue: ValueDescriptor, newValue: ValueDescriptor): MergeGrain? {
        return if (oldValue.end() < ZonedDateTime.now()) {
            null
        } else if (language.language == "fr" && frenchChangeHourRegex.matches(normalize(newValue.content!!))) {
            MergeGrain(false, day)
        } else if (oldValue.grain() > newValue.grain()
            && oldValue.grain().calculateEnd(newValue.start(), defaultZoneId) >= newValue.end()
        ) {
            //MergeGrain(false, oldValue.grainFromNow())
            null
        } else {
            null
        }
    }

    private fun hasToChangeDayInMonth(language: Locale, newValue: ValueDescriptor): Boolean {
        return language.language == "fr"
                && newValue.content != null
                && frenchChangeDayInMonth.matches(normalize(newValue.content!!))
    }

    private fun hasToChangeDayInWeek(language: Locale, newValue: ValueDescriptor): Boolean {
        return language.language == "fr"
                && newValue.content != null
                && frenchChangeDayInWeek.matches(normalize(newValue.content!!))
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
        val dateText = newValue.content
        val start = oldValue.start().withZoneSameInstant(referenceDateTime.zone)
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
        return parser.parse(language.language, DucklingDimensions.timeDucklingDimension, referenceDateTime, text)
            .firstOrNull()
            ?.run { ValueDescriptor(value, text) }
    }

}