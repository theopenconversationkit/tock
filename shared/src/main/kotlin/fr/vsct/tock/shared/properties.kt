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

package fr.vsct.tock.shared

private fun findProperty(name: String): String? {
    return System.getProperty(name) ?: System.getenv(name)
}

fun propertyExists(name: String): Boolean = findProperty(name) != null

fun property(name: String, defaultValue: String): String = findProperty(name) ?: defaultValue

fun intProperty(name: String, defaultValue: Int): Int = findProperty(name)?.toInt() ?: defaultValue

fun longProperty(name: String, defaultValue: Long): Long = findProperty(name)?.toLong() ?: defaultValue

fun booleanProperty(name: String, defaultValue: Boolean): Boolean = findProperty(name)?.toBoolean() ?: defaultValue

fun listProperty(name: String, defaultValue: List<String>, separator: String = ","): List<String>
        = findProperty(name)?.split(separator) ?: defaultValue

fun mapProperty(name: String, defaultValue: Map<String, String>, entrySeparator: String = "|", keyValueSeparator: String = "="): Map<String, String>
        = findProperty(name)?.split(entrySeparator)?.map { it.split(keyValueSeparator).let { it[0] to it[1] } }?.toMap() ?: defaultValue

fun mapListProperty(name: String, defaultValue: Map<String, List<String>>, entrySeparator: String = "|", keyValueSeparator: String = "=", listSeparator: String = ","): Map<String, List<String>>
        = findProperty(name)?.split(entrySeparator)?.map { it.split(keyValueSeparator).let { it[0] to it[1].split(listSeparator) } }?.toMap() ?: defaultValue


val devEnvironment: Boolean = property("tock_env", "dev") == "dev"
