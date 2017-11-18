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

package fr.vsct.tock.bot.admin.test

import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.time.Duration
import java.time.Instant

/**
 *
 */
data class TestPlanExecution(
        val testPlanId: Id<TestPlan>,
        val dialogs: List<DialogExecutionReport>,
        val nbErrors: Int,
        val date: Instant = Instant.now(),
        val duration: Duration = Duration.ZERO,
        val _id: Id<TestPlanExecution> = newId()) {
}