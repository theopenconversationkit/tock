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

package ai.tock.bot.engine

import ai.tock.bot.connector.ConnectorType
import ai.tock.translator.EMPTY_TRANSLATED_STRING
import ai.tock.translator.I18nContext
import ai.tock.translator.I18nKeyProvider
import ai.tock.translator.I18nLabelValue
import ai.tock.translator.TranslatedSequence
import ai.tock.translator.Translator
import ai.tock.translator.UserInterfaceType
import ai.tock.translator.raw
import java.util.Locale

/**
 * Translates [CharSequence] depending of current [userLocale], [userInterfaceType] and [targetConnectorType].
 */
interface I18nTranslator : I18nKeyProvider {

    /**
     * The current user [Locale].
     */
    val userLocale: Locale

    /**
     * The current user interface type.
     */
    val userInterfaceType: UserInterfaceType

    /**
     * The source [ConnectorType] used for the response.
     * The connector which initialize a conversation
     */
    val sourceConnectorType: ConnectorType

    /**
     * The target [ConnectorType] used for the response.
     * The connector used to fabric messages (bot responses)
     */
    val targetConnectorType: ConnectorType

    /**
     * The current context identifier.
     */
    val contextId: String?

    /**
     * Translates and format if needed the text with the optionals args.
     */
    fun translate(text: CharSequence?, vararg args: Any?): TranslatedSequence {
        return when {
            text.isNullOrBlank() -> EMPTY_TRANSLATED_STRING
            text is I18nLabelValue -> translate(text)
            text is TranslatedSequence -> text
            else -> return translate(i18n(text, args.toList()))
        }
    }

    /**
     * Translates and format if needed the text with the optionals args.
     */
    fun translate(text: CharSequence?, args: List<Any?>): TranslatedSequence =
        translate(text, *args.toTypedArray())

    /**
     * Translates the specified key.
     */
    fun translate(key: I18nLabelValue?): TranslatedSequence =
        if (key == null) EMPTY_TRANSLATED_STRING
        else Translator.translate(
            key,
            I18nContext(
                userLocale,
                userInterfaceType,
                targetConnectorType.id,
                contextId
            )
        )

    /**
     * Translates the specified text and return null if the answer is blank.
     */
    fun translateAndReturnBlankAsNull(s: CharSequence?): TranslatedSequence? =
        translate(s).run { if (isBlank()) null else this.raw }
}
