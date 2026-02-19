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

package ai.tock.bot.admin.indicators

sealed class IndicatorError(override val message: String) : Exception(message) {
    class IndicatorAlreadyExists(val name: String, val label: String, val namespace: String, val botId: String) :
        IndicatorError("An indicator with name '$name' or label '$label' already exists for bot '$botId'.")

    class IndicatorNotFound(val name: String, val namespace: String, val botId: String) :
        IndicatorError("Indicator '$name' not found for bot '$botId'.")

    class IndicatorDeletionFailed(val name: String, val namespace: String, val botId: String) :
        IndicatorError("Failed to delete indicator '$name' for bot '$botId'.")

    class IndicatorUnauthorizedUpdate(val name: String, val namespace: String, val botId: String) :
        IndicatorError("Failed to update indicator '$name' for bot '$botId'.")
}
