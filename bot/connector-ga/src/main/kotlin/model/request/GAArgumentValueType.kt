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

package ai.tock.bot.connector.ga.model.request

import com.fasterxml.jackson.annotation.JsonValue

@Suppress("ktlint:standard:enum-entry-name-case")
enum class GAArgumentValueType(
    @get:JsonValue val value: String,
) {
    transactionRequirementsCheckResult("type.googleapis.com/google.actions.v2.TransactionRequirementsCheckResult"),
    transactionRequirementsCheckResultV3("type.googleapis.com/google.actions.transactions.v3.TransactionRequirementsCheckResult"),
    transactionDecisionValue("type.googleapis.com/google.actions.v2.TransactionDecisionValue"),
    holdValue("type.googleapis.com/google.actions.v2.HoldValue"),
    signInValue("type.googleapis.com/google.actions.v2.SignInValue"),
    newSurfaceValue("type.googleapis.com/google.actions.v2.NewSurfaceValue"),
    mediaStatus("type.googleapis.com/google.actions.v2.MediaStatus"),
}
