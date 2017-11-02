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

package fr.vsct.tock.translator

/**
 *
 */
interface I18nKeyProvider {

    fun i18nKeyFromLabel(defaultLabel: CharSequence, vararg args: Any?): I18nLabelKey
            = i18nKeyFromLabel(defaultLabel, args.toList())

    fun i18nKeyFromLabel(defaultLabel: CharSequence, arg: Any?): I18nLabelKey
            = i18nKeyFromLabel(defaultLabel, listOf(arg))

    fun i18nKeyFromLabel(defaultLabel: CharSequence, args: List<Any?> = emptyList()): I18nLabelKey

    fun i18nKey(
            key: String,
            namespace: String,
            category: String,
            defaultLabel: CharSequence,
            args: List<Any?> = emptyList()): I18nLabelKey =

            I18nLabelKey(
                    key.toLowerCase(),
                    namespace,
                    category.toLowerCase(),
                    defaultLabel,
                    args)

    /**
     * Shortcut method for [i18nKeyFromLabel].
     */
    fun i18n(defaultLabel: CharSequence, vararg args: Any?): I18nLabelKey
            = i18nKeyFromLabel(defaultLabel, *args)

}