@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.admin.test

import java.time.Duration
import java.time.Instant
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KCollectionSimplePropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val ___id: KProperty1<TestPlanExecution, Id<TestPlanExecution>?>
  get() = TestPlanExecution::_id

private val __Date: KProperty1<TestPlanExecution, Instant?>
  get() = TestPlanExecution::date

private val __Dialogs: KProperty1<TestPlanExecution, List<DialogExecutionReport>?>
  get() = TestPlanExecution::dialogs

private val __Duration: KProperty1<TestPlanExecution, Duration?>
  get() = TestPlanExecution::duration

private val __NbErrors: KProperty1<TestPlanExecution, Int?>
  get() = TestPlanExecution::nbErrors

private val __Status: KProperty1<TestPlanExecution, TestPlanExecutionStatus?>
  get() = TestPlanExecution::status

private val __TestPlanId: KProperty1<TestPlanExecution, Id<TestPlan>?>
  get() = TestPlanExecution::testPlanId

public class TestPlanExecution_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, TestPlanExecution?>,
) : KPropertyPath<T, TestPlanExecution?>(previous, property) {
  public val _id: KPropertyPath<T, Id<TestPlanExecution>?>
    get() = KPropertyPath(this, ___id)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val dialogs: KCollectionSimplePropertyPath<T, DialogExecutionReport?>
    get() = KCollectionSimplePropertyPath(this, __Dialogs)

  public val duration: KPropertyPath<T, Duration?>
    get() = KPropertyPath(this, __Duration)

  public val nbErrors: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __NbErrors)

  public val status: KPropertyPath<T, TestPlanExecutionStatus?>
    get() = KPropertyPath(this, __Status)

  public val testPlanId: KPropertyPath<T, Id<TestPlan>?>
    get() = KPropertyPath(this, __TestPlanId)

  public companion object {
    public val _id: KProperty1<TestPlanExecution, Id<TestPlanExecution>?>
      get() = ___id

    public val Date: KProperty1<TestPlanExecution, Instant?>
      get() = __Date

    public val Dialogs: KCollectionSimplePropertyPath<TestPlanExecution, DialogExecutionReport?>
      get() = KCollectionSimplePropertyPath(null, __Dialogs)

    public val Duration: KProperty1<TestPlanExecution, Duration?>
      get() = __Duration

    public val NbErrors: KProperty1<TestPlanExecution, Int?>
      get() = __NbErrors

    public val Status: KProperty1<TestPlanExecution, TestPlanExecutionStatus?>
      get() = __Status

    public val TestPlanId: KProperty1<TestPlanExecution, Id<TestPlan>?>
      get() = __TestPlanId
  }
}

public class TestPlanExecution_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<TestPlanExecution?>?>,
) : KCollectionPropertyPath<T, TestPlanExecution?, TestPlanExecution_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<TestPlanExecution>?>
    get() = KPropertyPath(this, ___id)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val dialogs: KCollectionSimplePropertyPath<T, DialogExecutionReport?>
    get() = KCollectionSimplePropertyPath(this, __Dialogs)

  public val duration: KPropertyPath<T, Duration?>
    get() = KPropertyPath(this, __Duration)

  public val nbErrors: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __NbErrors)

  public val status: KPropertyPath<T, TestPlanExecutionStatus?>
    get() = KPropertyPath(this, __Status)

  public val testPlanId: KPropertyPath<T, Id<TestPlan>?>
    get() = KPropertyPath(this, __TestPlanId)

  override fun memberWithAdditionalPath(additionalPath: String): TestPlanExecution_<T> = TestPlanExecution_(this, customProperty(this, additionalPath))
}

public class TestPlanExecution_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, TestPlanExecution?>?>,
) : KMapPropertyPath<T, K, TestPlanExecution?, TestPlanExecution_<T>>(previous, property) {
  public val _id: KPropertyPath<T, Id<TestPlanExecution>?>
    get() = KPropertyPath(this, ___id)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val dialogs: KCollectionSimplePropertyPath<T, DialogExecutionReport?>
    get() = KCollectionSimplePropertyPath(this, __Dialogs)

  public val duration: KPropertyPath<T, Duration?>
    get() = KPropertyPath(this, __Duration)

  public val nbErrors: KPropertyPath<T, Int?>
    get() = KPropertyPath(this, __NbErrors)

  public val status: KPropertyPath<T, TestPlanExecutionStatus?>
    get() = KPropertyPath(this, __Status)

  public val testPlanId: KPropertyPath<T, Id<TestPlan>?>
    get() = KPropertyPath(this, __TestPlanId)

  override fun memberWithAdditionalPath(additionalPath: String): TestPlanExecution_<T> = TestPlanExecution_(this, customProperty(this, additionalPath))
}
