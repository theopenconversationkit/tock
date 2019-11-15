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

package ai.tock.bot.admin.test.xray.model

import java.time.OffsetDateTime

/**
 *
 */
data class XrayTestExecutionInfo(
        val summary: String,
        val description: String,
        val startDate: OffsetDateTime,
        val finishDate: OffsetDateTime,
        val testPlanKey: String,
        val testEnvironments: List<String> = listOf("")
) {
}