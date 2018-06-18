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
 * A [TranslatedString] that has also a "voice" version.
 */
data class TextAndVoiceTranslatedString(
        val text: CharSequence,
        val voice: CharSequence) : TranslatedString(text) {

    fun isSSML(): Boolean = voice.isSSML()

    override fun toString(): String {
        return text.toString()
    }

    //override extension method
    fun splitToCharSequence(vararg delimiters: String, ignoreCase: Boolean = false, limit: Int = 0): List<CharSequence> {
        val textSplit = text.split(*delimiters, ignoreCase = ignoreCase, limit = limit)
        val voiceSplit = voice.split(*delimiters, ignoreCase = ignoreCase, limit = limit)
        return textSplit.mapIndexed { i, s ->
            TextAndVoiceTranslatedString(
                    s,
                    voiceSplit.getOrNull(i) ?: s
            )
        }
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return TextAndVoiceTranslatedString(
                text.subSequence(startIndex, endIndex),
                voice.subSequence(startIndex, endIndex))
    }
}