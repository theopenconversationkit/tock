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

package ai.tock.bot.test

import java.util.logging.Logger

typealias TConsumer<T> = (T) -> Unit
typealias TSupplier<T> = () -> T
typealias TFunction<T,R> = (T) -> R
typealias TRunnable = () -> Unit

enum class FnType {
    CONSUMER,
    SUPPLIER,
    FUNCTION,
    RUNNABLE
}

class TestCase<T, R>(val name: String) {

    private val givenStatements = mutableListOf<Pair<FnType, Any>>()
    private val whenStatements = mutableListOf<Pair<FnType, Any>>()
    private val thenStatements = mutableListOf<Pair<FnType, Any>>()

    private val givenInfos = mutableListOf("\nGIVEN : ")
    private val whenInfos = mutableListOf("WHEN : ")
    private val thenInfos = mutableListOf("THEN : ")

    private var state: T? = null
    private var result: R? = null

    companion object {
        private val logger = Logger.getLogger(TestCase::class.simpleName)
    }

    @JvmName("givenCn")
    fun given(message: String, fn: TConsumer<T?>) : Given<T,R> {
        givenInfos.add(message)
        givenStatements.add(FnType.CONSUMER to fn)
        return Given(this)
    }

    @JvmName("givenSp")
    fun given(message: String, fn: TSupplier<T?>) : Given<T,R> {
        givenInfos.add(message)
        givenStatements.add(FnType.SUPPLIER to fn)
        return Given(this)
    }

    @JvmName("givenFn")
    fun given(message: String, fn: TFunction<T?, T?>) : Given<T,R> {
        givenInfos.add(message)
        givenStatements.add(FnType.FUNCTION to fn)
        return Given(this)
    }

    @JvmName("givenRn")
    fun given(message: String, fn: TRunnable) : Given<T,R> {
        givenInfos.add(message)
        givenStatements.add(FnType.RUNNABLE to fn)
        return Given(this)
    }

    @JvmName("andGivenCn")
    fun andGiven(message: String, fn: TConsumer<T?>) {
        givenInfos.add(message)
        givenStatements.add(FnType.CONSUMER to fn)
    }

    @JvmName("andGivenSp")
    fun andGiven(message: String, fn: TSupplier<T?>) {
        givenInfos.add(message)
        givenStatements.add(FnType.SUPPLIER to fn)
    }

    @JvmName("andGivenFn")
    fun andGiven(message: String, fn: TFunction<T?, T?>){
        givenInfos.add(message)
        givenStatements.add(FnType.FUNCTION to fn)
    }

    @JvmName("andGivenRn")
    fun andGiven(message: String, fn: TRunnable) {
        givenInfos.add(message)
        givenStatements.add(FnType.RUNNABLE to fn)
    }

    @JvmName("whenCn")
    fun `when`(message: String, fn: TConsumer<T?>): When<T,R> {
        whenInfos.add(message)
        whenStatements.add(FnType.CONSUMER to fn)
        return When(this)
    }

    @JvmName("whenFn")
    fun `when`(message: String, fn: TFunction<T?,R?>): When<T,R> {
        whenInfos.add(message)
        whenStatements.add(FnType.FUNCTION to fn)
        return When(this)
    }

    @JvmName("whenSp")
    fun `when`(message: String, fn: TSupplier<R?>): When<T,R> {
        whenInfos.add(message)
        whenStatements.add(FnType.SUPPLIER to fn)
        return When(this)
    }

    @JvmName("andWhenCn")
    fun andWhen(message: String, fn: TConsumer<T?>) {
        whenInfos.add(message)
        whenStatements.add(FnType.CONSUMER to fn)
    }

    @JvmName("andWhenFn")
    fun andWhen(message: String, fn: TFunction<T?,R?>) {
        whenInfos.add(message)
        whenStatements.add(FnType.FUNCTION to fn)
    }

    @JvmName("andWhenSp")
    fun andWhen(message: String, fn: TSupplier<R?>) {
        whenInfos.add(message)
        whenStatements.add(FnType.SUPPLIER to fn)
    }

    fun then(message: String, fn: TConsumer<R?>) : Then<T,R> {
        thenInfos.add(message)
        thenStatements.add(FnType.CONSUMER to fn)
        return Then(this)
    }

    fun then(message: String, fn: TRunnable) : Then<T,R> {
        thenInfos.add(message)
        thenStatements.add(FnType.RUNNABLE to fn)
        return Then(this)
    }

    fun andThen(message: String, fn: TConsumer<R>)  {
        thenInfos.add(message)
        thenStatements.add(FnType.CONSUMER to fn)
    }

    fun andThen(message: String, fn: TRunnable)  {
        thenInfos.add(message)
        thenStatements.add(FnType.RUNNABLE to fn)
    }


    @Suppress("UNCHECKED_CAST")
    fun run() {

        log()

        givenStatements.forEach {(ty, fn) ->
            when(ty) {
                FnType.RUNNABLE -> (fn as TRunnable).invoke()
                FnType.CONSUMER -> (fn as TConsumer<T?>).invoke(state)
                FnType.FUNCTION -> state = (fn as TFunction<T?, T?>).invoke(state)
                FnType.SUPPLIER -> state = (fn as TSupplier<T>).invoke()
            }
        }

        whenStatements.forEach {(ty, fn) ->
            when(ty) {
                FnType.CONSUMER -> (fn as TConsumer<T?>).invoke(state)
                FnType.FUNCTION -> result = (fn as TFunction<T?, R?>).invoke(state)
                FnType.SUPPLIER -> result = (fn as TSupplier<R?>).invoke()
                FnType.RUNNABLE -> error("Runnable is not supported for When statement")
            }
        }

        thenStatements.forEach {(ty, fn) ->
            when(ty) {
                FnType.RUNNABLE -> (fn as TRunnable).invoke()
                FnType.CONSUMER -> (fn as TConsumer<R?>).invoke(result)
                FnType.SUPPLIER -> error("Supplier  is not supported for Then statement")
                FnType.FUNCTION -> error("Function  is not supported for Then statement")
            }
        }
    }

    private fun log() {
        mutableListOf(
            "***************************************************************************",
            "TEST CASE ::: $name",
            "***************************************************************************",
        ).also {
            it.addAll(givenInfos)
            it.addAll(whenInfos)
            it.addAll(thenInfos)
        }.joinToString("\n").let { logger.info("\n$it\n") }
    }

}

class Given<T,R>(private val testCase: TestCase<T,R>){

    @JvmName("andCn")
    fun and(message: String, fn: TConsumer<T?>): Given<T,R> {
        testCase.andGiven(message, fn)
        return this
    }

    @JvmName("andRn")
    fun and(message: String, fn: TRunnable): Given<T,R> {
        testCase.andGiven(message, fn)
        return this
    }

    @JvmName("andSp")
    fun and(message: String, fn: TSupplier<T?>): Given<T,R> {
        testCase.andGiven(message, fn)
        return this
    }

    @JvmName("andFn")
    fun and(message: String, fn: TFunction<T?, T?>): Given<T,R> {
        testCase.andGiven(message, fn)
        return this
    }

    @JvmName("whenCn")
    fun `when`(message: String, fn: TConsumer<T?>) : When<T,R> {
        return testCase.`when`(message, fn)
    }

    @JvmName("whenSp")
    fun `when`(message: String, fn: TSupplier<R?>) : When<T,R> {
        return testCase.`when`(message, fn)
    }
    @JvmName("whenFn")
    fun `when`(message: String, fn: TFunction<T?,R?>) : When<T,R> {
        return testCase.`when`(message, fn)
    }
}

class When<T,R>(private val testCase: TestCase<T,R>){

    @JvmName("andSp")
    fun  and(message: String, fn: TSupplier<R>): When<T,R> {
        testCase.andWhen(message, fn)
        return this
    }

    @JvmName("andCn")
    fun  and(message: String, fn: TConsumer<T?>): When<T,R> {
        testCase.andWhen(message, fn)
        return this
    }

    @JvmName("andFn")
    fun  and(message: String, fn: TFunction<T?,R?>): When<T,R> {
        testCase.andWhen(message, fn)
        return this
    }

    fun then(message: String, fn: TConsumer<R?>) : Then<T,R> {
        return testCase.then(message, fn)
    }

    fun then(message: String, fn: TRunnable) : Then<T,R> {
        return testCase.then(message, fn)
    }
}

class Then<T,R>(private val testCase: TestCase<T,R>){

    fun and(message: String, fn: TConsumer<R?>): Then<T,R> {
        testCase.andThen(message, fn)
        return this
    }

    fun and(message: String, fn: TRunnable): Then<T,R> {
        testCase.andThen(message, fn)
        return this
    }

    fun run() {
        testCase.run()
    }
}
