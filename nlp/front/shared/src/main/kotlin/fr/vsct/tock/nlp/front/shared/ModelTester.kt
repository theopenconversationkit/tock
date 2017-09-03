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

package fr.vsct.tock.nlp.front.shared

import fr.vsct.tock.nlp.front.shared.test.EntityTestErrorQueryResult
import fr.vsct.tock.nlp.front.shared.test.IntentTestErrorQueryResult
import fr.vsct.tock.nlp.front.shared.test.TestBuild
import fr.vsct.tock.nlp.front.shared.test.TestErrorQuery
import java.util.Locale

/**
 *
 */
interface ModelTester {

    fun testModels()

    fun searchTestIntentErrors(query: TestErrorQuery): IntentTestErrorQueryResult

    fun searchTestEntityErrors(query: TestErrorQuery): EntityTestErrorQueryResult

    fun deleteTestIntentError(applicationId: String, language: Locale, text: String)

    fun deleteTestEntityError(applicationId: String, language: Locale, text: String)

    fun getTestBuilds(applicationId: String, language: Locale): List<TestBuild>
}