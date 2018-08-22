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
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KMapSimplePropertyPath
import org.litote.kmongo.property.KPropertyPath

class ApplicationDefinition_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ApplicationDefinition?>) : KPropertyPath<T, ApplicationDefinition?>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ApplicationDefinition::name)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ApplicationDefinition::namespace)

    val intents: KCollectionSimplePropertyPath<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.IntentDefinition>?>(this,ApplicationDefinition::intents)

    val supportedLocales: KCollectionSimplePropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, java.util.Locale?>(this,ApplicationDefinition::supportedLocales)

    val intentStatesMap: KMapSimplePropertyPath<T, Id<IntentDefinition>?, Set<String>?>
        get() = org.litote.kmongo.property.KMapSimplePropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.IntentDefinition>?, kotlin.collections.Set<kotlin.String>?>(this,ApplicationDefinition::intentStatesMap)

    val nlpEngineType: KPropertyPath<T, NlpEngineType?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.nlp.core.NlpEngineType?>(this,ApplicationDefinition::nlpEngineType)

    val mergeEngineTypes: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,ApplicationDefinition::mergeEngineTypes)

    val useEntityModels: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,ApplicationDefinition::useEntityModels)

    val supportSubEntities: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,ApplicationDefinition::supportSubEntities)

    val _id: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,ApplicationDefinition::_id)
    companion object {
        val Name: KProperty1<ApplicationDefinition, String?>
            get() = ApplicationDefinition::name
        val Namespace: KProperty1<ApplicationDefinition, String?>
            get() = ApplicationDefinition::namespace
        val Intents: KCollectionSimplePropertyPath<ApplicationDefinition, Id<IntentDefinition>?>
            get() = KCollectionSimplePropertyPath(null, ApplicationDefinition::intents)
        val SupportedLocales: KCollectionSimplePropertyPath<ApplicationDefinition, Locale?>
            get() = KCollectionSimplePropertyPath(null, ApplicationDefinition::supportedLocales)
        val IntentStatesMap: KMapSimplePropertyPath<ApplicationDefinition, Id<IntentDefinition>?, Set<String>?>
            get() = KMapSimplePropertyPath(null, ApplicationDefinition::intentStatesMap)
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
    val name_: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ApplicationDefinition::name)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ApplicationDefinition::namespace)

    val intents: KCollectionSimplePropertyPath<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.IntentDefinition>?>(this,ApplicationDefinition::intents)

    val supportedLocales: KCollectionSimplePropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, java.util.Locale?>(this,ApplicationDefinition::supportedLocales)

    val intentStatesMap: KMapSimplePropertyPath<T, Id<IntentDefinition>?, Set<String>?>
        get() = org.litote.kmongo.property.KMapSimplePropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.IntentDefinition>?, kotlin.collections.Set<kotlin.String>?>(this,ApplicationDefinition::intentStatesMap)

    val nlpEngineType: KPropertyPath<T, NlpEngineType?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.nlp.core.NlpEngineType?>(this,ApplicationDefinition::nlpEngineType)

    val mergeEngineTypes: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,ApplicationDefinition::mergeEngineTypes)

    val useEntityModels: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,ApplicationDefinition::useEntityModels)

    val supportSubEntities: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,ApplicationDefinition::supportSubEntities)

    val _id: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,ApplicationDefinition::_id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ApplicationDefinition_<T> = ApplicationDefinition_(this, customProperty(this, additionalPath))}

class ApplicationDefinition_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, ApplicationDefinition>?>) : KMapPropertyPath<T, K, ApplicationDefinition?, ApplicationDefinition_<T>>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ApplicationDefinition::name)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ApplicationDefinition::namespace)

    val intents: KCollectionSimplePropertyPath<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.IntentDefinition>?>(this,ApplicationDefinition::intents)

    val supportedLocales: KCollectionSimplePropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, java.util.Locale?>(this,ApplicationDefinition::supportedLocales)

    val intentStatesMap: KMapSimplePropertyPath<T, Id<IntentDefinition>?, Set<String>?>
        get() = org.litote.kmongo.property.KMapSimplePropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.IntentDefinition>?, kotlin.collections.Set<kotlin.String>?>(this,ApplicationDefinition::intentStatesMap)

    val nlpEngineType: KPropertyPath<T, NlpEngineType?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.nlp.core.NlpEngineType?>(this,ApplicationDefinition::nlpEngineType)

    val mergeEngineTypes: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,ApplicationDefinition::mergeEngineTypes)

    val useEntityModels: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,ApplicationDefinition::useEntityModels)

    val supportSubEntities: KPropertyPath<T, Boolean?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Boolean?>(this,ApplicationDefinition::supportSubEntities)

    val _id: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,ApplicationDefinition::_id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ApplicationDefinition_<T> = ApplicationDefinition_(this, customProperty(this, additionalPath))}
