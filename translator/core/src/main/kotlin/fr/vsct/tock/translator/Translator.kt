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

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.util.concurrent.UncheckedExecutionException
import fr.vsct.tock.shared.booleanProperty
import fr.vsct.tock.shared.defaultLocale
import fr.vsct.tock.shared.injector
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.provide
import fr.vsct.tock.translator.UserInterfaceType.textAndVoiceAssistant
import fr.vsct.tock.translator.UserInterfaceType.textChat
import fr.vsct.tock.translator.UserInterfaceType.voiceAssistant
import mu.KotlinLogging
import org.litote.kmongo.toId
import java.text.ChoiceFormat
import java.text.MessageFormat
import java.util.Formatter
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

/**
 * The main entry class of translator module.
 */
object Translator {

    private val logger = KotlinLogging.logger {}

    private val oldKeyTransformer = booleanProperty("tock_old_key_transformer", false)

    /**
     * Translator and i18n support is enable by default.
     * Set it to false if you want to disable i18n.
     */
    @Volatile
    var enabled: Boolean = booleanProperty("tock_i18n_enabled", true)

    private val keyLabelRegex = "[^\\p{L}_]+".toRegex()
    private val defaultInterface: UserInterfaceType = textChat

    private val i18nDAO: I18nDAO get() = injector.provide()
    private val translator: TranslatorEngine get() = injector.provide()

    private val cache: Cache<String, I18nLabel> = CacheBuilder.newBuilder()
        .expireAfterWrite(longProperty("tock_i18n_cache_write_timeout_in_seconds", 10), TimeUnit.SECONDS)
        .build()

    private val voiceTransformers: MutableList<VoiceTransformer> = CopyOnWriteArrayList()

    fun registerVoiceTransformer(transformer: VoiceTransformer) {
        voiceTransformers.add(transformer)
    }

    fun unregisterVoiceTransformer(transformer: VoiceTransformer) {
        voiceTransformers.remove(transformer)
    }

    private fun transformArg(text: Any, locale: Locale, userInterfaceType: UserInterfaceType): Any {
        var t = text
        voiceTransformers.forEach { t = it.transformArg(t, locale, userInterfaceType) }
        return t
    }

    private fun loadLabel(id: String): I18nLabel? {
        return try {
            cache.get(id, { requireNotNull(i18nDAO.getLabelById(id.toId())) }).copy()
        } catch (e: UncheckedExecutionException) {
            null
        }
    }

    private fun getLabel(id: String): I18nLabel? = loadLabel(id)

    fun getLabel(key: I18nLabelKey): I18nLabel? = getLabel(key.key)
        ?.apply {
            if (defaultLabel != null && key.defaultLabel != key.defaultLabel.toString()) {
                logger.warn { "default label has changed - old value $defaultLabel - new value : ${key.defaultLabel}" }
            }
        }

    fun saveIfNotExists(key: I18nLabelKey): I18nLabel = saveIfNotExists(key, defaultLocale)

    fun saveIfNotExists(key: I18nLabelKey, defaultLocale: Locale): I18nLabel = getLabel(key) ?: {
        val defaultLabelKey = key.defaultLabel.toString()
        val defaultLabel = I18nLocalizedLabel(defaultLocale, defaultInterface, defaultLabelKey)
        val label =
            I18nLabel(key.key.toId(), key.namespace, key.category, LinkedHashSet(listOf(defaultLabel)), defaultLabelKey)
        i18nDAO.save(label)
        label
    }.invoke()

    fun translate(
        key: I18nLabelKey,
        locale: Locale,
        userInterfaceType: UserInterfaceType,
        connectorId: String? = null
    ): TranslatedString {
        if (!enabled) {
            return TranslatedString(
                formatMessage(
                    key.defaultLabel.toString(),
                    locale,
                    userInterfaceType,
                    connectorId,
                    key.args
                )
            )
        }
        if (key.defaultLabel is TranslatedString) {
            logger.warn { "already translated string is proposed to translation - skipped: $key" }
            return key.defaultLabel
        }
        if (key.defaultLabel is RawString) {
            logger.warn { "raw string is proposed to translation - skipped: $key" }
            return TranslatedString(key.defaultLabel)
        }

        val storedLabel = getLabel(key)

        val targetDefaultUserInterface = if (userInterfaceType == textAndVoiceAssistant) textChat else userInterfaceType

        val label = if (storedLabel != null) {
            getLabel(storedLabel, key.defaultLabel.toString(), locale, userInterfaceType, connectorId)
        } else {
            val defaultLabel = I18nLocalizedLabel(defaultLocale, defaultInterface, key.defaultLabel.toString())
            if (locale != defaultLocale) {
                val localizedLabel = I18nLocalizedLabel(
                    locale,
                    targetDefaultUserInterface,
                    translate(
                        key.defaultLabel.toString(), defaultLocale, locale
                    )
                )
                val label = I18nLabel(
                    key.key.toId(),
                    key.namespace,
                    key.category,
                    LinkedHashSet(listOf(defaultLabel, localizedLabel)),
                    key.defaultLabel.toString()
                )
                i18nDAO.save(label)
                localizedLabel.label
            } else {
                val interfaceLabel =
                    if (defaultInterface != targetDefaultUserInterface)
                        I18nLocalizedLabel(locale, targetDefaultUserInterface, defaultLabel.label)
                    else null
                val label = I18nLabel(
                    key.key.toId(),
                    key.namespace,
                    key.category,
                    LinkedHashSet(listOfNotNull(defaultLabel, interfaceLabel)),
                    key.defaultLabel.toString()
                )
                i18nDAO.save(label)
                key.defaultLabel
            }
        }

        logger.debug { "find label $label for $key, $locale, $userInterfaceType and $connectorId" }

        return if (label is TextAndVoiceTranslatedString) {
            label.copy(
                text = formatMessage(label.text.toString(), locale, textChat, connectorId, key.args),
                voice = formatMessage(label.voice.toString(), locale, voiceAssistant, connectorId, key.args)
            )
        } else {
            TranslatedString(formatMessage(label.toString(), locale, targetDefaultUserInterface, connectorId, key.args))
        }
    }

    private fun getLabel(
        i18nLabel: I18nLabel,
        defaultLabel: String,
        locale: Locale,
        userInterfaceType: UserInterfaceType,
        connectorId: String?
    ): CharSequence {
        return if (userInterfaceType == textAndVoiceAssistant) {
            val text = i18nLabel.findLabel(locale, textChat, connectorId)
            val voice = i18nLabel.findLabel(locale, voiceAssistant, connectorId)
            if (voice != null) {
                if (text != null) {
                    val randomIndex = text.randomAlternativesIndex()
                    val t = text.randomText(randomIndex)
                    val v = voice.randomText(randomIndex)
                    if (t.isNotBlank() && v.isNotBlank()) {
                        TextAndVoiceTranslatedString(t, v)
                    } else {
                        t
                    }
                } else {
                    voice.randomText()
                }
            } else {
                text?.randomText() ?: labelWithoutUserInterface(
                    i18nLabel,
                    defaultLabel,
                    locale,
                    defaultInterface,
                    connectorId
                )
            }
        } else {
            i18nLabel.findLabel(locale, userInterfaceType, connectorId)?.randomText().run {
                if (isNullOrBlank()) {
                    labelWithoutUserInterface(i18nLabel, defaultLabel, locale, userInterfaceType, connectorId)
                } else {
                    this!!
                }
            }
        }
    }

    private fun labelWithoutUserInterface(
        i18nLabel: I18nLabel,
        defaultLabel: String,
        locale: Locale,
        userInterfaceType: UserInterfaceType,
        connectorId: String?
    ): String {

        val labelWithoutUserInterface = i18nLabel.findLabel(locale, connectorId)
        return if (labelWithoutUserInterface != null) {
            i18nDAO.save(
                i18nLabel.copy(
                    i18n =
                    LinkedHashSet(
                        i18nLabel.i18n + labelWithoutUserInterface.copy(
                            interfaceType = userInterfaceType,
                            validated = false
                        )
                    )
                )
            )
            labelWithoutUserInterface.label
        } else {
            val newLabel = translate(defaultLabel, defaultLocale, locale)
            i18nDAO.save(
                i18nLabel.copy(
                    i18n =
                    LinkedHashSet(
                        i18nLabel.i18n + I18nLocalizedLabel(
                            locale,
                            userInterfaceType,
                            newLabel
                        )
                    )
                )
            )
            newLabel
        }
    }

    fun formatMessage(
        label: String,
        locale: Locale,
        userInterfaceType: UserInterfaceType,
        connectorId: String?,
        args: List<Any?>
    ): String {
        if (args.isEmpty()) {
            return label
        }
        return MessageFormat(escapeQuotes(label), locale).format(
            args.map { formatArg(it, locale, userInterfaceType, connectorId) }.toTypedArray(),
            StringBuffer(),
            null
        ).toString()
    }

    private fun escapeQuotes(text: String): String = text
        .replace("'", "''")
        .replace("%%", "'")

    fun translate(text: String, source: Locale, target: Locale): String {
        val t = escapeQuotes(text)
        val m = MessageFormat(t, source)
        var pattern = m.toPattern()
        val choicePrefixList: MutableList<String> = mutableListOf()
        m.formatsByArgumentIndex.forEachIndexed { i, format ->
            if (format is ChoiceFormat) {
                pattern = pattern.replace(format.toPattern(), "")
                val choiceFormat = ChoiceFormat(
                    format.limits,
                    format.formats.map { translator.translate(it as String, source, target) }.toTypedArray()
                )
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
                        splitPattern = splitPattern.subList(0, i) + listOf(a) + listOf(b) + listOf(c) +
                                splitPattern.subList(i + 1, splitPattern.size)
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

    private fun formatArg(arg: Any?, locale: Locale, userInterfaceType: UserInterfaceType, connectorId: String?): Any? {
        val a = when (arg) {
            is String? -> arg ?: ""
            is Number? -> arg ?: -1
            is Boolean? -> if (arg == null) -1 else if (arg) 1 else 0
            is Enum<*>? -> arg?.ordinal ?: -1
            is I18nLabelKey -> translate(arg, locale, userInterfaceType, connectorId)
            null -> ""
            else -> Formatter().format(locale, "%s", arg).toString()
        }

        return transformArg(a, locale, userInterfaceType)
    }

    fun translate(
        key: String,
        namespace: String,
        category: String,
        defaultLabel: CharSequence,
        locale: Locale,
        userInterfaceType: UserInterfaceType,
        connectorId: String
    ): CharSequence {
        return translate(
            I18nLabelKey(
                key.toLowerCase(),
                namespace,
                category.toLowerCase(),
                defaultLabel
            ),
            locale,
            userInterfaceType,
            connectorId
        )
    }

    private fun oldKeyFromDefaultLabel(label: CharSequence): String {
        val s = label.trim().toString().replace(" ", "_").replace(keyLabelRegex, "").toLowerCase()
        return s.substring(0, Math.min(40, s.length))
    }

    private fun newKeyFromDefaultLabel(label: CharSequence): String {
        val s = label.trim().toString().replace(" ", "_").replace(keyLabelRegex, "_").toLowerCase()
        return s.substring(0, Math.min(512, s.length))
    }

    fun getKeyFromDefaultLabel(label: CharSequence): String {
        return if (oldKeyTransformer) oldKeyFromDefaultLabel(label) else newKeyFromDefaultLabel(label)
    }

    fun completeAllLabels(i18n: List<I18nLabel>) {
        val newI18n = i18n.map { i ->
            val defaultValue = i.findLabel(defaultLocale, defaultInterface, null)
            val newLabels = i.i18n.map {
                if (defaultValue == null || it.validated || !it.label.isBlank() || (it.locale == defaultLocale && it.interfaceType == defaultInterface)) {
                    it
                } else {
                    if (it.locale == defaultLocale) {
                        it.copy(label = defaultValue.label)
                    } else {
                        if (it.interfaceType == defaultInterface) {
                            it.copy(label = translate(defaultValue.label, defaultLocale, it.locale))
                        } else {
                            val defaultInterfaceValue = i.findLabel(defaultLocale, it.interfaceType, null)
                            if (defaultInterfaceValue?.label.isNullOrBlank()) {
                                it.copy(label = translate(defaultValue.label, defaultLocale, it.locale))
                            } else {
                                it.copy(label = translate(defaultInterfaceValue!!.label, defaultLocale, it.locale))
                            }
                        }
                    }
                }
            }
            i.copy(i18n = LinkedHashSet(newLabels))
        }
        i18nDAO.save(newI18n)
    }
}