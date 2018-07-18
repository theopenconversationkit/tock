package fr.vsct.tock.nlp.front.shared.config

import fr.vsct.tock.nlp.core.EntitiesRegexp
import java.util.LinkedHashSet
import java.util.Locale
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

class IntentDefinition_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, IntentDefinition?>) : KPropertyPath<T, IntentDefinition?>(previous,property) {
    val name_: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentDefinition::name)

    val namespace: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentDefinition::namespace)

    val applications: KProperty1<T, Set<Id<ApplicationDefinition>>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentDefinition::applications)

    val entities: EntityDefinition_Col<T>
        get() = EntityDefinition_Col(this,IntentDefinition::entities)

    val entitiesRegexp: KProperty1<T, Map<Locale, LinkedHashSet<EntitiesRegexp>>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentDefinition::entitiesRegexp)

    val mandatoryStates: KProperty1<T, Set<String>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentDefinition::mandatoryStates)

    val sharedIntents: KProperty1<T, Set<Id<IntentDefinition>>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentDefinition::sharedIntents)

    val _id: KProperty1<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentDefinition::_id)
    companion object {
        val Name: KProperty1<IntentDefinition, String?>
            get() = IntentDefinition::name
        val Namespace: KProperty1<IntentDefinition, String?>
            get() = IntentDefinition::namespace
        val Applications: KProperty1<IntentDefinition, Set<Id<ApplicationDefinition>>?>
            get() = IntentDefinition::applications
        val Entities: EntityDefinition_Col<IntentDefinition>
            get() = EntityDefinition_Col<IntentDefinition>(null,IntentDefinition::entities)
        val EntitiesRegexp: KProperty1<IntentDefinition, Map<Locale, LinkedHashSet<EntitiesRegexp>>?>
            get() = IntentDefinition::entitiesRegexp
        val MandatoryStates: KProperty1<IntentDefinition, Set<String>?>
            get() = IntentDefinition::mandatoryStates
        val SharedIntents: KProperty1<IntentDefinition, Set<Id<IntentDefinition>>?>
            get() = IntentDefinition::sharedIntents
        val _id: KProperty1<IntentDefinition, Id<IntentDefinition>?>
            get() = IntentDefinition::_id}
}

class IntentDefinition_Col<T>(
        previous: KPropertyPath<T, *>?,
        property: KProperty1<*, Collection<IntentDefinition>?>,
        additionalPath: String? = null
) : KCollectionPropertyPath<T, IntentDefinition?>(previous,property,additionalPath) {
    override val arrayProjection: IntentDefinition_Col<T>
        @Suppress("UNCHECKED_CAST")
        get() = IntentDefinition_Col(null, this as KProperty1<*, Collection<IntentDefinition>?>, "$")

    val name_: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentDefinition::name)

    val namespace: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentDefinition::namespace)

    val applications: KProperty1<T, Set<Id<ApplicationDefinition>>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentDefinition::applications)

    val entities: EntityDefinition_Col<T>
        get() = EntityDefinition_Col(this,IntentDefinition::entities)

    val entitiesRegexp: KProperty1<T, Map<Locale, LinkedHashSet<EntitiesRegexp>>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentDefinition::entitiesRegexp)

    val mandatoryStates: KProperty1<T, Set<String>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentDefinition::mandatoryStates)

    val sharedIntents: KProperty1<T, Set<Id<IntentDefinition>>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentDefinition::sharedIntents)

    val _id: KProperty1<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,IntentDefinition::_id)
}
