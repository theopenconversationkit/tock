package ai.tock.nlp.front.shared.config

import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Name: KProperty1<EntityTypeDefinition, String?>
    get() = EntityTypeDefinition::name
private val __Description: KProperty1<EntityTypeDefinition, String?>
    get() = EntityTypeDefinition::description
private val __SubEntities: KProperty1<EntityTypeDefinition, List<EntityDefinition>?>
    get() = EntityTypeDefinition::subEntities
private val __Dictionary: KProperty1<EntityTypeDefinition, Boolean?>
    get() = EntityTypeDefinition::dictionary
private val __Obfuscated: KProperty1<EntityTypeDefinition, Boolean?>
    get() = EntityTypeDefinition::obfuscated
private val ___id: KProperty1<EntityTypeDefinition, Id<EntityTypeDefinition>?>
    get() = EntityTypeDefinition::_id
class EntityTypeDefinition_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        EntityTypeDefinition?>) : KPropertyPath<T, EntityTypeDefinition?>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val description: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Description)

    val subEntities: EntityDefinition_Col<T>
        get() = EntityDefinition_Col(this,EntityTypeDefinition::subEntities)

    val dictionary: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Dictionary)

    val obfuscated: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Obfuscated)

    val _id: KPropertyPath<T, Id<EntityTypeDefinition>?>
        get() = KPropertyPath(this,___id)

    companion object {
        val Name: KProperty1<EntityTypeDefinition, String?>
            get() = __Name
        val Description: KProperty1<EntityTypeDefinition, String?>
            get() = __Description
        val SubEntities: EntityDefinition_Col<EntityTypeDefinition>
            get() = EntityDefinition_Col(null,__SubEntities)
        val Dictionary: KProperty1<EntityTypeDefinition, Boolean?>
            get() = __Dictionary
        val Obfuscated: KProperty1<EntityTypeDefinition, Boolean?>
            get() = __Obfuscated
        val _id: KProperty1<EntityTypeDefinition, Id<EntityTypeDefinition>?>
            get() = ___id}
}

class EntityTypeDefinition_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<EntityTypeDefinition>?>) : KCollectionPropertyPath<T, EntityTypeDefinition?,
        EntityTypeDefinition_<T>>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val description: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Description)

    val subEntities: EntityDefinition_Col<T>
        get() = EntityDefinition_Col(this,EntityTypeDefinition::subEntities)

    val dictionary: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Dictionary)

    val obfuscated: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Obfuscated)

    val _id: KPropertyPath<T, Id<EntityTypeDefinition>?>
        get() = KPropertyPath(this,___id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): EntityTypeDefinition_<T> =
            EntityTypeDefinition_(this, customProperty(this, additionalPath))}

class EntityTypeDefinition_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        EntityTypeDefinition>?>) : KMapPropertyPath<T, K, EntityTypeDefinition?,
        EntityTypeDefinition_<T>>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val description: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Description)

    val subEntities: EntityDefinition_Col<T>
        get() = EntityDefinition_Col(this,EntityTypeDefinition::subEntities)

    val dictionary: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Dictionary)

    val obfuscated: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Obfuscated)

    val _id: KPropertyPath<T, Id<EntityTypeDefinition>?>
        get() = KPropertyPath(this,___id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): EntityTypeDefinition_<T> =
            EntityTypeDefinition_(this, customProperty(this, additionalPath))}
