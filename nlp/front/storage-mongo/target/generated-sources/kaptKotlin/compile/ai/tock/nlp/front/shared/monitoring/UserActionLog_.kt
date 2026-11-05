@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.front.shared.monitoring

import ai.tock.nlp.front.shared.config.ApplicationDefinition
import java.time.Instant
import kotlin.Any
import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __ActionType: KProperty1<UserActionLog, String?>
  get() = UserActionLog::actionType

private val __ApplicationId: KProperty1<UserActionLog, Id<ApplicationDefinition>?>
  get() = UserActionLog::applicationId

private val __Date: KProperty1<UserActionLog, Instant?>
  get() = UserActionLog::date

private val __Error: KProperty1<UserActionLog, Boolean?>
  get() = UserActionLog::error

private val __Login: KProperty1<UserActionLog, String?>
  get() = UserActionLog::login

private val __Namespace: KProperty1<UserActionLog, String?>
  get() = UserActionLog::namespace

private val __NewData: KProperty1<UserActionLog, Any?>
  get() = UserActionLog::newData

public class UserActionLog_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, UserActionLog?>,
) : KPropertyPath<T, UserActionLog?>(previous, property) {
  public val actionType: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ActionType)

  public val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val error: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Error)

  public val login: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Login)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val newData: KPropertyPath<T, Any?>
    get() = KPropertyPath(this, __NewData)

  public companion object {
    public val ActionType: KProperty1<UserActionLog, String?>
      get() = __ActionType

    public val ApplicationId: KProperty1<UserActionLog, Id<ApplicationDefinition>?>
      get() = __ApplicationId

    public val Date: KProperty1<UserActionLog, Instant?>
      get() = __Date

    public val Error: KProperty1<UserActionLog, Boolean?>
      get() = __Error

    public val Login: KProperty1<UserActionLog, String?>
      get() = __Login

    public val Namespace: KProperty1<UserActionLog, String?>
      get() = __Namespace

    public val NewData: KProperty1<UserActionLog, Any?>
      get() = __NewData
  }
}

public class UserActionLog_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<UserActionLog?>?>,
) : KCollectionPropertyPath<T, UserActionLog?, UserActionLog_<T>>(previous, property) {
  public val actionType: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ActionType)

  public val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val error: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Error)

  public val login: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Login)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val newData: KPropertyPath<T, Any?>
    get() = KPropertyPath(this, __NewData)

  override fun memberWithAdditionalPath(additionalPath: String): UserActionLog_<T> = UserActionLog_(this, customProperty(this, additionalPath))
}

public class UserActionLog_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, UserActionLog?>?>,
) : KMapPropertyPath<T, K, UserActionLog?, UserActionLog_<T>>(previous, property) {
  public val actionType: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __ActionType)

  public val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
    get() = KPropertyPath(this, __ApplicationId)

  public val date: KPropertyPath<T, Instant?>
    get() = KPropertyPath(this, __Date)

  public val error: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Error)

  public val login: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Login)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val newData: KPropertyPath<T, Any?>
    get() = KPropertyPath(this, __NewData)

  override fun memberWithAdditionalPath(additionalPath: String): UserActionLog_<T> = UserActionLog_(this, customProperty(this, additionalPath))
}
