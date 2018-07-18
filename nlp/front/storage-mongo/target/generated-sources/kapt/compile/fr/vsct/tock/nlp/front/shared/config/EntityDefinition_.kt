package fr.vsct.tock.nlp.front.shared.config

import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
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

class EntityDefinition_Col<T>(
        previous: KPropertyPath<T, *>?,
        property: KProperty1<*, Collection<EntityDefinition>?>,
        additionalPath: String? = null
) : KCollectionPropertyPath<T, EntityDefinition?>(previous,property,additionalPath) {
    override val arrayProjection: EntityDefinition_Col<T>
        @Suppress("UNCHECKED_CAST")
        get() = EntityDefinition_Col(null, this as KProperty1<*, Collection<EntityDefinition>?>, "$")

    val entityTypeName: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityDefinition::entityTypeName)

    val role: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityDefinition::role)

    val atStartOfDay: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,EntityDefinition::atStartOfDay)
}
