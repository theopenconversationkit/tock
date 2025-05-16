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

package ai.tock.bot.admin.model

interface ToValidate {
    fun validate() : List<String>
}

/**
 * Validate the request, if any bad request add error into the list
 * if no errors return an empty list
 * otherwise return a list of errors
 */
data class Valid<T: ToValidate> (val data: T) {
    init {
        data.validate().let {
            if (it.isNotEmpty()) throw ValidationError(it.joinToString("\n"))
        }
    }
}

data class ValidationError(override val message: String?) : Exception(message)

