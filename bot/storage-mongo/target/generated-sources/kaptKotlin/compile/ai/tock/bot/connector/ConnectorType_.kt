@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.bot.connector

import ai.tock.translator.UserInterfaceType
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __Id: KProperty1<ConnectorType, String?>
  get() = ConnectorType::id

private val __UserInterfaceType: KProperty1<ConnectorType, UserInterfaceType?>
  get() = ConnectorType::userInterfaceType

public class ConnectorType_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, ConnectorType?>,
) : KPropertyPath<T, ConnectorType?>(previous, property) {
  public val id: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Id)

  public val userInterfaceType: KPropertyPath<T, UserInterfaceType?>
    get() = KPropertyPath(this, __UserInterfaceType)

  public companion object {
    public val Id: KProperty1<ConnectorType, String?>
      get() = __Id

    public val UserInterfaceType: KProperty1<ConnectorType, UserInterfaceType?>
      get() = __UserInterfaceType
  }
}

public class ConnectorType_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<ConnectorType?>?>,
) : KCollectionPropertyPath<T, ConnectorType?, ConnectorType_<T>>(previous, property) {
  public val id: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Id)

  public val userInterfaceType: KPropertyPath<T, UserInterfaceType?>
    get() = KPropertyPath(this, __UserInterfaceType)

  override fun memberWithAdditionalPath(additionalPath: String): ConnectorType_<T> = ConnectorType_(this, customProperty(this, additionalPath))
}

public class ConnectorType_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, ConnectorType?>?>,
) : KMapPropertyPath<T, K, ConnectorType?, ConnectorType_<T>>(previous, property) {
  public val id: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Id)

  public val userInterfaceType: KPropertyPath<T, UserInterfaceType?>
    get() = KPropertyPath(this, __UserInterfaceType)

  override fun memberWithAdditionalPath(additionalPath: String): ConnectorType_<T> = ConnectorType_(this, customProperty(this, additionalPath))
}
