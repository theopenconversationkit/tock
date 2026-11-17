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

import ai.tock.shared.error
import ai.tock.shared.listProperty
import mu.KotlinLogging
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readBytes
import kotlin.io.path.relativeTo
import kotlin.io.path.writeText
import kotlin.streams.asSequence

/**
 * Kotlin source code compiler.
 *
 * Relies only on the command line entry point of the compiler ([K2JVMCompiler]),
 * so that it does not depend on the compiler internals, which change with every Kotlin release.
 */
internal object KotlinCompiler {
    private val logger = KotlinLogging.logger {}

    /**
     * Jars that are not useful to compile the scripts, and are expensive to index (the compiler itself).
     */
    private val excludedJars = listOf("kotlin-compiler-embeddable", "kotlin-daemon-embeddable")

    private const val JVM_TARGET = "21"

    @Volatile
    private var classpath: List<File> = emptyList()

    fun init(extraClassPath: List<String> = listProperty("tock_kotlin_compiler_classpath", emptyList())) {
        try {
            logger.info { "Init KotlinCompiler" }
            classpath = resolveClasspath(KotlinCompiler::class.java.classLoader, extraClassPath)
            logger.info { "End KotlinCompiler initialization" }
        } catch (t: Throwable) {
            logger.error(t)
        }
    }

    private fun resolveClasspath(
        classLoader: ClassLoader,
        extraClassPath: List<String>,
    ): List<File> {
        val paths: List<Path> =
            (classLoader as? URLClassLoader)?.urLs?.map { Paths.get(it.toURI()) }
                ?: (extraClassPath + System.getProperty("java.class.path").split(File.pathSeparator))
                    .filter { it.isNotBlank() }
                    .map { Paths.get(it) }
                    .flatMap { path ->
                        if (path.isDirectory()) {
                            path.listDirectoryEntries("*.jar") + listOf(path)
                        } else {
                            listOf(path)
                        }
                    }
        return paths
            .distinct()
            .map { it.toFile() }
            .filter { file -> excludedJars.none { file.name.startsWith(it) } }
            .apply { logger.debug { "class path used : $this" } }
    }

    /**
     * Compiles the [files] (file name -> source code).
     *
     * @return the compiled classes, and the file facade class of [fileName] as main class
     * @throws KotlinCompilationException if the compilation fails
     */
    @Synchronized
    fun compile(
        files: Map<String, String>,
        fileName: String,
    ): CompilationResult {
        val workDir = Files.createTempDirectory("tock-kotlin-compiler")
        try {
            val sourceDir = workDir.resolve("src").createDirectories()
            val outputDir = workDir.resolve("out").createDirectories()
            val sourceFiles =
                files.map { (name, code) ->
                    sourceDir.resolve(sourceFileName(name)).apply { writeText(code) }
                }

            val collector = ErrorCollector()
            val arguments =
                K2JVMCompilerArguments().apply {
                    freeArgs = sourceFiles.map { it.toString() }
                    destination = outputDir.toString()
                    classpath = this@KotlinCompiler.classpath.joinToString(File.pathSeparator)
                    // the stdlib and kotlin-reflect are already in the classpath
                    noStdlib = true
                    noReflect = true
                    moduleName = "tockScript"
                    jvmTarget = JVM_TARGET
                }
            val exitCode = K2JVMCompiler().exec(collector, Services.EMPTY, arguments)
            if (exitCode != ExitCode.OK || collector.errors.isNotEmpty()) {
                throw KotlinCompilationException(
                    collector.errors.ifEmpty { listOf(compileError("Compilation failed: $exitCode")) },
                )
            }

            val classes =
                Files.walk(outputDir).use { stream ->
                    stream
                        .asSequence()
                        .filter { it.toString().endsWith(".class") }
                        .associate { it.relativeTo(outputDir).joinToString("/") to it.readBytes() }
                }
            return CompilationResult(classes, findMainClass(classes.keys, sourceFileName(fileName)))
        } finally {
            workDir.toFile().deleteRecursively()
        }
    }

    private fun sourceFileName(name: String): String = File(name).name.let { if (it.endsWith(".kt")) it else "$it.kt" }

    /**
     * Finds the file facade class generated for [fileName] (`Greeting.kt` -> `GreetingKt`).
     */
    private fun findMainClass(
        classFiles: Set<String>,
        fileName: String,
    ): String {
        val facadeName = "${facadeBaseName(fileName.removeSuffix(".kt"))}Kt.class"
        val classFile =
            classFiles.firstOrNull { it == facadeName || it.endsWith("/$facadeName") }
                ?: classFiles.firstOrNull { !it.contains('$') }
                ?: throw KotlinCompilationException(listOf(compileError("No class generated for $fileName")))
        return classFile.removeSuffix(".class").replace('/', '.')
    }

    /**
     * Same rules as the compiler (PackagePartClassUtils): invalid identifier chars are replaced by `_`,
     * the first char is capitalized.
     */
    private fun facadeBaseName(name: String): String {
        val sanitized = name.map { if (Character.isJavaIdentifierPart(it)) it else '_' }.joinToString("")
        return when {
            sanitized.isEmpty() -> "_"
            !Character.isJavaIdentifierStart(sanitized[0]) -> "_$sanitized"
            else -> sanitized.replaceFirstChar { it.uppercaseChar() }
        }
    }

    private fun compileError(
        message: String,
        location: CompilerMessageSourceLocation? = null,
    ): CompileError {
        // compiler locations are 1-based, editor locations are 0-based
        val start =
            TextPosition(
                ((location?.line ?: 1) - 1).coerceAtLeast(0),
                ((location?.column ?: 1) - 1).coerceAtLeast(0),
            )
        val end =
            location
                ?.takeIf { it.lineEnd > 0 && it.columnEnd > 0 }
                ?.let { TextPosition(it.lineEnd - 1, it.columnEnd - 1) }
                ?.takeIf { it.line > start.line || (it.line == start.line && it.ch > start.ch) }
        // an empty range (e.g. a missing char at the end of a line) highlights the previous char
        val interval =
            when {
                end != null -> TextInterval(start, end)
                start.ch > 0 -> TextInterval(TextPosition(start.line, start.ch - 1), start)
                else -> TextInterval(start, TextPosition(start.line, start.ch + 1))
            }
        return CompileError(interval, message, Severity.ERROR, "red_wavy_line")
    }

    private class ErrorCollector : MessageCollector {
        val errors = mutableListOf<CompileError>()

        override fun clear() {
            errors.clear()
        }

        override fun hasErrors(): Boolean = errors.isNotEmpty()

        override fun report(
            severity: CompilerMessageSeverity,
            message: String,
            location: CompilerMessageSourceLocation?,
        ) {
            if (severity.isError) {
                errors.add(compileError(message, location))
            } else {
                logger.debug { "$severity $message $location" }
            }
        }
    }
}
