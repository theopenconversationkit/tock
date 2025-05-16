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

import org.litote.kmongo.Id

/**
 *
 */
interface TestPlanDAO {

    /**
     * Save the given common test plan into the mongo database.
     *
     * @param testPlan is the test plan to save.
     */
    fun saveTestPlan(testPlan: TestPlan)

    /**
     * Remove the given test plan from the database.
     *
     * @param planId is the identifier of the common test plan to remove from the database.
     */
    fun removeTestPlan(planId: Id<TestPlan>)

    /**
     * Save the given common test plan execution into the mongo database.
     *
     * @param testPlanExecution is the test plan execution to save.
     */
    fun saveTestExecution(testPlanExecution: TestPlanExecution)

    fun getTestPlans(): List<TestPlan>

    fun getTestPlan(testPlanId: Id<TestPlan>): TestPlan?

    fun getPlansByApplicationId(applicationId: String): List<TestPlan>

    fun getPlanExecutions(testPlanId: Id<TestPlan>): List<TestPlanExecution>

    fun getPlanExecution(testPlanId: Id<TestPlan>): TestPlanExecution?

    fun getTestPlanExecution(testPlan: TestPlan, testPlanExecutionId: Id<TestPlanExecution>): TestPlanExecution?
}
