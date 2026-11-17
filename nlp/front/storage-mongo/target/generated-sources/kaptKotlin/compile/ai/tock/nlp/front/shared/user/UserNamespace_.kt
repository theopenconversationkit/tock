@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.front.shared.user

import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __Current: KProperty1<UserNamespace, Boolean?>
  get() = UserNamespace::current

private val __Label: KProperty1<UserNamespace, String?>
  get() = UserNamespace::label

private val __Login: KProperty1<UserNamespace, String?>
  get() = UserNamespace::login

private val __Namespace: KProperty1<UserNamespace, String?>
  get() = UserNamespace::namespace

private val __Owner: KProperty1<UserNamespace, Boolean?>
  get() = UserNamespace::owner

public class UserNamespace_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, UserNamespace?>,
) : KPropertyPath<T, UserNamespace?>(previous, property) {
  public val current: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Current)

  public val label: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Label)

  public val login: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Login)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val owner: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Owner)

  public companion object {
    public val Current: KProperty1<UserNamespace, Boolean?>
      get() = __Current

    public val Label: KProperty1<UserNamespace, String?>
      get() = __Label

    public val Login: KProperty1<UserNamespace, String?>
      get() = __Login

    public val Namespace: KProperty1<UserNamespace, String?>
      get() = __Namespace

    public val Owner: KProperty1<UserNamespace, Boolean?>
      get() = __Owner
  }
}

public class UserNamespace_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<UserNamespace?>?>,
) : KCollectionPropertyPath<T, UserNamespace?, UserNamespace_<T>>(previous, property) {
  public val current: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Current)

  public val label: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Label)

  public val login: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Login)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val owner: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Owner)

  override fun memberWithAdditionalPath(additionalPath: String): UserNamespace_<T> = UserNamespace_(this, customProperty(this, additionalPath))
}

public class UserNamespace_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, UserNamespace?>?>,
) : KMapPropertyPath<T, K, UserNamespace?, UserNamespace_<T>>(previous, property) {
  public val current: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Current)

  public val label: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Label)

  public val login: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Login)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val owner: KPropertyPath<T, Boolean?>
    get() = KPropertyPath(this, __Owner)

  override fun memberWithAdditionalPath(additionalPath: String): UserNamespace_<T> = UserNamespace_(this, customProperty(this, additionalPath))
}
