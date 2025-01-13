package ai.tock.nlp.front.shared.config

import ai.tock.nlp.core.NlpEngineType
import java.util.Locale
import kotlin.Boolean
import kotlin.Double
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

private val __Name: KProperty1<ApplicationDefinition, String?>
    get() = ApplicationDefinition::name
private val __Label: KProperty1<ApplicationDefinition, String?>
    get() = ApplicationDefinition::label
private val __Namespace: KProperty1<ApplicationDefinition, String?>
    get() = ApplicationDefinition::namespace
private val __Intents: KProperty1<ApplicationDefinition, Set<Id<IntentDefinition>>?>
    get() = ApplicationDefinition::intents
private val __SupportedLocales: KProperty1<ApplicationDefinition, Set<Locale>?>
    get() = ApplicationDefinition::supportedLocales
private val __IntentStatesMap: KProperty1<ApplicationDefinition, Map<Id<IntentDefinition>,
        Set<String>>?>
    get() = ApplicationDefinition::intentStatesMap
private val __NlpEngineType: KProperty1<ApplicationDefinition, NlpEngineType?>
    get() = ApplicationDefinition::nlpEngineType
private val __MergeEngineTypes: KProperty1<ApplicationDefinition, Boolean?>
    get() = ApplicationDefinition::mergeEngineTypes
private val __UseEntityModels: KProperty1<ApplicationDefinition, Boolean?>
    get() = ApplicationDefinition::useEntityModels
private val __SupportSubEntities: KProperty1<ApplicationDefinition, Boolean?>
    get() = ApplicationDefinition::supportSubEntities
private val __UnknownIntentThreshold: KProperty1<ApplicationDefinition, Double?>
    get() = ApplicationDefinition::unknownIntentThreshold
private val __KnownIntentThreshold: KProperty1<ApplicationDefinition, Double?>
    get() = ApplicationDefinition::knownIntentThreshold
private val __NormalizeText: KProperty1<ApplicationDefinition, Boolean?>
    get() = ApplicationDefinition::normalizeText
private val ___id: KProperty1<ApplicationDefinition, Id<ApplicationDefinition>?>
    get() = ApplicationDefinition::_id
class ApplicationDefinition_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        ApplicationDefinition?>) : KPropertyPath<T, ApplicationDefinition?>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val label: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Label)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val intents: KCollectionSimplePropertyPath<T, Id<IntentDefinition>?>
        get() = KCollectionSimplePropertyPath(this,ApplicationDefinition::intents)

    val supportedLocales: KCollectionSimplePropertyPath<T, Locale?>
        get() = KCollectionSimplePropertyPath(this,ApplicationDefinition::supportedLocales)

    val intentStatesMap: KMapSimplePropertyPath<T, Id<IntentDefinition>?, Set<String>?>
        get() = KMapSimplePropertyPath(this,ApplicationDefinition::intentStatesMap)

    val nlpEngineType: KPropertyPath<T, NlpEngineType?>
        get() = KPropertyPath(this,__NlpEngineType)

    val mergeEngineTypes: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__MergeEngineTypes)

    val useEntityModels: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__UseEntityModels)

    val supportSubEntities: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__SupportSubEntities)

    val unknownIntentThreshold: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__UnknownIntentThreshold)

    val knownIntentThreshold: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__KnownIntentThreshold)

    val normalizeText: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__NormalizeText)

    val _id: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,___id)

    companion object {
        val Name: KProperty1<ApplicationDefinition, String?>
            get() = __Name
        val Label: KProperty1<ApplicationDefinition, String?>
            get() = __Label
        val Namespace: KProperty1<ApplicationDefinition, String?>
            get() = __Namespace
        val Intents: KCollectionSimplePropertyPath<ApplicationDefinition, Id<IntentDefinition>?>
            get() = KCollectionSimplePropertyPath(null, __Intents)
        val SupportedLocales: KCollectionSimplePropertyPath<ApplicationDefinition, Locale?>
            get() = KCollectionSimplePropertyPath(null, __SupportedLocales)
        val IntentStatesMap: KMapSimplePropertyPath<ApplicationDefinition, Id<IntentDefinition>?,
                Set<String>?>
            get() = KMapSimplePropertyPath(null, __IntentStatesMap)
        val NlpEngineType: KProperty1<ApplicationDefinition, NlpEngineType?>
            get() = __NlpEngineType
        val MergeEngineTypes: KProperty1<ApplicationDefinition, Boolean?>
            get() = __MergeEngineTypes
        val UseEntityModels: KProperty1<ApplicationDefinition, Boolean?>
            get() = __UseEntityModels
        val SupportSubEntities: KProperty1<ApplicationDefinition, Boolean?>
            get() = __SupportSubEntities
        val UnknownIntentThreshold: KProperty1<ApplicationDefinition, Double?>
            get() = __UnknownIntentThreshold
        val KnownIntentThreshold: KProperty1<ApplicationDefinition, Double?>
            get() = __KnownIntentThreshold
        val NormalizeText: KProperty1<ApplicationDefinition, Boolean?>
            get() = __NormalizeText
        val _id: KProperty1<ApplicationDefinition, Id<ApplicationDefinition>?>
            get() = ___id}
}

class ApplicationDefinition_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ApplicationDefinition>?>) : KCollectionPropertyPath<T, ApplicationDefinition?,
        ApplicationDefinition_<T>>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val label: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Label)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val intents: KCollectionSimplePropertyPath<T, Id<IntentDefinition>?>
        get() = KCollectionSimplePropertyPath(this,ApplicationDefinition::intents)

    val supportedLocales: KCollectionSimplePropertyPath<T, Locale?>
        get() = KCollectionSimplePropertyPath(this,ApplicationDefinition::supportedLocales)

    val intentStatesMap: KMapSimplePropertyPath<T, Id<IntentDefinition>?, Set<String>?>
        get() = KMapSimplePropertyPath(this,ApplicationDefinition::intentStatesMap)

    val nlpEngineType: KPropertyPath<T, NlpEngineType?>
        get() = KPropertyPath(this,__NlpEngineType)

    val mergeEngineTypes: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__MergeEngineTypes)

    val useEntityModels: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__UseEntityModels)

    val supportSubEntities: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__SupportSubEntities)

    val unknownIntentThreshold: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__UnknownIntentThreshold)

    val knownIntentThreshold: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__KnownIntentThreshold)

    val normalizeText: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__NormalizeText)

    val _id: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,___id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ApplicationDefinition_<T> =
            ApplicationDefinition_(this, customProperty(this, additionalPath))}

class ApplicationDefinition_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        ApplicationDefinition>?>) : KMapPropertyPath<T, K, ApplicationDefinition?,
        ApplicationDefinition_<T>>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val label: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Label)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val intents: KCollectionSimplePropertyPath<T, Id<IntentDefinition>?>
        get() = KCollectionSimplePropertyPath(this,ApplicationDefinition::intents)

    val supportedLocales: KCollectionSimplePropertyPath<T, Locale?>
        get() = KCollectionSimplePropertyPath(this,ApplicationDefinition::supportedLocales)

    val intentStatesMap: KMapSimplePropertyPath<T, Id<IntentDefinition>?, Set<String>?>
        get() = KMapSimplePropertyPath(this,ApplicationDefinition::intentStatesMap)

    val nlpEngineType: KPropertyPath<T, NlpEngineType?>
        get() = KPropertyPath(this,__NlpEngineType)

    val mergeEngineTypes: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__MergeEngineTypes)

    val useEntityModels: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__UseEntityModels)

    val supportSubEntities: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__SupportSubEntities)

    val unknownIntentThreshold: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__UnknownIntentThreshold)

    val knownIntentThreshold: KPropertyPath<T, Double?>
        get() = KPropertyPath(this,__KnownIntentThreshold)

    val normalizeText: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__NormalizeText)

    val _id: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,___id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ApplicationDefinition_<T> =
            ApplicationDefinition_(this, customProperty(this, additionalPath))}
