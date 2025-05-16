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

package ai.tock.bot.admin.kotlin.compiler

/**
 * Compile a Kotlin script.
 */
object TockKotlinCompiler {

    init {
        KotlinCompiler.init()
    }

    /**
     * Compile a Kotlin script with a specified [fileName].
     * @return the name of the file class with the compiled code
     */
    fun compile(script: String, fileName: String): CompilationResult {
        val sourceCode = mapOf(fileName to script)
        val errors = KotlinCompiler.getErrors(sourceCode)[fileName]!!
        if (errors.isNotEmpty()) {
            throw KotlinCompilationException(errors)
        }
        return KotlinCompiler.compileCorrectFiles(sourceCode, fileName, false)
    }
}
