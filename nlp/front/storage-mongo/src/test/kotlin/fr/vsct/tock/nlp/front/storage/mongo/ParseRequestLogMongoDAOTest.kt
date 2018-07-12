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

package fr.vsct.tock.nlp.front.storage.mongo

import com.mongodb.client.MongoCollection
import fr.vsct.tock.nlp.core.Entity
import fr.vsct.tock.nlp.core.EntityType
import fr.vsct.tock.nlp.entity.NumberValue
import fr.vsct.tock.nlp.front.shared.monitoring.ParseRequestLog
import fr.vsct.tock.nlp.front.shared.parser.ParseQuery
import fr.vsct.tock.nlp.front.shared.parser.ParseResult
import fr.vsct.tock.nlp.front.shared.parser.ParsedEntityValue
import fr.vsct.tock.nlp.front.shared.parser.QueryContext
import fr.vsct.tock.nlp.front.storage.mongo.ParseRequestLogMongoDAO.ParseRequestLogStatCol
import fr.vsct.tock.shared.Dice
import fr.vsct.tock.shared.defaultLocale
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.findOne
import org.litote.kmongo.toId
import kotlin.test.assertEquals

/**
 *
 */
class ParseRequestLogMongoDAOTest : AbstractTest() {

    private val log = ParseRequestLog(
        "a".toId(),
        ParseQuery(listOf("1"), "namespace", "appName", QueryContext(defaultLocale, Dice.newId())),
        ParseResult(
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
            mapOf("test2" to 2.0)
        ),
        2
    )

    val col: MongoCollection<ParseRequestLogMongoDAO.ParseRequestLogCol> by lazy { ParseRequestLogMongoDAO.col }
    val statsCol: MongoCollection<ParseRequestLogStatCol> by lazy { ParseRequestLogMongoDAO.statsCol }

    @BeforeEach
    fun clearCols() {
        col.drop()
        statsCol.drop()
    }

    @Test
    fun `save persists log and create a stat if one does not exist`() {
        ParseRequestLogMongoDAO.save(log)
        assertEquals(log, col.findOne()?.toRequest())
        assertEquals(ParseRequestLogStatCol(log), statsCol.findOne())
    }

    @Test
    fun `save persists log and increment a stat if one already exists`() {
        ParseRequestLogMongoDAO.save(log)
        assertEquals(log, col.findOne()?.toRequest())
        assertEquals(ParseRequestLogStatCol(log), statsCol.findOne())
        ParseRequestLogMongoDAO.save(log)
        assertEquals(1, statsCol.count())
        assertEquals(ParseRequestLogStatCol(log).copy(count = 2), statsCol.findOne())
    }
}