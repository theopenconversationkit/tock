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

enum class Dimensions(val value: String) {
    GEN_AI("Gen AI"),
}

enum class IndicatorValues(val value: IndicatorValue) {
    SUCCESS(IndicatorValue(name = "success", label = "SUCCESS")),
    FAILURE(IndicatorValue(name = "failure", label = "FAILURE")),
    NO_ANSWER(IndicatorValue(name = "no answer", label = "NO ANSWER")),
}

enum class Indicators(val value: Indicator) {
    RAG(
        Indicator(
            name = "rag",
            label = "RAG",
            description = "Predefined indicator for the RAG Story.",
            // A predefined indicator does not have a namespace.
            namespace = "",
            // A predefined indicator does not have a botId.
            botId = "",
            dimensions = setOf(Dimensions.GEN_AI.value),
            values =
                setOf(
                    IndicatorValues.SUCCESS.value,
                    IndicatorValues.FAILURE.value,
                    IndicatorValues.NO_ANSWER.value,
                ),
        ),
    ),
}

object PredefinedIndicators {
    val indicators = Indicators.entries.map { it.value }.toSet()

    fun has(name: String) = indicators.any { it.name.equals(name, ignoreCase = true) }
}
