package ai.tock.bot.admin.test

import java.time.Duration
import java.time.Instant
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __TestPlanId: KProperty1<TestPlanExecution, Id<TestPlan>?>
    get() = TestPlanExecution::testPlanId
private val __Dialogs: KProperty1<TestPlanExecution, List<DialogExecutionReport>?>
    get() = TestPlanExecution::dialogs
private val __NbErrors: KProperty1<TestPlanExecution, Int?>
    get() = TestPlanExecution::nbErrors
private val __Date: KProperty1<TestPlanExecution, Instant?>
    get() = TestPlanExecution::date
private val __Duration: KProperty1<TestPlanExecution, Duration?>
    get() = TestPlanExecution::duration
private val ___id: KProperty1<TestPlanExecution, Id<TestPlanExecution>?>
    get() = TestPlanExecution::_id
private val __Status: KProperty1<TestPlanExecution, TestPlanExecutionStatus?>
    get() = TestPlanExecution::status
class TestPlanExecution_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        TestPlanExecution?>) : KPropertyPath<T, TestPlanExecution?>(previous,property) {
    val testPlanId: KPropertyPath<T, Id<TestPlan>?>
        get() = KPropertyPath(this,__TestPlanId)

    val dialogs: KCollectionSimplePropertyPath<T, DialogExecutionReport?>
        get() = KCollectionSimplePropertyPath(this,TestPlanExecution::dialogs)

    val nbErrors: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__NbErrors)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    val duration: KPropertyPath<T, Duration?>
        get() = KPropertyPath(this,__Duration)

    val _id: KPropertyPath<T, Id<TestPlanExecution>?>
        get() = KPropertyPath(this,___id)

    val status: KPropertyPath<T, TestPlanExecutionStatus?>
        get() = KPropertyPath(this,__Status)

    companion object {
        val TestPlanId: KProperty1<TestPlanExecution, Id<TestPlan>?>
            get() = __TestPlanId
        val Dialogs: KCollectionSimplePropertyPath<TestPlanExecution, DialogExecutionReport?>
            get() = KCollectionSimplePropertyPath(null, __Dialogs)
        val NbErrors: KProperty1<TestPlanExecution, Int?>
            get() = __NbErrors
        val Date: KProperty1<TestPlanExecution, Instant?>
            get() = __Date
        val Duration: KProperty1<TestPlanExecution, Duration?>
            get() = __Duration
        val _id: KProperty1<TestPlanExecution, Id<TestPlanExecution>?>
            get() = ___id
        val Status: KProperty1<TestPlanExecution, TestPlanExecutionStatus?>
            get() = __Status}
}

class TestPlanExecution_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<TestPlanExecution>?>) : KCollectionPropertyPath<T, TestPlanExecution?,
        TestPlanExecution_<T>>(previous,property) {
    val testPlanId: KPropertyPath<T, Id<TestPlan>?>
        get() = KPropertyPath(this,__TestPlanId)

    val dialogs: KCollectionSimplePropertyPath<T, DialogExecutionReport?>
        get() = KCollectionSimplePropertyPath(this,TestPlanExecution::dialogs)

    val nbErrors: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__NbErrors)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    val duration: KPropertyPath<T, Duration?>
        get() = KPropertyPath(this,__Duration)

    val _id: KPropertyPath<T, Id<TestPlanExecution>?>
        get() = KPropertyPath(this,___id)

    val status: KPropertyPath<T, TestPlanExecutionStatus?>
        get() = KPropertyPath(this,__Status)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): TestPlanExecution_<T> =
            TestPlanExecution_(this, customProperty(this, additionalPath))}

class TestPlanExecution_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        TestPlanExecution>?>) : KMapPropertyPath<T, K, TestPlanExecution?,
        TestPlanExecution_<T>>(previous,property) {
    val testPlanId: KPropertyPath<T, Id<TestPlan>?>
        get() = KPropertyPath(this,__TestPlanId)

    val dialogs: KCollectionSimplePropertyPath<T, DialogExecutionReport?>
        get() = KCollectionSimplePropertyPath(this,TestPlanExecution::dialogs)

    val nbErrors: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__NbErrors)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    val duration: KPropertyPath<T, Duration?>
        get() = KPropertyPath(this,__Duration)

    val _id: KPropertyPath<T, Id<TestPlanExecution>?>
        get() = KPropertyPath(this,___id)

    val status: KPropertyPath<T, TestPlanExecutionStatus?>
        get() = KPropertyPath(this,__Status)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): TestPlanExecution_<T> =
            TestPlanExecution_(this, customProperty(this, additionalPath))}
