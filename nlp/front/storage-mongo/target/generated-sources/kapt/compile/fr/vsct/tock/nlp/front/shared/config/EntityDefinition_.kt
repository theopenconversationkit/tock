package fr.vsct.tock.nlp.front.shared.config

import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

class EntityDefinition_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, EntityDefinition?>) : KPropertyPath<T, EntityDefinition?>(previous,property) {
    val entityTypeName: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,EntityDefinition::entityTypeName)

    val role: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,EntityDefinition::role)

    val atStartOfDay: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,EntityDefinition::atStartOfDay)
    companion object {
        val EntityTypeName: KProperty1<EntityDefinition, String?>
            get() = EntityDefinition::entityTypeName
        val Role: KProperty1<EntityDefinition, String?>
            get() = EntityDefinition::role
        val AtStartOfDay: KProperty1<EntityDefinition, Boolean?>
            get() = EntityDefinition::atStartOfDay}
}

class EntityDefinition_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<EntityDefinition>?>) : KCollectionPropertyPath<T, EntityDefinition?, EntityDefinition_<T>>(previous,property) {
    val entityTypeName: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,EntityDefinition::entityTypeName)

    val role: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,EntityDefinition::role)

    val atStartOfDay: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,EntityDefinition::atStartOfDay)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): EntityDefinition_<T> = EntityDefinition_(this, customProperty(this, additionalPath))}

class EntityDefinition_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, EntityDefinition>?>) : KMapPropertyPath<T, K, EntityDefinition?, EntityDefinition_<T>>(previous,property) {
    val entityTypeName: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,EntityDefinition::entityTypeName)

    val role: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,EntityDefinition::role)

    val atStartOfDay: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,EntityDefinition::atStartOfDay)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): EntityDefinition_<T> = EntityDefinition_(this, customProperty(this, additionalPath))}
