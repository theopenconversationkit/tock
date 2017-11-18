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

package fr.vsct.tock.nlp.front.service.storage

import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.nlp.front.shared.test.EntityTestError
import fr.vsct.tock.nlp.front.shared.test.EntityTestErrorQueryResult
import fr.vsct.tock.nlp.front.shared.test.IntentTestError
import fr.vsct.tock.nlp.front.shared.test.IntentTestErrorQueryResult
import fr.vsct.tock.nlp.front.shared.test.TestBuild
import fr.vsct.tock.nlp.front.shared.test.TestErrorQuery
import org.litote.kmongo.Id
import java.util.Locale

/**
 *
 */
interface TestModelDAO {

    fun getTestBuilds(applicationId: Id<ApplicationDefinition>, language: Locale): List<TestBuild>

    fun saveTestBuild(build: TestBuild)

    fun searchTestIntentErrors(query: TestErrorQuery): IntentTestErrorQueryResult

    fun addTestIntentError(intentError: IntentTestError)

    fun deleteTestIntentError(applicationId: Id<ApplicationDefinition>, language: Locale, text: String)

    fun searchTestEntityErrors(query: TestErrorQuery): EntityTestErrorQueryResult

    fun addTestEntityError(entityError: EntityTestError)

    fun deleteTestEntityError(applicationId: Id<ApplicationDefinition>, language: Locale, text: String)

}