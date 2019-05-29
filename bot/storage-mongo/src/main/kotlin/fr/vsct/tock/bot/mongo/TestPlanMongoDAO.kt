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

package fr.vsct.tock.bot.mongo

import fr.vsct.tock.bot.admin.test.TestPlan
import fr.vsct.tock.bot.admin.test.TestPlanDAO
import fr.vsct.tock.bot.admin.test.TestPlanExecution
import fr.vsct.tock.bot.admin.test.TestPlanExecution_.Companion.Date
import fr.vsct.tock.bot.admin.test.TestPlanExecution_.Companion.TestPlanId
import fr.vsct.tock.bot.admin.test.TestPlan_.Companion.ApplicationId
import fr.vsct.tock.bot.admin.test.TestPlan_.Companion.Name
import org.litote.kmongo.Id
import org.litote.kmongo.ascendingSort
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.descendingSort
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.eq
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.save

/**
 *
 */
internal object TestPlanMongoDAO : TestPlanDAO {

    private val testPlanCol = MongoBotConfiguration.database.getCollection<TestPlan>()
    private val testPlanExecutionCol = MongoBotConfiguration.database.getCollection<TestPlanExecution>()

    init {
        testPlanCol.ensureIndex(ApplicationId)
    }

    /**
     * Save the given common test plan into the mongo database.
     *
     * @param testPlan is the test plan to save.
     */
    override fun saveTestPlan(testPlan: TestPlan) {
        testPlanCol.save(testPlan)
    }

    override fun removeTestPlan(planId: Id<TestPlan>) {
        testPlanCol.deleteOneById(planId)
        testPlanExecutionCol.deleteMany(TestPlanId eq planId)
    }

    /**
     * Save the given common test plan execution into the mongo database.
     *
     * @param testPlanExecution is the test plan execution to save.
     */
    override fun saveTestExecution(testPlanExecution: TestPlanExecution) {
        testPlanExecutionCol.save(testPlanExecution)
    }

    override fun getPlan(testPlanId: Id<TestPlan>): TestPlan? {
        return testPlanCol.findOneById(testPlanId)
    }

    override fun getPlansByApplicationId(applicationId: String): List<TestPlan> {
        return testPlanCol.find(ApplicationId eq applicationId).ascendingSort(Name).toList()
    }

    override fun getPlans(): List<TestPlan> {
        return testPlanCol.find().ascendingSort(Name).toList()
    }

    override fun getPlanExecutions(testPlanId: Id<TestPlan>): List<TestPlanExecution> {
        return testPlanExecutionCol.find(TestPlanId eq testPlanId).descendingSort(Date).toList()
    }
}