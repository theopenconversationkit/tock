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

package ai.tock.nlp.front.shared

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import ai.tock.nlp.front.shared.test.EntityTestErrorQueryResult
import ai.tock.nlp.front.shared.test.IntentTestErrorQueryResult
import ai.tock.nlp.front.shared.test.TestBuild
import ai.tock.nlp.front.shared.test.TestErrorQuery
import org.litote.kmongo.Id
import java.util.Locale

/**
 *
 */
interface ModelTester {

    fun testModels()

    fun searchTestIntentErrors(query: TestErrorQuery): IntentTestErrorQueryResult

    fun searchTestEntityErrors(query: TestErrorQuery): EntityTestErrorQueryResult

    fun deleteTestIntentError(applicationId: Id<ApplicationDefinition>, language: Locale, text: String)

    fun deleteTestEntityError(applicationId: Id<ApplicationDefinition>, language: Locale, text: String)

    fun getTestBuilds(query: TestErrorQuery): List<TestBuild>
}
