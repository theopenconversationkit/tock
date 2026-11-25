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

package ai.tock.shared.bytecode

import java.io.DataInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile

/**
 * Lists the classes that compiled classes reference, to check that a module never loads a library at runtime.
 *
 * The references come from the constant pool (classes, members and method types used by the code) and from
 * the descriptors of the declared fields and methods. The generic signatures, the annotations and the Kotlin
 * metadata are ignored: the JVM does not load the types they name, so a `(Router) -> Unit` parameter, erased to
 * `Function1`, is not a reference to `Router`.
 */
object BytecodeDependencies {
    /** [fromClass] references [toClass] (internal names, such as `io/vertx/core/Vertx`). */
    data class Reference(
        val fromClass: String,
        val toClass: String,
    ) {
        override fun toString(): String = "${fromClass.replace('/', '.')} -> ${toClass.replace('/', '.')}"
    }

    private val classInDescriptor = Regex("L([^;<]+)[;<]")

    /** The references of all the classes of a directory (target/classes). */
    fun references(classesDir: Path): List<Reference> =
        Files.walk(classesDir).use { paths ->
            paths
                .filter { it.isRegularFile() && it.extension == "class" }
                .toList()
                .flatMap { file -> file.inputStream().use { references(DataInputStream(it.buffered())) } }
        }

    /** The internal names of the classes of a jar, or of a directory of classes. */
    fun classesOf(jar: Path): Set<String> =
        if (Files.isDirectory(jar)) {
            Files.walk(jar).use { paths ->
                paths
                    .filter { it.isRegularFile() && it.extension == "class" }
                    .map {
                        jar
                            .relativize(it)
                            .toString()
                            .replace('\\', '/')
                            .removeSuffix(".class")
                    }.toList()
                    .toSet()
            }
        } else {
            jarClasses(jar)
        }

    private fun jarClasses(jar: Path): Set<String> =
        ZipFile(jar.toFile()).use { zip ->
            zip
                .entries()
                .asSequence()
                .map { it.name }
                .filter { it.endsWith(".class") && !it.startsWith("META-INF/") }
                .map { it.removeSuffix(".class") }
                .toSet()
        }

    /** The jar (or directory) that contains [type]. */
    fun locationOf(type: Class<*>): Path =
        Path.of(
            type.protectionDomain.codeSource.location
                .toURI(),
        )

    /**
     * The references of the classes of [classesDir] to a [forbidden] class, except from the [allowed] classes.
     */
    fun violations(
        classesDir: Path,
        forbidden: (String) -> Boolean,
        allowed: (String) -> Boolean = { false },
    ): List<Reference> =
        references(classesDir)
            .filter { forbidden(it.toClass) && !allowed(it.fromClass) }
            .distinct()
            .sortedWith(compareBy({ it.fromClass }, { it.toClass }))

    private fun references(input: DataInputStream): List<Reference> {
        input.readInt() // magic
        input.readUnsignedShort() // minor version
        input.readUnsignedShort() // major version
        val count = input.readUnsignedShort()
        val utf8 = arrayOfNulls<String>(count)
        val classNames = mutableListOf<Int>()
        val classNameOfEntry = IntArray(count)
        val descriptors = mutableListOf<Int>()
        var i = 1
        while (i < count) {
            when (val tag = input.readUnsignedByte()) {
                1 -> {
                    utf8[i] = input.readUTF()
                }

                7 -> {
                    classNameOfEntry[i] = input.readUnsignedShort()
                    classNames += classNameOfEntry[i]
                }

                8, 19, 20 -> {
                    input.readUnsignedShort()
                }

                16 -> {
                    descriptors += input.readUnsignedShort()
                }

                3, 4 -> {
                    input.readInt()
                }

                5, 6 -> {
                    input.readLong()
                    i++
                }

                9, 10, 11, 17, 18 -> {
                    input.readInt()
                }

                12 -> {
                    input.readUnsignedShort()
                    descriptors += input.readUnsignedShort()
                }

                15 -> {
                    input.readUnsignedByte()
                    input.readUnsignedShort()
                }

                else -> {
                    error("unknown constant pool tag $tag")
                }
            }
            i++
        }
        input.readUnsignedShort() // access flags
        val thisClass = utf8[classNameOfEntry[input.readUnsignedShort()]]
        input.readUnsignedShort() // super class: already a class constant
        repeat(input.readUnsignedShort()) { input.readUnsignedShort() } // interfaces: already class constants
        repeat(2) {
            // fields, then methods
            repeat(input.readUnsignedShort()) {
                input.readUnsignedShort() // access flags
                input.readUnsignedShort() // name
                descriptors += input.readUnsignedShort()
                repeat(input.readUnsignedShort()) {
                    input.readUnsignedShort() // attribute name
                    input.skipNBytes(input.readInt().toLong())
                }
            }
        }
        val referenced =
            classNames.mapNotNull { utf8[it] }.flatMap { name ->
                if (name.startsWith("[")) classInDescriptor.findAll(name).map { it.groupValues[1] }.toList() else listOf(name)
            } + descriptors.mapNotNull { utf8[it] }.flatMap { d -> classInDescriptor.findAll(d).map { it.groupValues[1] }.toList() }
        val from = thisClass ?: error("no class name")
        return referenced.filter { it != from }.toSet().map { Reference(from, it) }
    }
}
