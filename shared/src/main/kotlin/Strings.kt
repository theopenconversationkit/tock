/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.shared

import org.apache.commons.lang3.StringEscapeUtils.escapeHtml4
import java.text.Normalizer
import java.util.Locale
import java.util.regex.Pattern


/**
 * This is the maximum text size allowed.
 */
private const val TEXT_MAX_LENGTH_ALLOWED = 50000

/**
 * Checks that the text has no more than 50000 chars.
 * Else cuts the String.
 */
fun checkMaxLengthAllowed(text: String): String =
    if (text.length > TEXT_MAX_LENGTH_ALLOWED) text.substring(0, TEXT_MAX_LENGTH_ALLOWED) else text

/**
 * Extract a namespace from a qualified name (ie namespace:name).
 */
fun String.namespace(): String = namespaceAndName().first

/**
 * Extract a name from a qualified name (ie namespace:name).
 */
fun String.name(): String = namespaceAndName().second

/**
 * Extract namespace and name from a qualified name (ie namespace:name).
 */
fun String.namespaceAndName(): Pair<String, String> = this.split(":").let { it[0] to it[1] }

/**
 * Add the specified namespace to a name if the name does not contains yet a namespace,
 * and return the result.
 */
fun String.withNamespace(namespace: String): String = if (contains(":")) this else "$namespace:$this"

/**
 * Replace the current namespace with the new namespace.
 */
fun String.changeNamespace(newNamespace: String): String =
    withoutNamespace().withNamespace(newNamespace)

/**
 * Remove the specified namespace from a qualified name if this qualified name contains the namespace,
 * and return the result.
 */
fun String.withoutNamespace(namespace: String? = null): String =
    if (contains(":")) namespace().let { if (namespace == null || it == namespace) name() else this }
    else this

internal fun String.endWithPunctuation(): Boolean =
    endsWith(".") || endsWith("!") || endsWith("?") || endsWith(",") || endsWith(";") || endsWith(":")

/**
 * Concat two strings and manage intermediate punctuation.
 */
fun concat(s1: String?, s2: String?): String {
    val s = s1?.trim() ?: ""
    return s + (if (s.isEmpty() || s.endWithPunctuation()) " " else ". ") + (s2?.trim() ?: "")
}

private val trailingRegexp = "[.,:;?!]+$".toRegex()
private val accentsRegexp = "\\p{InCombiningDiacriticalMarks}+".toRegex()

private const val HTML_TAG_PLACEHOLDER = "CHANGE_IT"
private val htmlTagPattern = "(?:&lt;$HTML_TAG_PLACEHOLDER)(.*?)(?:&gt;)"

private val notAllowedPattern = property("tock_safehtml_block_tag",
    "(?i)s*(script|iframe|object|embed|form|input|link|meta|onload|alert|onerror)[^>]").toRegex()

private val allowedTags = listProperty("tock_safehtml_allowed_tag",
    listOf("em", "strong","ul", "li","h1","h2","h3","blockquote","code","a","s","p", "⭐")).toSet()

private val htmlToFrenchLetter = mapOf(
    "&agrave;" to "à", "&acirc;" to "â", "&auml;" to "ä", "&ccedil;" to "ç",
    "&egrave;" to "è", "&eacute;" to "é", "&ecirc;" to "ê", "&euml;" to "ë",
    "&icirc;" to "î", "&iuml;" to "ï", "&ocirc;" to "ô", "&ouml;" to "ö",
    "&ugrave;" to "ù", "&ucirc;" to "û", "&uuml;" to "ü", "&ntilde;" to "ñ"
)

private val diacriticReplacements = mapOf(
    'e' to "[eéèêë]",
    'a' to "[aàáâãä]",
    'i' to "[iìíîï]",
    'o' to "[oòóôõöø]",
    'u' to "[uùúûü]",
    'n' to "[nñ]",
    'c' to "[cç]"
)

fun String.removeTrailingPunctuation(): String =
    replace(trailingRegexp, "").trim()

fun String.stripAccents(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(accentsRegexp, "")

fun String.normalize(locale: Locale): String =
    lowercase(locale)
        .removeTrailingPunctuation()
        .stripAccents()

fun allowDiacriticsInRegexp(input: String): String =
    input.map { char ->
        diacriticReplacements[char.lowercaseChar()] ?: char
    }.joinToString("")
        .replace(" ", "['-_ ]")

fun safeHTML(value: String): String {
    return value
        .let(::escapeHtml4)
        .let(::replaceAllowedTags)
        .let(::replaceHtmlEntities)
        .let(::removeMaliciousContent)
        .let(::filterAllowedCharacters)
}

private fun replaceAllowedTags(value: String): String {
    var result = value
    allowedTags.forEach { tag ->
        detectHtmlTag(result, tag)?.let { match ->
            result = result.replace(match, match
                .replace("&lt;$tag", "<$tag")
                .replace("&gt;", ">"))
        }
        result = result
            .replace("&lt;$tag&gt;", "<$tag>")
            .replace("&lt;/$tag&gt;", "</$tag>")
            .replace("&lt;$tag", "<$tag")
    }
    return result.replace("&quot;", "\"")
}

private fun replaceHtmlEntities(value: String): String =
    htmlToFrenchLetter.entries.fold(value) { acc, (entity, letter) ->
        acc.replace(entity, letter)
    }

private fun removeMaliciousContent(value: String): String =
    notAllowedPattern.findAll(value)
        .flatMap { it.groups.drop(1) }
        .mapNotNull { it?.value }
        .fold(value) { acc, match ->
            acc.replace(match, "")
        }

private fun filterAllowedCharacters(value: String): String =
    value.filter { char ->
        allowedTags.contains(char.toString()) ||
                htmlToFrenchLetter.values.contains(char.toString()) ||
                char.code < 192
    }

private fun detectHtmlTag(text: String, tag: String): String? =
    htmlTagPattern
        .replace(HTML_TAG_PLACEHOLDER, tag)
        .toRegex()
        .find(text)
        ?.value
