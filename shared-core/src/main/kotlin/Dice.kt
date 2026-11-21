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

package ai.tock.shared

import org.bson.types.ObjectId
import java.util.Random
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

/**
 * Manage random things.
 */
object Dice {
    private fun random(): Random = ThreadLocalRandom.current()

    /**
     * Choose randomly an item in a list.
     */
    fun <T> choose(list: List<T>): T = list[index(list)]

    /**
     * Return a random index in a [Collection].
     */
    fun index(col: Collection<*>): Int = newInt(col.size)

    /**
     * Return a random int between 0 and max (excluded).
     */
    fun newInt(max: Int): Int = random().nextInt(max)

    /**
     * Return a new random [Id].
     */
    fun newId(): String =
        try {
            ObjectId().toHexString()
        } catch (e: NoClassDefFoundError) {
            UUID.randomUUID().toString()
        }
}
