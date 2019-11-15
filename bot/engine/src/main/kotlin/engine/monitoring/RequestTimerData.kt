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

package ai.tock.bot.engine.monitoring

/**
 *
 */
open class RequestTimerData(val type: String,
                            val start: Long = System.currentTimeMillis(),
                            var error: Boolean = false,
                            var message: String? = null,
                            var throwable: Throwable? = null) {

    override fun toString(): String {
        return "type='$type', start=$start, error=$error, message=$message, throwable=$throwable"
    }
}