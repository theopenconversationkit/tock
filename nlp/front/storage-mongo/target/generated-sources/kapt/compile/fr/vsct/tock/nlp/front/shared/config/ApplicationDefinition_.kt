package fr.vsct.tock.nlp.front.shared.config

import fr.vsct.tock.nlp.core.NlpEngineType
import java.util.Locale
import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

class ApplicationDefinition_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ApplicationDefinition?>) : KPropertyPath<T, ApplicationDefinition?>(previous,property) {
    val name_: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ApplicationDefinition::name)

    val namespace: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ApplicationDefinition::namespace)

    val intents: KProperty1<T, Set<Id<IntentDefinition>>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ApplicationDefinition::intents)

    val supportedLocales: KProperty1<T, Set<Locale>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ApplicationDefinition::supportedLocales)

    val intentStatesMap: KProperty1<T, Map<Id<IntentDefinition>, Set<String>>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ApplicationDefinition::intentStatesMap)

    val nlpEngineType: KProperty1<T, NlpEngineType?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ApplicationDefinition::nlpEngineType)

    val mergeEngineTypes: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ApplicationDefinition::mergeEngineTypes)

    val useEntityModels: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ApplicationDefinition::useEntityModels)

    val supportSubEntities: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ApplicationDefinition::supportSubEntities)

    val _id: KProperty1<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ApplicationDefinition::_id)
    companion object {
        val Name: KProperty1<ApplicationDefinition, String?>
            get() = ApplicationDefinition::name
        val Namespace: KProperty1<ApplicationDefinition, String?>
            get() = ApplicationDefinition::namespace
        val Intents: KProperty1<ApplicationDefinition, Set<Id<IntentDefinition>>?>
            get() = ApplicationDefinition::intents
        val SupportedLocales: KProperty1<ApplicationDefinition, Set<Locale>?>
            get() = ApplicationDefinition::supportedLocales
        val IntentStatesMap: KProperty1<ApplicationDefinition, Map<Id<IntentDefinition>, Set<String>>?>
            get() = ApplicationDefinition::intentStatesMap
        val NlpEngineType: KProperty1<ApplicationDefinition, NlpEngineType?>
            get() = ApplicationDefinition::nlpEngineType
        val MergeEngineTypes: KProperty1<ApplicationDefinition, Boolean?>
            get() = ApplicationDefinition::mergeEngineTypes
        val UseEntityModels: KProperty1<ApplicationDefinition, Boolean?>
            get() = ApplicationDefinition::useEntityModels
        val SupportSubEntities: KProperty1<ApplicationDefinition, Boolean?>
            get() = ApplicationDefinition::supportSubEntities
        val _id: KProperty1<ApplicationDefinition, Id<ApplicationDefinition>?>
            get() = ApplicationDefinition::_id}
}

class ApplicationDefinition_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<ApplicationDefinition>?>) : KCollectionPropertyPath<T, ApplicationDefinition?, ApplicationDefinition_<T>>(previous,property) {
    val name_: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ApplicationDefinition::name)

    val namespace: KProperty1<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ApplicationDefinition::namespace)

    val intents: KProperty1<T, Set<Id<IntentDefinition>>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ApplicationDefinition::intents)

    val supportedLocales: KProperty1<T, Set<Locale>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ApplicationDefinition::supportedLocales)

    val intentStatesMap: KProperty1<T, Map<Id<IntentDefinition>, Set<String>>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ApplicationDefinition::intentStatesMap)

    val nlpEngineType: KProperty1<T, NlpEngineType?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ApplicationDefinition::nlpEngineType)

    val mergeEngineTypes: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ApplicationDefinition::mergeEngineTypes)

    val useEntityModels: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ApplicationDefinition::useEntityModels)

    val supportSubEntities: KProperty1<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ApplicationDefinition::supportSubEntities)

    val _id: KProperty1<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,ApplicationDefinition::_id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ApplicationDefinition_<T> = ApplicationDefinition_(this, customProperty(this, additionalPath))}
