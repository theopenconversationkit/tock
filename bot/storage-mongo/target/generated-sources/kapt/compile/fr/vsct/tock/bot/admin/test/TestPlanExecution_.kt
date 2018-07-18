package fr.vsct.tock.bot.admin.test

import java.time.Duration
import java.time.Instant
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

class TestPlanExecution_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, TestPlanExecution?>) : KPropertyPath<T, TestPlanExecution?>(previous,property) {
    val testPlanId: KProperty1<T, Id<TestPlan>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlanExecution::testPlanId)

    val dialogs: KProperty1<T, List<DialogExecutionReport>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlanExecution::dialogs)

    val nbErrors: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlanExecution::nbErrors)

    val date: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlanExecution::date)

    val duration: KProperty1<T, Duration?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlanExecution::duration)

    val _id: KProperty1<T, Id<TestPlanExecution>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlanExecution::_id)
    companion object {
        val TestPlanId: KProperty1<TestPlanExecution, Id<TestPlan>?>
            get() = TestPlanExecution::testPlanId
        val Dialogs: KProperty1<TestPlanExecution, List<DialogExecutionReport>?>
            get() = TestPlanExecution::dialogs
        val NbErrors: KProperty1<TestPlanExecution, Int?>
            get() = TestPlanExecution::nbErrors
        val Date: KProperty1<TestPlanExecution, Instant?>
            get() = TestPlanExecution::date
        val Duration: KProperty1<TestPlanExecution, Duration?>
            get() = TestPlanExecution::duration
        val _id: KProperty1<TestPlanExecution, Id<TestPlanExecution>?>
            get() = TestPlanExecution::_id}
}

class TestPlanExecution_Col<T>(
        previous: KPropertyPath<T, *>?,
        property: KProperty1<*, Collection<TestPlanExecution>?>,
        additionalPath: String? = null
) : KCollectionPropertyPath<T, TestPlanExecution?>(previous,property,additionalPath) {
    override val arrayProjection: TestPlanExecution_Col<T>
        @Suppress("UNCHECKED_CAST")
        get() = TestPlanExecution_Col(null, this as KProperty1<*, Collection<TestPlanExecution>?>, "$")

    val testPlanId: KProperty1<T, Id<TestPlan>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlanExecution::testPlanId)

    val dialogs: KProperty1<T, List<DialogExecutionReport>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlanExecution::dialogs)

    val nbErrors: KProperty1<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlanExecution::nbErrors)

    val date: KProperty1<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlanExecution::date)

    val duration: KProperty1<T, Duration?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlanExecution::duration)

    val _id: KProperty1<T, Id<TestPlanExecution>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,TestPlanExecution::_id)
}
