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

package ai.tock.duckling.service

import clojure.java.api.Clojure
import clojure.lang.Keyword
import clojure.lang.PersistentArrayMap
import clojure.lang.PersistentVector
import clojure.lang.RT
import duckling.`core$load_BANG_`
import duckling.`core$parse`
import mu.KotlinLogging
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.time.ZoneId
import java.time.ZoneId.systemDefault
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now
import java.util.Collections
import java.util.HashMap

/**
 *
 */
internal object DucklingBridge {

    private val logger = KotlinLogging.logger {}

    private lateinit var referenceTime: Keyword
    private lateinit var start: Keyword
    private lateinit var grain: Keyword
    private lateinit var second: Keyword
    @Volatile
    var initialized = false

    private fun keyword(name: String): Keyword = RT.keyword(null as String?, name)
    private fun find(name: String): Keyword? = Keyword.find(null, name)

    fun initDuckling() {
        val require = Clojure.`var`("clojure.core", "require")
        require.invoke(Clojure.read("duckling.core"))
        val l = `core$load_BANG_`()
        l.invoke()

        referenceTime = keyword("reference-time")
        start = keyword("start")
        grain = keyword("grain")
        second = keyword("second")

        parse("en", "tomorrow", listOf("time"), now(), systemDefault())
        initialized = true
    }

    fun parse(
        language: String,
        textToParse: String,
        dimensions: List<String>,
        referenceDate: ZonedDateTime,
        referenceTimezone: ZoneId
    ): Any {
        val dateMap = HashMap<Keyword?, Any?>()
        // set timezone for wit
        val timezone = try {
            if (referenceTimezone.id == "Z") DateTimeZone.UTC else DateTimeZone.forID(referenceTimezone.id)
        } catch (e: Exception) {
            logger.warn { "unrecognized timezone $referenceTimezone - use UTC" }
            DateTimeZone.UTC
        }
        dateMap.put(
            start,
            DateTime(referenceDate.toInstant().toEpochMilli(), timezone)
        )
        dateMap.put(grain, second)
        val dateClosureMap = PersistentArrayMap.create(dateMap)
        val context = PersistentArrayMap.create(Collections.singletonMap(referenceTime, dateClosureMap))
        val dims = PersistentVector.create(dimensions.map { find(it) })

        val c = `core$parse`()
        return c.invoke(language + "\$core", textToParse, dims, context)
    }
}
