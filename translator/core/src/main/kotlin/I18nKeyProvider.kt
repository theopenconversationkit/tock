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

package ai.tock.translator

/**
 * An i18n key generator. Used to generate an unique id from a default label.
 */
interface I18nKeyProvider {
    companion object {
        /**
         * Simple key provider depending of a namespace and a category.
         */
        fun simpleKeyProvider(
            namespace: String,
            category: String,
        ): I18nKeyProvider =
            object : I18nKeyProvider {
                override fun i18n(
                    defaultLabel: CharSequence,
                    args: List<Any?>,
                ): I18nLabelValue =
                    I18nLabelValue(
                        generateKey(namespace, category, defaultLabel),
                        namespace,
                        category,
                        defaultLabel,
                        args,
                    )
            }

        /**
         * Generates a label key from a namespace, a category and a default label.
         */
        fun generateKey(
            namespace: String,
            category: String,
            defaultLabel: CharSequence,
        ): String {
            val prefix =
                if (category.isEmpty()) {
                    namespace
                } else {
                    "${namespace}_$category"
                }
            return "${prefix}_${Translator.getKeyFromDefaultLabel(defaultLabel)}"
        }
    }

    /**
     * Gets an [I18nLabelValue] from a default label and option args.
     * This is the method to implement for this interface.
     */
    fun i18n(
        defaultLabel: CharSequence,
        args: List<Any?> = emptyList(),
    ): I18nLabelValue

    /**
     * Shortcut method of [i18n] with varargs.
     */
    fun i18n(
        defaultLabel: CharSequence,
        vararg args: Any?,
    ): I18nLabelValue = i18n(defaultLabel, args.toList())
}
