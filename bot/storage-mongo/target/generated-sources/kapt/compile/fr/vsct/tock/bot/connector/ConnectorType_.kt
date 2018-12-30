package fr.vsct.tock.bot.connector

import fr.vsct.tock.translator.UserInterfaceType
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

class ConnectorType_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ConnectorType?>) :
        KPropertyPath<T, ConnectorType?>(previous,property) {
    val id: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ConnectorType::id)

    val userInterfaceType: KPropertyPath<T, UserInterfaceType?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                fr.vsct.tock.translator.UserInterfaceType?>(this,ConnectorType::userInterfaceType)

    companion object {
        val Id: KProperty1<ConnectorType, String?>
            get() = ConnectorType::id
        val UserInterfaceType: KProperty1<ConnectorType, UserInterfaceType?>
            get() = ConnectorType::userInterfaceType}
}

class ConnectorType_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ConnectorType>?>) : KCollectionPropertyPath<T, ConnectorType?,
        ConnectorType_<T>>(previous,property) {
    val id: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ConnectorType::id)

    val userInterfaceType: KPropertyPath<T, UserInterfaceType?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                fr.vsct.tock.translator.UserInterfaceType?>(this,ConnectorType::userInterfaceType)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ConnectorType_<T> =
            ConnectorType_(this, customProperty(this, additionalPath))}

class ConnectorType_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        ConnectorType>?>) : KMapPropertyPath<T, K, ConnectorType?,
        ConnectorType_<T>>(previous,property) {
    val id: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ConnectorType::id)

    val userInterfaceType: KPropertyPath<T, UserInterfaceType?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                fr.vsct.tock.translator.UserInterfaceType?>(this,ConnectorType::userInterfaceType)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ConnectorType_<T> =
            ConnectorType_(this, customProperty(this, additionalPath))}
