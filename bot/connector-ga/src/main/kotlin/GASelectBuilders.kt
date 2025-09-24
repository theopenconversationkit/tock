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

package ai.tock.bot.connector.ga

import ai.tock.bot.connector.ga.model.GAIntent
import ai.tock.bot.connector.ga.model.response.GAExpectedIntent
import ai.tock.bot.connector.ga.model.response.GASelectItem
import ai.tock.bot.connector.ga.model.response.GASimpleSelect
import ai.tock.bot.definition.IntentAware
import ai.tock.bot.definition.Parameters
import ai.tock.bot.definition.StoryStep
import ai.tock.bot.definition.StoryStepDef
import ai.tock.bot.engine.Bus
import ai.tock.bot.engine.I18nTranslator

/**
 * Provides a [GAExpectedIntent] with a [GASimpleSelect].
 */
fun I18nTranslator.expectedIntentForSimpleSelect(items: List<GASelectItem>): GAExpectedIntent {
    return GAExpectedIntent(
        GAIntent.option,
        optionValueSpec(
            simpleSelect = GASimpleSelect(items)
        )
    )
}

/**
 * Provides a [GASelectItem] with [String] parameters.
 */
fun <T : Bus<T>> T.selectItem(
    title: CharSequence,
    targetIntent: IntentAware,
    vararg parameters: Pair<String, String>
): GASelectItem = selectItem<T>(title, targetIntent, null, null, *parameters)

/**
 * Provides a [GASelectItem] with option title and [String] parameters.
 */
fun <T : Bus<T>> T.selectItem(
    title: CharSequence,
    targetIntent: IntentAware,
    optionTitle: CharSequence? = null,
    vararg parameters: Pair<String, String>
): GASelectItem = selectItem(title, targetIntent, null, optionTitle, *parameters)

/**
 * Provides a [GASelectItem] with option title, [StoryStep] and [Parameters] parameters.
 */
fun <T : Bus<T>> T.selectItem(
    title: CharSequence,
    targetIntent: IntentAware,
    step: StoryStepDef,
    optionTitle: CharSequence? = null,
    parameters: Parameters
): GASelectItem = selectItem(title, targetIntent, step, optionTitle, *parameters.toArray())

/**
 * Provides a [GASelectItem] with option title, [StoryStep] and [String] parameters.
 */
fun <T : Bus<T>> T.selectItem(
    title: CharSequence,
    targetIntent: IntentAware,
    step: StoryStepDef? = null,
    optionTitle: CharSequence? = null,
    vararg parameters: Pair<String, String>
): GASelectItem {
    return GASelectItem(
        optionInfo(
            title,
            targetIntent,
            step,
            *parameters
        ),
        optionTitle?.let { translate(it).toString() }
    )
}
