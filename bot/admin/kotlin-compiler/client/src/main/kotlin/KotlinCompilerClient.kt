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

package ai.tock.bot.admin.kotlin.compiler.client

import ai.tock.bot.admin.kotlin.compiler.KotlinFile
import ai.tock.bot.admin.kotlin.compiler.KotlinFileCompilation
import ai.tock.shared.addJacksonConverter
import ai.tock.shared.booleanProperty
import ai.tock.shared.create
import ai.tock.shared.error
import ai.tock.shared.longProperty
import ai.tock.shared.property
import ai.tock.shared.retrofitBuilderWithTimeoutAndLogger
import mu.KotlinLogging

/**
 *
 */
object KotlinCompilerClient {
    private val compilerTimeoutInSeconds = longProperty("tock_bot_compiler_timeout_in_ms", 60000L)
    private val compilerUrl = property("tock_bot_compiler_service_url", "http://localhost:8887")
    private val logger = KotlinLogging.logger {}
    val compilerDisabled: Boolean = booleanProperty("tock_bot_compiler_disabled", false)

    private val service: KotlinCompilerService? =
        try {
            retrofitBuilderWithTimeoutAndLogger(compilerTimeoutInSeconds)
                .addJacksonConverter()
                .baseUrl(compilerUrl)
                .build()
                .create()
        } catch (t: Throwable) {
            logger.error(t)
            null
        }

    fun compile(file: KotlinFile): KotlinFileCompilation? =
        if (compilerDisabled) {
            logger.warn { "kotlin compiler is disabled" }
            null
        } else {
            service?.compile(file)?.execute()?.body()
        }
}
