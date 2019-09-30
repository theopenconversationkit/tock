package ai.tock.bot.connector

import ai.tock.translator.UserInterfaceType
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Id: KProperty1<ConnectorType, String?>
    get() = ConnectorType::id
private val __UserInterfaceType: KProperty1<ConnectorType, UserInterfaceType?>
    get() = ConnectorType::userInterfaceType
class ConnectorType_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ConnectorType?>) :
        KPropertyPath<T, ConnectorType?>(previous,property) {
    val id: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Id)

    val userInterfaceType: KPropertyPath<T, UserInterfaceType?>
        get() = KPropertyPath(this,__UserInterfaceType)

    companion object {
        val Id: KProperty1<ConnectorType, String?>
            get() = __Id
        val UserInterfaceType: KProperty1<ConnectorType, UserInterfaceType?>
            get() = __UserInterfaceType}
}

class ConnectorType_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ConnectorType>?>) : KCollectionPropertyPath<T, ConnectorType?,
        ConnectorType_<T>>(previous,property) {
    val id: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Id)

    val userInterfaceType: KPropertyPath<T, UserInterfaceType?>
        get() = KPropertyPath(this,__UserInterfaceType)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ConnectorType_<T> =
            ConnectorType_(this, customProperty(this, additionalPath))}

class ConnectorType_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        ConnectorType>?>) : KMapPropertyPath<T, K, ConnectorType?,
        ConnectorType_<T>>(previous,property) {
    val id: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Id)

    val userInterfaceType: KPropertyPath<T, UserInterfaceType?>
        get() = KPropertyPath(this,__UserInterfaceType)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ConnectorType_<T> =
            ConnectorType_(this, customProperty(this, additionalPath))}
