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

package ai.tock.bot.definition

import com.fasterxml.jackson.annotation.JsonValue
import java.util.Collections
import kotlin.reflect.safeCast

/**
 * An arbitrary data store for dialogs, intended for use by bot stories
 *
 * In contexts where the data is persisted, the key's type is used for (de)serialization
 *
 * @see ai.tock.bot.engine.BotBus.contextValue
 * @see ai.tock.bot.engine.BotBus.changeContextValue
 * @see ai.tock.bot.engine.BotBus.getBusContextValue
 * @see ai.tock.bot.engine.BotBus.setBusContextValue
 */
interface DialogContext {
    companion object {
        /**
         * The empty immutable [DialogContext]
         */
        val EMPTY: DialogContext = DialogContextMap.EMPTY

        fun <T : Any> of(entry: Pair<DialogContextKey<T>, T>): DialogContext = DialogContextMap(entry)

        fun <T1 : Any, T2 : Any> of(
            entry1: Pair<DialogContextKey<T1>, T1>,
            entry2: Pair<DialogContextKey<T2>, T2>,
        ): DialogContext = DialogContextMap(entry1, entry2)

        fun <T1 : Any, T2 : Any, T3 : Any> of(
            entry1: Pair<DialogContextKey<T1>, T1>,
            entry2: Pair<DialogContextKey<T2>, T2>,
            entry3: Pair<DialogContextKey<T3>, T3>,
        ): DialogContext = DialogContextMap(entry1, entry2, entry3)

        fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any> of(
            entry1: Pair<DialogContextKey<T1>, T1>,
            entry2: Pair<DialogContextKey<T2>, T2>,
            entry3: Pair<DialogContextKey<T3>, T3>,
            entry4: Pair<DialogContextKey<T4>, T4>,
        ): DialogContext = DialogContextMap(entry1, entry2, entry3, entry4)
    }

    /**
     * @return a Map view of this context store
     */
    fun asMap(): Map<DialogContextKey<*>, Any>

    operator fun <T : Any> get(key: DialogContextKey<T>): T?

    operator fun <T : Any> set(
        key: DialogContextKey<T>,
        value: T,
    )

    fun <T : Any> add(entry: Pair<DialogContextKey<T>, T>)
}

interface MutableDialogContext : DialogContext {
    override fun asMap(): MutableMap<DialogContextKey<*>, Any>

    operator fun <T : Any> plusAssign(entry: Pair<DialogContextKey<T>, T>)

    fun <T : Any> put(
        key: DialogContextKey<T>,
        value: T,
    )

    fun remove(key: DialogContextKey<*>)

    fun clear()
}

class DialogContextMap private constructor(
    @JsonValue private val entries: MutableMap<DialogContextKey<*>, Any>,
) : MutableDialogContext {
    companion object {
        /**
         * The empty context map (immutable)
         */
        val EMPTY = DialogContextMap(Collections.emptyMap())

        operator fun <T : Any> invoke(entry: Pair<DialogContextKey<T>, T>): DialogContextMap =
            DialogContextMap().apply {
                add(entry)
            }

        operator fun <T1 : Any, T2 : Any> invoke(
            entry1: Pair<DialogContextKey<T1>, T1>,
            entry2: Pair<DialogContextKey<T2>, T2>,
        ): DialogContextMap =
            DialogContextMap().apply {
                add(entry1)
                add(entry2)
            }

        operator fun <T1 : Any, T2 : Any, T3 : Any> invoke(
            entry1: Pair<DialogContextKey<T1>, T1>,
            entry2: Pair<DialogContextKey<T2>, T2>,
            entry3: Pair<DialogContextKey<T3>, T3>,
        ): DialogContextMap =
            DialogContextMap().apply {
                add(entry1)
                add(entry2)
                add(entry3)
            }

        operator fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any> invoke(
            entry1: Pair<DialogContextKey<T1>, T1>,
            entry2: Pair<DialogContextKey<T2>, T2>,
            entry3: Pair<DialogContextKey<T3>, T3>,
            entry4: Pair<DialogContextKey<T4>, T4>,
        ): DialogContextMap =
            DialogContextMap().apply {
                add(entry1)
                add(entry2)
                add(entry3)
                add(entry4)
            }
    }

    constructor() : this(mutableMapOf())
    constructor(other: DialogContext) : this(other.asMap().toMutableMap())

    override fun asMap() = entries

    override operator fun <T : Any> get(key: DialogContextKey<T>): T? {
        return key.type.safeCast(entries[key])
    }

    override fun <T : Any> set(
        key: DialogContextKey<T>,
        value: T,
    ) {
        entries[key] = value
    }

    override operator fun <T : Any> plusAssign(entry: Pair<DialogContextKey<T>, T>) {
        set(entry.first, entry.second)
    }

    override fun <T : Any> add(entry: Pair<DialogContextKey<T>, T>) {
        set(entry.first, entry.second)
    }

    override fun <T : Any> put(
        key: DialogContextKey<T>,
        value: T,
    ) {
        entries[key] = value
    }

    override fun remove(key: DialogContextKey<*>) {
        entries.remove(key)
    }

    override fun clear() {
        entries.clear()
    }

    override fun toString(): String {
        return entries.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DialogContextMap

        return entries == other.entries
    }

    override fun hashCode(): Int {
        return entries.hashCode()
    }
}
