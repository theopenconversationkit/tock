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
import org.litote.kmongo.newId
import java.time.Duration
import java.time.Instant

/**
 * An execution of a [TestPlan], with its result.
 */
data class TestPlanExecution(
    /**
     * The test plan identifier.
     */
    val testPlanId: Id<TestPlan>,
    /**
     * The dialog execution test reports.
     */
    val dialogs: List<DialogExecutionReport>,
    /**
     * Number of errors, if any.
     */
    val nbErrors: Int,
    /**
     * Date of the execution.
     */
    val date: Instant = Instant.now(),
    /**
     * Duration of the execution.
     */
    val duration: Duration = Duration.ZERO,
    /**
     * The execution identifier.
     */
    val _id: Id<TestPlanExecution> = newId(),
    /**
     * The status of the test plan execution
     */
    val status: TestPlanExecutionStatus = TestPlanExecutionStatus.PENDING,
)
