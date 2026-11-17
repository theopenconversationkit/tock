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

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 *
 */
class KotlinCompilerTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun beforeClass() {
            KotlinCompiler.init(listOf("target/test-classes/"))
        }

        var mark = false
    }

    class CompilationResultClassLoader(
        val className: String,
        val bytes: ByteArray,
    ) : ClassLoader(CompilationResultClassLoader::class.java.classLoader) {
        @Override
        override fun findClass(name: String): Class<*> {
            return if (name == className) {
                return defineClass(name, bytes, 0, bytes.size)
            } else {
                super.findClass(name)
            }
        }
    }

    @Test
    fun `simple compilation with erroneous file reports error`() {
        val erroneousSourceCode =
            mapOf(
                "ClassToBeCompiled.kt"
                    to
                    """
                fun main() {
                    println("Hello)
                }""",
            )

        val errors =
            assertFailsWith<KotlinCompilationException> {
                KotlinCompiler.compile(erroneousSourceCode, "ClassToBeCompiled.kt")
            }.errors
        println(errors)
        assertTrue(errors.isNotEmpty())
        assertTrue(errors.all { it.severity == Severity.ERROR })
        assertEquals(TextPosition(2, 34), errors.first().interval.start)
    }

    @Test
    fun `unresolved reference reports error at the right position`() {
        val errors =
            assertFailsWith<KotlinCompilationException> {
                KotlinCompiler.compile(mapOf("Invalid.kt" to "fun message() = missingValue"), "Invalid.kt")
            }.errors
        println(errors)
        assertEquals(1, errors.size)
        assertEquals(TextInterval(TextPosition(0, 16), TextPosition(0, 28)), errors.first().interval)
        assertTrue(errors.first().message.contains("missingValue"))
    }

    @Test
    fun `simple compilation and execution succeed`() {
        val sourceCode =
            mapOf(
                "ClassToBeCompiled.kt"
                    to
                    """
                import ai.tock.bot.admin.kotlin.compiler.KotlinCompilerTest
                fun main() {
                    KotlinCompilerTest.mark = true
                }""",
            )
        assertFalse(mark)
        val result = KotlinCompiler.compile(sourceCode, "ClassToBeCompiled.kt")
        assertEquals("ClassToBeCompiledKt", result.mainClass)
        val compiledClassLoader =
            CompilationResultClassLoader("ClassToBeCompiledKt", result.files["ClassToBeCompiledKt.class"]!!)
        val c = compiledClassLoader.loadClass("ClassToBeCompiledKt")
        c.getMethod("main").invoke(null)
        assertTrue(mark)
    }

    @Test
    fun `main class is the file facade of the requested file, in its package`() {
        val result =
            KotlinCompiler.compile(
                mapOf("my-script.kt" to "package a.b\n\nclass Helper\n\nval x = Helper()"),
                "my-script.kt",
            )
        assertEquals("a.b.My_scriptKt", result.mainClass)
        assertTrue(result.files.containsKey("a/b/Helper.class"))
    }

    @Test
    fun `simple compilation with external classes succeed`() {
        val sourceCode =
            mapOf(
                "ClassToBeCompiled.kt"
                    to
                    """
                fun main() {
                    ai.tock.shared.Dice.newInt(2)
                }""",
            )
        KotlinCompiler.compile(sourceCode, "ClassToBeCompiled.kt")
    }
}
