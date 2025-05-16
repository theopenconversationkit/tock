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
        val bytes: ByteArray
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
        val expectedError = try {
            Runtime::class.java.getMethod("version").invoke(null)
            listOf(
                CompileError(
                    TextInterval(
                        TextPosition(2, 34),
                        TextPosition(2, 35)
                    ),
                    """Expecting '"'""",
                    Severity.ERROR,
                    "red_wavy_line"
                ),
                CompileError(
                    TextInterval(
                        TextPosition(line = 2, ch = 34),
                        TextPosition(line = 2, ch = 35)
                    ),
                    "Expecting ')'",
                    Severity.ERROR,
                    "red_wavy_line"
                )
            )
        } catch (e: Throwable) {
            // java 8
            listOf(
                CompileError(
                    TextInterval(
                        TextPosition(2, 34),
                        TextPosition(2, 35)
                    ),
                    "Expecting '\"'",
                    Severity.ERROR,
                    "red_wavy_line"
                ),
                CompileError(
                    TextInterval(
                        TextPosition(2, 34),
                        TextPosition(2, 35)
                    ),
                    "Expecting ')'",
                    Severity.ERROR,
                    "red_wavy_line"
                )
            )
        }

        val erroneousSourceCode = mapOf(
            "ClassToBeCompiled.kt"
                to
                    """
                fun main() {
                    println("Hello)
                }"""
        )

        val errors = KotlinCompiler.getErrors(erroneousSourceCode)
        assertEquals(1, errors.size)
        assertEquals(
            expectedError,
            errors["ClassToBeCompiled.kt"]
        )
    }

    @Test
    fun `simple compilation and execution succeed`() {
        val sourceCode = mapOf(
            "ClassToBeCompiled.kt"
                to
                    """
                import ai.tock.bot.admin.kotlin.compiler.KotlinCompilerTest  
                fun main() {
                    KotlinCompilerTest.mark = true
                }"""
        )
        assertFalse(mark)
        assertEquals(emptyList(), KotlinCompiler.getErrors(sourceCode)["ClassToBeCompiled.kt"])
        val result = KotlinCompiler.compileCorrectFiles(sourceCode, "ClassToBeCompiled.kt", true)
        val compiledClassLoader =
            CompilationResultClassLoader("ClassToBeCompiledKt", result.files["ClassToBeCompiledKt.class"]!!)
        val c = compiledClassLoader.loadClass("ClassToBeCompiledKt")
        c.declaredMethods[0].invoke(null)
        assertTrue(mark)
    }

    @Test
    fun `simple compilation with external classes and java 9 succeed`() {
        try {
            // does not fail with java >= 9
            Runtime::class.java.getMethod("version").invoke(null)
        } catch (e: Exception) {
            // java 8 return
            return
        }
        val sourceCode = mapOf(
            "ClassToBeCompiled.kt"
                to
                    """
                fun main() {
                    ai.tock.shared.Dice.newInt(2)
                }"""
        )
        assertEquals(emptyList(), KotlinCompiler.getErrors(sourceCode)["ClassToBeCompiled.kt"])
        KotlinCompiler.compileCorrectFiles(sourceCode, "ClassToBeCompiled.kt", true)
    }
}
