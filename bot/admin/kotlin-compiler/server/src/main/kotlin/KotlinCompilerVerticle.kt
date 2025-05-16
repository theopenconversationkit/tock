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

package ai.tock.bot.admin.kotlin.compiler.server

import ai.tock.bot.admin.kotlin.compiler.KotlinCompilationException
import ai.tock.bot.admin.kotlin.compiler.KotlinFile
import ai.tock.bot.admin.kotlin.compiler.KotlinFileCompilation
import ai.tock.bot.admin.kotlin.compiler.TockKotlinCompiler
import ai.tock.shared.security.TockUserRole
import ai.tock.shared.vertx.WebVerticle
import io.vertx.ext.web.RoutingContext
import mu.KLogger
import mu.KotlinLogging

/**
 *
 */
class KotlinCompilerVerticle : WebVerticle() {

    override val logger: KLogger = KotlinLogging.logger {}

    override fun configure() {
        blockingJsonPost("/compile", TockUserRole.admin) { _, file: KotlinFile ->

            try {
                KotlinFileCompilation(TockKotlinCompiler.compile(file.script, file.fileName))
            } catch (e: KotlinCompilationException) {
                KotlinFileCompilation(null, e.errors)
            }
        }
    }

    override fun defaultHealthcheck(): (RoutingContext) -> Unit {
        return { it.response().end() }
    }

    override fun detailedHealthcheck(): (RoutingContext) -> Unit = ai.tock.shared.vertx.detailedHealthcheck()
}
