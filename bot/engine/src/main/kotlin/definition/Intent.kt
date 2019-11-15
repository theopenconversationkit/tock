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

package ai.tock.bot.definition

import ai.tock.shared.TOCK_NAMESPACE

/**
 * An intent definition.
 */
data class Intent(
    /**
     * The name of the intent.
     */
    val name: String
) : IntentAware {

    companion object {
        /**
         * The unknown intent.
         */
        val unknown: Intent = Intent("$TOCK_NAMESPACE:unknown")
        /**
         * The keyword intent.
         */
        val keyword: Intent = Intent("$TOCK_NAMESPACE:keyword")
    }

    override fun wrappedIntent(): Intent = this
}