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
    class IndicatorAlreadyExists(val name: String, val label: String, val applicationName: String) :
        IndicatorError("Indicator already exists for bot $applicationName with following name: $name or/and label: $label")

    class IndicatorNotFound(val name: String, val applicationName: String) :
        IndicatorError("Indicator $name not found for bot $applicationName")
    class IndicatorDeletionFailed(val name: String, val applicationName: String) :
        IndicatorError("Indicator $name for bot $applicationName deletion failed")

}
