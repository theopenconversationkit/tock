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
import org.litote.kmongo.Id
import org.litote.kmongo.deleteMany
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.find
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.json
import org.litote.kmongo.save
import org.litote.kmongo.sort

/**
 *
 */
internal object TestPlanMongoDAO : TestPlanDAO {

    private val testPlanCol = MongoBotConfiguration.database.getCollection<TestPlan>()
    private val testPlanExecutionCol = MongoBotConfiguration.database.getCollection<TestPlanExecution>()

    init {
        testPlanCol.ensureIndex("{applicationId:1}")
    }

    override fun save(testPlan: TestPlan) {
        testPlanCol.save(testPlan)
    }

    override fun removeTestPlan(planId: Id<TestPlan>) {
        testPlanCol.deleteOneById(planId)
        testPlanExecutionCol.deleteMany("{'testPlanId':${planId.json}}")
    }

    override fun save(testPlan: TestPlanExecution) {
        testPlanExecutionCol.save(testPlan)
    }

    override fun getPlan(testPlanId: Id<TestPlan>): TestPlan? {
        return testPlanCol.findOneById(testPlanId)
    }

    override fun getPlansByApplicationId(applicationId: String): List<TestPlan> {
        return testPlanCol.find("{applicationId:${applicationId.json}}").sort("{name:1}").toList()
    }

    override fun getPlans(): List<TestPlan> {
        return testPlanCol.find().sort("{name:1}").toList()
    }

    override fun getPlanExecutions(testPlanId: Id<TestPlan>): List<TestPlanExecution> {
        return testPlanExecutionCol.find("{'testPlanId':${testPlanId.json}}").sort("{date:-1}").toList()
    }
}