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

import com.github.salomonbrys.kodein.instance
import fr.vsct.tock.shared.Dice
import fr.vsct.tock.shared.booleanProperty
import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.injector
import java.text.ChoiceFormat
import java.text.MessageFormat
import java.util.Formatter
import java.util.Locale

/**
 * The main entry class of translator module.
 */
object Translator {

    /**
     * Translator and i18n support is disabled by default.
     * Set it to true if you want to enable i18n.
     */
    @Volatile
    var enabled: Boolean = booleanProperty("tock_i18n_enabled", false)

    private val keyLabelRegex = "[^\\p{L}_]+".toRegex()
    private val defaultInterface: UserInterfaceType = UserInterfaceType.textChat

    private val i18nDAO: I18nDAO by injector.instance()
    private val translator: TranslatorEngine by injector.instance()

    fun translate(key: I18nLabelKey, locale: Locale, userInterfaceType: UserInterfaceType): String {
        if (!enabled) {
            return formatMessage(key.defaultLabel, locale, userInterfaceType, key.args)
        }
        val storedLabel = i18nDAO.getLabelById(key.key)

        val label = if (storedLabel != null) {
            getLabel(storedLabel, key.defaultLabel, locale, userInterfaceType)
        } else {
            val defaultLabel = I18nLocalizedLabel(defaultLocale, defaultInterface, key.defaultLabel)
            if (locale != defaultLocale) {
                val localizedLabel = I18nLocalizedLabel(locale, userInterfaceType, translate(key.defaultLabel, defaultLocale, locale))
                val label = I18nLabel(key.key, key.category, listOf(defaultLabel, localizedLabel))
                i18nDAO.save(label)
                localizedLabel.label
            } else {
                val interfaceLabel =
                        if (defaultInterface != userInterfaceType) I18nLocalizedLabel(locale, userInterfaceType, defaultLabel.label)
                        else null
                val label = I18nLabel(key.key, key.category, listOfNotNull(defaultLabel, interfaceLabel))
                i18nDAO.save(label)
                key.defaultLabel
            }
        }

        return formatMessage(label, locale, userInterfaceType, key.args)
    }

    private fun getLabel(i18nLabel: I18nLabel,
                         defaultLabel: String,
                         locale: Locale,
                         userInterfaceType: UserInterfaceType): String {
        val localizedLabel = i18nLabel.findLabel(locale, userInterfaceType)
        return if (localizedLabel != null) {
            if (localizedLabel.alternatives.isEmpty()) {
                localizedLabel.label
            } else {
                Dice.choose(listOf(localizedLabel.label) + localizedLabel.alternatives)
            }
        } else {
            val labelWithoutUserInterface = i18nLabel.findLabel(locale)
            if (labelWithoutUserInterface != null) {
                i18nDAO.save(i18nLabel.copy(i18n = i18nLabel.i18n + labelWithoutUserInterface.copy(interfaceType = userInterfaceType, validated = false)))
                labelWithoutUserInterface.label
            } else {
                val newLabel = translate(defaultLabel, defaultLocale, locale)
                i18nDAO.save(i18nLabel.copy(i18n = i18nLabel.i18n + I18nLocalizedLabel(locale, userInterfaceType, newLabel)))
                newLabel
            }
        }
    }

    internal fun formatMessage(label: String, locale: Locale, userInterfaceType: UserInterfaceType, args: List<Any?>): String {
        if (args.isEmpty()) {
            return label
        }
        return MessageFormat(escapeQuotes(label), locale).format(
                args.map { formatArg(it, locale, userInterfaceType) }.toTypedArray(),
                StringBuffer(),
                null).toString()
    }

    private fun escapeQuotes(text: String): String = text.replace("'", "''")

    fun translate(text: String, source: Locale, target: Locale): String {
        val t = escapeQuotes(text)
        val m = MessageFormat(t, source)
        var pattern = m.toPattern()
        val choicePrefixList: MutableList<String> = mutableListOf()
        m.formatsByArgumentIndex.forEachIndexed { i, format ->
            if (format is ChoiceFormat) {
                pattern = pattern.replace(format.toPattern(), "")
                val choiceFormat = ChoiceFormat(format.limits, format.formats.map { translator.translate(it as String, source, target) }.toTypedArray())
                m.setFormatByArgumentIndex(i, choiceFormat)
                choicePrefixList.add("{$i,choice,}")
            }
        }
        if (choicePrefixList.isEmpty()) {
            return translator.translate(text, source, target)
        } else {
            var splitPattern = listOf(pattern)
            choicePrefixList.forEach { prefix ->
                splitPattern.forEachIndexed { i, s ->
                    val index = s.indexOf(prefix)
                    if (index != -1) {
                        val a = s.substring(0, index)
                        val b = prefix
                        val c = s.substring(index + prefix.length, s.length)
                        splitPattern = splitPattern.subList(0, i) + listOf(a) + listOf(b) + listOf(c) + splitPattern.subList(i + 1, splitPattern.size)
                    }
                }
            }
            val newMessage = MessageFormat(splitPattern.map {
                if (choicePrefixList.contains(it)) {
                    it
                } else {
                    translator.translate(it.replace("''", "'"), source, target)
                }
            }.joinToString(""))
            newMessage.formats = m.formats
            return newMessage.toPattern()
        }
    }

    private fun formatArg(arg: Any?, locale: Locale, userInterfaceType: UserInterfaceType): Any? {
        return when (arg) {
            is String? -> arg ?: ""
            is Number? -> arg ?: -1
            is Boolean? -> if (arg == null) -1 else if (arg) 1 else 0
            is Enum<*>? -> arg?.ordinal ?: -1
            is I18nLabelKey -> translate(arg, locale, userInterfaceType)
            null -> ""
            else -> Formatter().format(locale, "%s", arg).toString()
        }
    }

    fun translate(key: String, category: String, defaultLabel: String, locale: Locale, userInterfaceType: UserInterfaceType): String {
        return translate(I18nLabelKey(key.toLowerCase(), category.toLowerCase(), defaultLabel), locale, userInterfaceType)
    }

    fun getKeyFromDefaultLabel(label: String): String {
        val s = label.trim().replace(" ", "_").replace(keyLabelRegex, "").toLowerCase()
        return s.substring(0, Math.min(40, s.length))
    }

    fun completeAllLabels(i18n: List<I18nLabel>) {
        val newI18n = i18n.map { i ->
            val defaultValue = i.findLabel(defaultLocale, defaultInterface)
            val newLabels = i.i18n.map {
                if (defaultValue == null || it.validated || !it.label.isNullOrBlank() || (it.locale == defaultLocale && it.interfaceType == defaultInterface)) {
                    it
                } else {
                    if (it.locale == defaultLocale) {
                        it.copy(label = defaultValue.label)
                    } else {
                        if (it.interfaceType == defaultInterface) {
                            it.copy(label = translate(defaultValue.label, defaultLocale, it.locale))
                        } else {
                            val defaultInterfaceValue = i.findLabel(defaultLocale, it.interfaceType)
                            if (defaultInterfaceValue?.label.isNullOrBlank()) {
                                it.copy(label = translate(defaultValue.label, defaultLocale, it.locale))
                            } else {
                                it.copy(label = translate(defaultInterfaceValue!!.label, defaultLocale, it.locale))
                            }
                        }
                    }
                }
            }
            i.copy(i18n = newLabels)
        }
        i18nDAO.save(newI18n)
    }
}