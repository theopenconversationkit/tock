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

package ai.tock.bot.admin.test

import ai.tock.nlp.admin.AdminVerticle
import ai.tock.shared.Loader
import org.litote.kmongo.Id

/**
 * Retrieve a test service from [ServiceLoader].
 */
fun findTestService(): TestService =
    Loader.loadServices<TestService>().maxByOrNull { it.priority() } ?: error("no test service found")

interface TestService {

    fun registerServices(): (AdminVerticle).() -> Unit

    fun priority(): Int

    /**
     * This function saves the current test plan in the mongo database and
     * executes all test contained in the common test plan.
     *
     */
    fun saveAndExecuteTestPlan(namespace: String, testPlan: TestPlan, executionId: Id<TestPlanExecution>): TestPlanExecution
}
