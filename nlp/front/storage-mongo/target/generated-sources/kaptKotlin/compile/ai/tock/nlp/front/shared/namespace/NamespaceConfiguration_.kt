@file:Suppress("UNCHECKED_CAST", "unused", "RedundantVisibilityModifier")

package ai.tock.nlp.front.shared.namespace

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Iterable
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.`property`.KCollectionPropertyPath
import org.litote.kmongo.`property`.KMapPropertyPath
import org.litote.kmongo.`property`.KMapSimplePropertyPath
import org.litote.kmongo.`property`.KPropertyPath

private val __DefaultSharingConfiguration:
    KProperty1<NamespaceConfiguration, NamespaceSharingConfiguration?>
  get() = NamespaceConfiguration::defaultSharingConfiguration

private val __Label: KProperty1<NamespaceConfiguration, String?>
  get() = NamespaceConfiguration::label

private val __Namespace: KProperty1<NamespaceConfiguration, String?>
  get() = NamespaceConfiguration::namespace

private val __NamespaceImportConfiguration:
    KProperty1<NamespaceConfiguration, Map<String, NamespaceSharingConfiguration>?>
  get() = NamespaceConfiguration::namespaceImportConfiguration

public class NamespaceConfiguration_<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, NamespaceConfiguration?>,
) : KPropertyPath<T, NamespaceConfiguration?>(previous, property) {
  public val defaultSharingConfiguration: KPropertyPath<T, NamespaceSharingConfiguration?>
    get() = KPropertyPath(this, __DefaultSharingConfiguration)

  public val label: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Label)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val namespaceImportConfiguration:
      KMapSimplePropertyPath<T, String?, NamespaceSharingConfiguration?>
    get() = KMapSimplePropertyPath(this, __NamespaceImportConfiguration)

  public companion object {
    public val DefaultSharingConfiguration:
        KProperty1<NamespaceConfiguration, NamespaceSharingConfiguration?>
      get() = __DefaultSharingConfiguration

    public val Label: KProperty1<NamespaceConfiguration, String?>
      get() = __Label

    public val Namespace: KProperty1<NamespaceConfiguration, String?>
      get() = __Namespace

    public val NamespaceImportConfiguration:
        KMapSimplePropertyPath<NamespaceConfiguration, String?, NamespaceSharingConfiguration?>
      get() = KMapSimplePropertyPath(null, __NamespaceImportConfiguration)
  }
}

public class NamespaceConfiguration_Col<T>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Iterable<NamespaceConfiguration?>?>,
) : KCollectionPropertyPath<T, NamespaceConfiguration?, NamespaceConfiguration_<T>>(previous, property) {
  public val defaultSharingConfiguration: KPropertyPath<T, NamespaceSharingConfiguration?>
    get() = KPropertyPath(this, __DefaultSharingConfiguration)

  public val label: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Label)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val namespaceImportConfiguration:
      KMapSimplePropertyPath<T, String?, NamespaceSharingConfiguration?>
    get() = KMapSimplePropertyPath(this, __NamespaceImportConfiguration)

  override fun memberWithAdditionalPath(additionalPath: String): NamespaceConfiguration_<T> = NamespaceConfiguration_(this, customProperty(this, additionalPath))
}

public class NamespaceConfiguration_Map<T, K>(
  previous: KPropertyPath<T, *>?,
  `property`: KProperty1<*, Map<out K, NamespaceConfiguration?>?>,
) : KMapPropertyPath<T, K, NamespaceConfiguration?, NamespaceConfiguration_<T>>(previous, property) {
  public val defaultSharingConfiguration: KPropertyPath<T, NamespaceSharingConfiguration?>
    get() = KPropertyPath(this, __DefaultSharingConfiguration)

  public val label: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Label)

  public val namespace: KPropertyPath<T, String?>
    get() = KPropertyPath(this, __Namespace)

  public val namespaceImportConfiguration:
      KMapSimplePropertyPath<T, String?, NamespaceSharingConfiguration?>
    get() = KMapSimplePropertyPath(this, __NamespaceImportConfiguration)

  override fun memberWithAdditionalPath(additionalPath: String): NamespaceConfiguration_<T> = NamespaceConfiguration_(this, customProperty(this, additionalPath))
}
