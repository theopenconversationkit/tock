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

package ai.tock.shared.changes

import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

/**
 * The checks of the changes that a storage cannot push to the process (no change stream).
 *
 * A storage registers its check; a process frozen between its requests (the serverless runtime) runs them at
 * the start of each request, as a timer would never fire. A check must be cheap when nothing changed: it is
 * up to it to limit how often it reads its storage.
 *
 * The process runs [prepareAll] before it loads its data, so that a check knows the state that was loaded.
 */
object ChangeChecks {
    private val logger = KotlinLogging.logger {}

    private class Check(
        val prepare: () -> Unit,
        val check: () -> Unit,
    )

    private val checks = ConcurrentHashMap<String, Check>()

    /**
     * Registers [check] under [name], and what to do before the data is loaded ([prepare]): a second
     * registration of the same name replaces the first one.
     */
    fun register(
        name: String,
        prepare: () -> Unit = {},
        check: () -> Unit,
    ) {
        checks[name] = Check(prepare, check)
    }

    fun unregister(name: String) {
        checks.remove(name)
    }

    /** Prepares the registered checks, before the data is loaded. */
    fun prepareAll() = run("preparation") { it.prepare() }

    /** Runs the registered checks: a failed check is logged, and does not prevent the next ones. */
    fun runAll() = run("check") { it.check() }

    private fun run(
        step: String,
        block: (Check) -> Unit,
    ) {
        checks.forEach { (name, check) ->
            try {
                block(check)
            } catch (e: Exception) {
                logger.error(e) { "the $step of the $name changes failed" }
            }
        }
    }
}
