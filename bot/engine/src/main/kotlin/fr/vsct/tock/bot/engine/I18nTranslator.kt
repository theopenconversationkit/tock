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

package fr.vsct.tock.bot.engine

import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.translator.I18nKeyProvider
import fr.vsct.tock.translator.I18nLabelKey
import fr.vsct.tock.translator.RawString
import fr.vsct.tock.translator.TranslatedString
import fr.vsct.tock.translator.Translator
import fr.vsct.tock.translator.UserInterfaceType
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
     * The [ConnectorType] used for the response.
     */
    val targetConnectorType: ConnectorType

    /**
     * Translates and format if needed the text with the optionals args.
     */
    fun translate(text: CharSequence?, vararg args: Any?): CharSequence {
        return if (text.isNullOrBlank()) {
            ""
        } else if (text is I18nLabelKey) {
            translate(text)
        } else if (text is TranslatedString || text is RawString) {
            text
        } else {
            return translate(i18nKeyFromLabel(text!!, args.toList()))
        }
    }

    /**
     * Translates the specified key.
     */
    fun translate(key: I18nLabelKey?): CharSequence =
        if (key == null) ""
        else Translator.translate(
            key,
            userLocale,
            userInterfaceType,
            targetConnectorType.id
        )

    /**
     * Translates the specified text and return null if the answer is blank.
     */
    fun translateAndReturnBlankAsNull(s: CharSequence?): String? =
        translate(s).run { if (isBlank()) null else this.toString() }
}