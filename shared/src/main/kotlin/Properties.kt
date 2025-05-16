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

package ai.tock.shared

import java.util.Properties

/**
 * Return an env or system String property or null if not found
 */
fun propertyOrNull(name: String): String? {
    return System.getProperty(name) ?: System.getenv(name)
}

/**
 * Is this env or system property exists?
 */
fun propertyExists(name: String): Boolean = propertyOrNull(name) != null

/**
 * Return an env or system String property.
 */
fun property(name: String, defaultValue: String): String = propertyOrNull(name) ?: defaultValue

/**
 * Return an env or system Int property.
 */
fun intProperty(name: String, defaultValue: Int): Int = propertyOrNull(name)?.toInt() ?: defaultValue

/**
 * Return an env or system Long property.
 */
fun longProperty(name: String, defaultValue: Long): Long = propertyOrNull(name)?.toLong() ?: defaultValue

/**
 * Return an env or system Boolean property.
 */
fun booleanProperty(name: String, defaultValue: Boolean): Boolean = propertyOrNull(name)?.toBoolean() ?: defaultValue

/**
 * Return an env or system List property.
 */
fun listProperty(name: String, defaultValue: List<String>, separator: String = ","): List<String> =
    propertyOrNull(name)?.split(separator) ?: defaultValue

/**
 * Return an env or system Map property.
 */
fun mapProperty(
    name: String,
    defaultValue: Map<String, String>,
    entrySeparator: String = "|",
    keyValueSeparator: String = "="
): Map<String, String> =
    propertyOrNull(name)?.split(entrySeparator)?.map { it.split(keyValueSeparator).let { it[0] to it[1] } }?.toMap()
        ?: defaultValue

/**
 * Return an env or system Map of List property.
 */
fun mapListProperty(
    name: String,
    defaultValue: Map<String, List<String>>,
    entrySeparator: String = "|",
    keyValueSeparator: String = "=",
    listSeparator: String = ","
): Map<String, List<String>> = propertyOrNull(name)?.split(entrySeparator)?.map {
    it.split(keyValueSeparator).let { it[0] to it[1].split(listSeparator) }
}?.toMap() ?: defaultValue

/**
 * Load a [Properties] for classpath.
 */
fun loadProperties(fileInClasspath: String): Properties =
    Properties().apply { load(resourceAsStream(fileInClasspath)) }

/**
 * Return true is the current environment is a dev environment.
 * Use "tock_env" property to know the environment - if not set or if the value is "dev", this is a dev environment.
 */
val devEnvironment: Boolean = property("tock_env", "dev") == "dev"

