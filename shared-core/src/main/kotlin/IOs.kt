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

import com.google.common.io.Resources
import java.io.InputStream
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * Get a resource url from the classpath.
 */
fun resource(path: String): URL = Loader::class.java.getResource(path) ?: error("resource $path not found")

/**
 * Get a resource [InputStream] from the classpath.
 */
fun resourceAsStream(path: String): InputStream = Loader::class.java.getResourceAsStream(path) ?: error("path not found: $path")

/**
 * Get a text content of a resource from the classpath.
 */
fun resourceAsString(path: String): String = Resources.toString(resource(path), StandardCharsets.UTF_8)
