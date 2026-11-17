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
import ai.tock.bot.admin.kotlin.compiler.TockKotlinCompiler
import ai.tock.bot.definition.StoryDefinition
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Exercise compilation with the server's resolved dependencies, including the bot DSL.
 * Core-only tests cannot detect a compiler version overridden by dependency management in the server.
 */
class StoryCompilationTest {
    @Test
    fun `compile and load a story with server dependencies`() {
        val result =
            TockKotlinCompiler.compile(
                """
                import ai.tock.bot.definition.story

                fun message() = "Hello from a compiled story"
                val greeting = story("greeting") { end(message()) }
                """.trimIndent(),
                "Greeting.kt",
            )
        val classLoader =
            object : ClassLoader(javaClass.classLoader) {
                override fun findClass(name: String): Class<*> {
                    val bytes = result.files[name.replace('.', '/') + ".class"] ?: return super.findClass(name)
                    return defineClass(name, bytes, 0, bytes.size)
                }
            }
        val compiledClass = classLoader.loadClass(result.mainClass)
        val story = compiledClass.getMethod("getGreeting").invoke(null) as StoryDefinition

        assertEquals("greeting", story.mainIntent().name)
        assertEquals("Hello from a compiled story", compiledClass.getMethod("message").invoke(null))
    }

    @Test
    fun `invalid script reports compilation errors with server dependencies`() {
        val exception =
            assertFailsWith<KotlinCompilationException> {
                TockKotlinCompiler.compile("fun message() = missingValue", "Invalid.kt")
            }

        assertTrue(exception.errors.isNotEmpty())
    }
}
