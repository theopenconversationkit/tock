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

package ai.tock.bot.connector.ga.model

import com.fasterxml.jackson.annotation.JsonValue

enum class GAInputValueDataType(@get:JsonValue val value: String) {
    option("type.googleapis.com/google.actions.v2.OptionValueSpec"),
    confirmation("type.googleapis.com/google.actions.v2.ConfirmationValueSpec"),
    transactionRequirementsCheckV3("type.googleapis.com/google.actions.transactions.v3.TransactionRequirementsCheckSpec"),
    deliveryAddress("type.googleapis.com/google.actions.v2.DeliveryAddressValueSpec"),
    transactionDecisionV3("type.googleapis.com/google.actions.transactions.v3.TransactionDecisionValueSpec"),
    permission("type.googleapis.com/google.actions.v2.PermissionValueSpec"),
    datetime("type.googleapis.com/google.actions.v2.DateTimeValueSpec"),
    newSurface("type.googleapis.com/google.actions.v2.NewSurfaceValueSpec"),
    mediaStatus("type.googleapis.com/google.actions.v2.MediaStatus")
}
