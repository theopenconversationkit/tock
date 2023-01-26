package ai.tock.nlp.front.shared.namespace

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KMapSimplePropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Namespace: KProperty1<NamespaceConfiguration, String?>
    get() = NamespaceConfiguration::namespace
private val __DefaultSharingConfiguration: KProperty1<NamespaceConfiguration,
        NamespaceSharingConfiguration?>
    get() = NamespaceConfiguration::defaultSharingConfiguration
private val __NamespaceImportConfiguration: KProperty1<NamespaceConfiguration, Map<String,
        NamespaceSharingConfiguration>?>
    get() = NamespaceConfiguration::namespaceImportConfiguration
class NamespaceConfiguration_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        NamespaceConfiguration?>) : KPropertyPath<T, NamespaceConfiguration?>(previous,property) {
    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val defaultSharingConfiguration: KPropertyPath<T, NamespaceSharingConfiguration?>
        get() = KPropertyPath(this,__DefaultSharingConfiguration)

    val namespaceImportConfiguration: KMapSimplePropertyPath<T, String?,
            NamespaceSharingConfiguration?>
        get() = KMapSimplePropertyPath(this,NamespaceConfiguration::namespaceImportConfiguration)

    companion object {
        val Namespace: KProperty1<NamespaceConfiguration, String?>
            get() = __Namespace
        val DefaultSharingConfiguration: KProperty1<NamespaceConfiguration,
                NamespaceSharingConfiguration?>
            get() = __DefaultSharingConfiguration
        val NamespaceImportConfiguration: KMapSimplePropertyPath<NamespaceConfiguration, String?,
                NamespaceSharingConfiguration?>
            get() = KMapSimplePropertyPath(null, __NamespaceImportConfiguration)}
}

class NamespaceConfiguration_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<NamespaceConfiguration>?>) : KCollectionPropertyPath<T, NamespaceConfiguration?,
        NamespaceConfiguration_<T>>(previous,property) {
    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val defaultSharingConfiguration: KPropertyPath<T, NamespaceSharingConfiguration?>
        get() = KPropertyPath(this,__DefaultSharingConfiguration)

    val namespaceImportConfiguration: KMapSimplePropertyPath<T, String?,
            NamespaceSharingConfiguration?>
        get() = KMapSimplePropertyPath(this,NamespaceConfiguration::namespaceImportConfiguration)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): NamespaceConfiguration_<T> =
            NamespaceConfiguration_(this, customProperty(this, additionalPath))}

class NamespaceConfiguration_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, NamespaceConfiguration>?>) : KMapPropertyPath<T, K, NamespaceConfiguration?,
        NamespaceConfiguration_<T>>(previous,property) {
    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val defaultSharingConfiguration: KPropertyPath<T, NamespaceSharingConfiguration?>
        get() = KPropertyPath(this,__DefaultSharingConfiguration)

    val namespaceImportConfiguration: KMapSimplePropertyPath<T, String?,
            NamespaceSharingConfiguration?>
        get() = KMapSimplePropertyPath(this,NamespaceConfiguration::namespaceImportConfiguration)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): NamespaceConfiguration_<T> =
            NamespaceConfiguration_(this, customProperty(this, additionalPath))}
