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

package ai.tock.bot.admin.answer

import ai.tock.bot.admin.bot.BotVersion
import ai.tock.bot.definition.StoryDefinition
import ai.tock.shared.error
import mu.KotlinLogging
import java.time.Instant
import java.time.Instant.now

/**
 * A version of [ScriptAnswerConfiguration]
 * - useful to be compliant with the current tock and bot versions.
 */
class ScriptAnswerVersionedConfiguration(
    val script: String,
    val compiledCode: List<Pair<String, ByteArray>>,
    val version: BotVersion,
    val mainClassName: String,
    val date: Instant = now()
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private class ScriptClassLoader(
        val classes: Map<String, ByteArray>
    ) : ClassLoader(ScriptClassLoader::class.java.classLoader) {

        @Override
        override fun findClass(name: String): Class<*> =
            classes[name]?.run { defineClass(name, this, 0, this.size) } ?: super.findClass(name)
    }

    val storyDefinition: StoryDefinition? by lazy {
        try {
            val classLoader = ScriptClassLoader(compiledCode.toMap())
            val c = classLoader.loadClass(mainClassName)
            c.declaredFields
                .firstOrNull { field ->
                    StoryDefinition::class.java.isAssignableFrom(field.type)
                }
                ?.run {
                    isAccessible = true
                    get(null) as? StoryDefinition
                }
        } catch (t: Throwable) {
            logger.error(t)
            null
        }
    }
}
