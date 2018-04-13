package fr.vsct.tock.nlp.front.shared.config

import kotlin.Boolean
import kotlin.String
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KPropertyPath

class EntityDefinition_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, EntityDefinition?>) : KPropertyPath<T, EntityDefinition?>(previous,property) {
    val entityTypeName: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityDefinition::entityTypeName)

    val role: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityDefinition::role)

    val atStartOfDay: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityDefinition::atStartOfDay)
    companion object {
        val EntityTypeName: KProperty1<EntityDefinition, String?>
            get() = EntityDefinition::entityTypeName
        val Role: KProperty1<EntityDefinition, String?>
            get() = EntityDefinition::role
        val AtStartOfDay: KProperty1<EntityDefinition, Boolean?>
            get() = EntityDefinition::atStartOfDay}
}

class EntityDefinition_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<EntityDefinition>?>) : KPropertyPath<T, Collection<EntityDefinition>?>(previous,property) {
    val entityTypeName: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityDefinition::entityTypeName)

    val role: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityDefinition::role)

    val atStartOfDay: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityDefinition::atStartOfDay)
}
