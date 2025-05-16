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

package ai.tock.nlp.front.storage.mongo

import ai.tock.nlp.core.Entity
import ai.tock.nlp.core.EntityType
import ai.tock.nlp.entity.NumberValue
import ai.tock.nlp.front.shared.monitoring.ParseRequestLog
import ai.tock.nlp.front.shared.parser.ParseQuery
import ai.tock.nlp.front.shared.parser.ParseResult
import ai.tock.nlp.front.shared.parser.ParsedEntityValue
import ai.tock.nlp.front.shared.parser.QueryContext
import ai.tock.nlp.front.storage.mongo.ParseRequestLogMongoDAO.ParseRequestLogIntentStatCol
import ai.tock.nlp.front.storage.mongo.ParseRequestLogMongoDAO.ParseRequestLogStatCol
import ai.tock.shared.Dice
import ai.tock.shared.defaultLocale
import com.mongodb.ReadPreference.primary
import com.mongodb.client.MongoCollection
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.findOne
import org.litote.kmongo.toId
import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 *
 */
internal class ParseRequestLogMongoDAOTest : AbstractTest() {

    private val parseResult = ParseResult(
        "test",
        "namespace",
        defaultLocale,
        listOf(
            ParsedEntityValue(
                0,
                1,
                Entity(EntityType("type"), "role"),
                NumberValue(1)
            )
        ),
        emptyList(),
        1.0,
        1.0,
        "sentence",
        mapOf("app:test2" to 0.4)
    )

    private val log = ParseRequestLog(
        "a".toId(),
        ParseQuery(
            listOf("1"), "namespace", "appName",
            QueryContext(
                defaultLocale,
                Dice.newId(),
                referenceDate = ZonedDateTime.now(UTC).truncatedTo(ChronoUnit.MILLIS)
            )
        , configuration = null
        ),
        parseResult,
        2,
        date = Instant.now().truncatedTo(ChronoUnit.MILLIS)
    )

    private val log2 = log.copy(result = parseResult.copy(otherIntentsProbabilities = mapOf("app:test2" to 0.5)))

    private val log3 = log.copy(result = parseResult.copy(otherIntentsProbabilities = emptyMap()))

    val col: MongoCollection<ParseRequestLogMongoDAO.ParseRequestLogCol> by lazy { ParseRequestLogMongoDAO.col }
    val statsCol: MongoCollection<ParseRequestLogStatCol> by lazy { ParseRequestLogMongoDAO.statsCol }
    val intentStatsCol: MongoCollection<ParseRequestLogIntentStatCol> by lazy { ParseRequestLogMongoDAO.intentStatsCol }

    @BeforeEach
    fun clearCols() {
        col.drop()
        statsCol.drop()
        intentStatsCol.drop()
    }

    @Test
    fun `save persists log and create a stat if one does not exist`() {
        ParseRequestLogMongoDAO.save(log)
        assertEquals(log, col.withReadPreference(primary()).findOne()?.toRequest())
        assertEquals(ParseRequestLogStatCol(log), statsCol.findOne())
    }

    @Test
    fun `save persists log and increment a stat if one already exists`() {
        ParseRequestLogMongoDAO.save(log)
        assertEquals(log, col.withReadPreference(primary()).findOne()?.toRequest())
        assertEquals(ParseRequestLogStatCol(log), statsCol.findOne())
        ParseRequestLogMongoDAO.save(log)
        assertEquals(1, statsCol.countDocuments())
        assertEquals(ParseRequestLogStatCol(log).copy(count = 2), statsCol.findOne())
    }

    @Test
    fun `GIVEN log contains secondary intent WHEN save log THEN create new intent log`() {
        ParseRequestLogMongoDAO.save(log)
        val intentStatCol = intentStatsCol.findOne()
        assertNotNull(intentStatCol)
        assertEquals(intentStatCol.intent1, "test")
        assertEquals(intentStatCol.intent2, "test2")
        assertEquals(intentStatCol.averageDiff, 0.6)
        assertEquals(intentStatCol.count, 1)
    }

    @Test
    fun `GIVEN log contains secondary intent already saved WHEN save log THEN update existing intent log`() {
        ParseRequestLogMongoDAO.save(log)
        ParseRequestLogMongoDAO.save(log2)
        val intentStatCol2 = intentStatsCol.findOne()
        assertNotNull(intentStatCol2)
        assertEquals(intentStatCol2.averageDiff, (0.6 + 0.5) / 2)
        assertEquals(intentStatCol2.count, 2)
    }

    @Test
    fun `GIVEN log does not contains secondary intent WHEN save log THEN don't save intent log`() {
        ParseRequestLogMongoDAO.save(log3)
        assertNull(intentStatsCol.findOne())
    }
}
