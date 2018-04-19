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
 * A label value contains an unique key used to retrieve the label translations from the db,
 * There is also a default label used to generate the label if no translation is found, and optional
 * format pattern arguments.
 */
data class I18nLabelValue internal constructor(
    val key: String,
    val namespace: String,
    val category: String,
    val defaultLabel: CharSequence,
    val args: List<Any?> = emptyList()
) : CharSequence by defaultLabel {

    internal constructor(
        key: String,
        namespace: String,
        category: String,
        defaultLabel: CharSequence,
        vararg args: Any?
    ) : this(key, namespace, category, defaultLabel, args.toList())

    internal constructor(
        key: String,
        namespace: String,
        category: String,
        defaultLabel: CharSequence,
        arg: Any?
    ) : this(key, namespace, category, defaultLabel, listOf(arg))

    override fun toString(): String {
        return defaultLabel.toString()
    }

}