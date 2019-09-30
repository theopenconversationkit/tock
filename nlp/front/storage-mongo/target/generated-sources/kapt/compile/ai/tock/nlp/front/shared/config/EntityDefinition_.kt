package ai.tock.nlp.front.shared.config

import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __EntityTypeName: KProperty1<EntityDefinition, String?>
    get() = EntityDefinition::entityTypeName
private val __Role: KProperty1<EntityDefinition, String?>
    get() = EntityDefinition::role
private val __AtStartOfDay: KProperty1<EntityDefinition, Boolean?>
    get() = EntityDefinition::atStartOfDay
class EntityDefinition_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        EntityDefinition?>) : KPropertyPath<T, EntityDefinition?>(previous,property) {
    val entityTypeName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__EntityTypeName)

    val role: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Role)

    val atStartOfDay: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__AtStartOfDay)

    companion object {
        val EntityTypeName: KProperty1<EntityDefinition, String?>
            get() = __EntityTypeName
        val Role: KProperty1<EntityDefinition, String?>
            get() = __Role
        val AtStartOfDay: KProperty1<EntityDefinition, Boolean?>
            get() = __AtStartOfDay}
}

class EntityDefinition_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<EntityDefinition>?>) : KCollectionPropertyPath<T, EntityDefinition?,
        EntityDefinition_<T>>(previous,property) {
    val entityTypeName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__EntityTypeName)

    val role: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Role)

    val atStartOfDay: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__AtStartOfDay)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): EntityDefinition_<T> =
            EntityDefinition_(this, customProperty(this, additionalPath))}

class EntityDefinition_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        EntityDefinition>?>) : KMapPropertyPath<T, K, EntityDefinition?,
        EntityDefinition_<T>>(previous,property) {
    val entityTypeName: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__EntityTypeName)

    val role: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Role)

    val atStartOfDay: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__AtStartOfDay)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): EntityDefinition_<T> =
            EntityDefinition_(this, customProperty(this, additionalPath))}
