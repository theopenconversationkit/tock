package fr.vsct.tock.nlp.front.shared.config

import fr.vsct.tock.nlp.core.PredefinedValue
import kotlin.String
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KPropertyPath

class EntityTypeDefinition_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, EntityTypeDefinition?>) : KPropertyPath<T, EntityTypeDefinition?>(previous,property) {
    val name_: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTypeDefinition::name)

    val description: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTypeDefinition::description)

    val subEntities: EntityDefinition_Col<T>
        get() = EntityDefinition_Col(this,EntityTypeDefinition::subEntities)

    val predefinedValues: KProperty1<T, List<PredefinedValue>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTypeDefinition::predefinedValues)

    val _id: KProperty1<T, Id<EntityTypeDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTypeDefinition::_id)
    companion object {
        val Name: KProperty1<EntityTypeDefinition, String?>
            get() = EntityTypeDefinition::name
        val Description: KProperty1<EntityTypeDefinition, String?>
            get() = EntityTypeDefinition::description
        val SubEntities: EntityDefinition_Col<EntityTypeDefinition>
            get() = EntityDefinition_Col<EntityTypeDefinition>(null,EntityTypeDefinition::subEntities)
        val PredefinedValues: KProperty1<EntityTypeDefinition, List<PredefinedValue>?>
            get() = EntityTypeDefinition::predefinedValues
        val _id: KProperty1<EntityTypeDefinition, Id<EntityTypeDefinition>?>
            get() = EntityTypeDefinition::_id}
}

class EntityTypeDefinition_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<EntityTypeDefinition>?>) : KPropertyPath<T, Collection<EntityTypeDefinition>?>(previous,property) {
    val name_: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTypeDefinition::name)

    val description: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTypeDefinition::description)

    val subEntities: EntityDefinition_Col<T>
        get() = EntityDefinition_Col(this,EntityTypeDefinition::subEntities)

    val predefinedValues: KProperty1<T, List<PredefinedValue>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTypeDefinition::predefinedValues)

    val _id: KProperty1<T, Id<EntityTypeDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityTypeDefinition::_id)
}
