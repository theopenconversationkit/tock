package fr.vsct.tock.bot.admin.test

import java.time.Duration
import java.time.Instant
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

class TestPlanExecution_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, TestPlanExecution?>) : KPropertyPath<T, TestPlanExecution?>(previous,property) {
    val testPlanId: KPropertyPath<T, Id<TestPlan>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.bot.admin.test.TestPlan>?>(this,TestPlanExecution::testPlanId)

    val dialogs: KCollectionSimplePropertyPath<T, DialogExecutionReport?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, fr.vsct.tock.bot.admin.test.DialogExecutionReport?>(this,TestPlanExecution::dialogs)

    val nbErrors: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,TestPlanExecution::nbErrors)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,TestPlanExecution::date)

    val duration: KPropertyPath<T, Duration?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Duration?>(this,TestPlanExecution::duration)

    val _id: KPropertyPath<T, Id<TestPlanExecution>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.bot.admin.test.TestPlanExecution>?>(this,TestPlanExecution::_id)
    companion object {
        val TestPlanId: KProperty1<TestPlanExecution, Id<TestPlan>?>
            get() = TestPlanExecution::testPlanId
        val Dialogs: KCollectionSimplePropertyPath<TestPlanExecution, DialogExecutionReport?>
            get() = KCollectionSimplePropertyPath(null, TestPlanExecution::dialogs)
        val NbErrors: KProperty1<TestPlanExecution, Int?>
            get() = TestPlanExecution::nbErrors
        val Date: KProperty1<TestPlanExecution, Instant?>
            get() = TestPlanExecution::date
        val Duration: KProperty1<TestPlanExecution, Duration?>
            get() = TestPlanExecution::duration
        val _id: KProperty1<TestPlanExecution, Id<TestPlanExecution>?>
            get() = TestPlanExecution::_id}
}

class TestPlanExecution_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<TestPlanExecution>?>) : KCollectionPropertyPath<T, TestPlanExecution?, TestPlanExecution_<T>>(previous,property) {
    val testPlanId: KPropertyPath<T, Id<TestPlan>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.bot.admin.test.TestPlan>?>(this,TestPlanExecution::testPlanId)

    val dialogs: KCollectionSimplePropertyPath<T, DialogExecutionReport?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, fr.vsct.tock.bot.admin.test.DialogExecutionReport?>(this,TestPlanExecution::dialogs)

    val nbErrors: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,TestPlanExecution::nbErrors)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,TestPlanExecution::date)

    val duration: KPropertyPath<T, Duration?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Duration?>(this,TestPlanExecution::duration)

    val _id: KPropertyPath<T, Id<TestPlanExecution>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.bot.admin.test.TestPlanExecution>?>(this,TestPlanExecution::_id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): TestPlanExecution_<T> = TestPlanExecution_(this, customProperty(this, additionalPath))}

class TestPlanExecution_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, TestPlanExecution>?>) : KMapPropertyPath<T, K, TestPlanExecution?, TestPlanExecution_<T>>(previous,property) {
    val testPlanId: KPropertyPath<T, Id<TestPlan>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.bot.admin.test.TestPlan>?>(this,TestPlanExecution::testPlanId)

    val dialogs: KCollectionSimplePropertyPath<T, DialogExecutionReport?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, fr.vsct.tock.bot.admin.test.DialogExecutionReport?>(this,TestPlanExecution::dialogs)

    val nbErrors: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,TestPlanExecution::nbErrors)

    val date: KPropertyPath<T, Instant?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Instant?>(this,TestPlanExecution::date)

    val duration: KPropertyPath<T, Duration?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.time.Duration?>(this,TestPlanExecution::duration)

    val _id: KPropertyPath<T, Id<TestPlanExecution>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.bot.admin.test.TestPlanExecution>?>(this,TestPlanExecution::_id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): TestPlanExecution_<T> = TestPlanExecution_(this, customProperty(this, additionalPath))}
