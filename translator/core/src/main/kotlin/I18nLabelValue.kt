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
 * A label value contains an unique key used to retrieve the translations from the database.
 *
 * There is also a default label used to generate the label if no translation is found, and optional
 * format pattern arguments for this current translation.
 */
class I18nLabelValue constructor(
    /**
     * Unique key of the label.
     */
    val key: String,
    /**
     * Namespace of the label.
     */
    namespace: String,
    /**
     * Category of the label.
     */
    category: String,
    /**
     * The fallback value if none is found for the requested locale and interface type.
     *
     * If a [TranslatorEngine] is configured, this default label will also be used for automated translation.
     */
    val defaultLabel: CharSequence,
    /**
     * The optional format pattern arguments.
     */
    val args: List<Any?> = emptyList(),
    /**
     * Default values for various languages and interface types.
     */
    val defaultI18n: Set<I18nLocalizedLabel> = emptySet(),
) : CharSequence by defaultLabel {
    constructor(label: I18nLabel) :
        this(
            label._id.toString(),
            label.namespace,
            label.category,
            label.defaultLabel ?: "",
            defaultI18n = label.defaultI18n,
        )

    /**
     * Namespace of the label.
     */
    val namespace: String = namespace.lowercase()

    /**
     * Category of the label.
     */
    val category: String = category.lowercase()

    /**
     * Returns the value with the given namespace.
     */
    fun withNamespace(newNamespace: String): I18nLabelValue = I18nLabelValue(key.replaceBefore("_", newNamespace), newNamespace, category, defaultLabel, args, defaultI18n)

    /**
     * Returns the value with the given args.
     */
    fun withArgs(newArgs: List<Any?>): I18nLabelValue = I18nLabelValue(key, namespace, category, defaultLabel, newArgs, defaultI18n)

    /**
     * Returns the value with the given args.
     */
    fun withArgs(vararg newArgs: Any?): I18nLabelValue = withArgs(listOf(*newArgs))

    override fun toString(): String {
        return defaultLabel.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as I18nLabelValue

        if (defaultLabel != other.defaultLabel) return false
        if (args != other.args) return false
        if (key != other.key) return false
        if (namespace != other.namespace) return false
        if (category != other.category) return false
        if (defaultI18n != other.defaultI18n) return false

        return true
    }

    override fun hashCode(): Int {
        var result = defaultLabel.hashCode()
        result = 31 * result + args.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + namespace.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + defaultI18n.hashCode()
        return result
    }
}
