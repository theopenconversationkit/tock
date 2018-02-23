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

package fr.vsct.tock.bot.admin.kotlin.compiler.client

import fr.vsct.tock.bot.admin.kotlin.compiler.KotlinFile
import fr.vsct.tock.bot.admin.kotlin.compiler.KotlinFileCompilation
import fr.vsct.tock.shared.addJacksonConverter
import fr.vsct.tock.shared.create
import fr.vsct.tock.shared.longProperty
import fr.vsct.tock.shared.property
import fr.vsct.tock.shared.retrofitBuilderWithTimeoutAndLogger


/**
 *
 */
object KotlinCompilerClient {

    private val compilerTimeoutInSeconds = longProperty("tock_bot_compiler_timeout_in_ms", 60000L)
    private val compilerUrl = property("tock_bot_compiler_service_url", "http://localhost:8887")

    private val service: KotlinCompilerService

    init {
        service = retrofitBuilderWithTimeoutAndLogger(compilerTimeoutInSeconds)
            .addJacksonConverter()
            .baseUrl(compilerUrl)
            .build()
            .create()
    }

    fun compile(file: KotlinFile): KotlinFileCompilation? = service.compile(file).execute().body()

}