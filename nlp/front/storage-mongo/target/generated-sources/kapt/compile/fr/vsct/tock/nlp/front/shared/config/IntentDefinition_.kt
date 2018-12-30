package fr.vsct.tock.nlp.front.shared.config

import fr.vsct.tock.nlp.core.EntitiesRegexp
import java.util.LinkedHashSet
import java.util.Locale
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KMapSimplePropertyPath
import org.litote.kmongo.property.KPropertyPath

class IntentDefinition_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        IntentDefinition?>) : KPropertyPath<T, IntentDefinition?>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,IntentDefinition::name)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,IntentDefinition::namespace)

    val applications: KCollectionSimplePropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,IntentDefinition::applications)

    val entities: EntityDefinition_Col<T>
        get() = EntityDefinition_Col(this,IntentDefinition::entities)

    val entitiesRegexp: KMapSimplePropertyPath<T, Locale?, LinkedHashSet<EntitiesRegexp>?>
        get() = org.litote.kmongo.property.KMapSimplePropertyPath<T, java.util.Locale?,
                java.util.LinkedHashSet<fr.vsct.tock.nlp.core.EntitiesRegexp>?>(this,IntentDefinition::entitiesRegexp)

    val mandatoryStates: KCollectionSimplePropertyPath<T, String?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T,
                kotlin.String?>(this,IntentDefinition::mandatoryStates)

    val sharedIntents: KCollectionSimplePropertyPath<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.IntentDefinition>?>(this,IntentDefinition::sharedIntents)

    val label: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,IntentDefinition::label)

    val description: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,IntentDefinition::description)

    val category: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,IntentDefinition::category)

    val _id: KPropertyPath<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.IntentDefinition>?>(this,IntentDefinition::_id)

    companion object {
        val Name: KProperty1<IntentDefinition, String?>
            get() = IntentDefinition::name
        val Namespace: KProperty1<IntentDefinition, String?>
            get() = IntentDefinition::namespace
        val Applications: KCollectionSimplePropertyPath<IntentDefinition,
                Id<ApplicationDefinition>?>
            get() = KCollectionSimplePropertyPath(null, IntentDefinition::applications)
        val Entities: EntityDefinition_Col<IntentDefinition>
            get() = EntityDefinition_Col<IntentDefinition>(null,IntentDefinition::entities)
        val EntitiesRegexp: KMapSimplePropertyPath<IntentDefinition, Locale?,
                LinkedHashSet<EntitiesRegexp>?>
            get() = KMapSimplePropertyPath(null, IntentDefinition::entitiesRegexp)
        val MandatoryStates: KCollectionSimplePropertyPath<IntentDefinition, String?>
            get() = KCollectionSimplePropertyPath(null, IntentDefinition::mandatoryStates)
        val SharedIntents: KCollectionSimplePropertyPath<IntentDefinition, Id<IntentDefinition>?>
            get() = KCollectionSimplePropertyPath(null, IntentDefinition::sharedIntents)
        val Label: KProperty1<IntentDefinition, String?>
            get() = IntentDefinition::label
        val Description: KProperty1<IntentDefinition, String?>
            get() = IntentDefinition::description
        val Category: KProperty1<IntentDefinition, String?>
            get() = IntentDefinition::category
        val _id: KProperty1<IntentDefinition, Id<IntentDefinition>?>
            get() = IntentDefinition::_id}
}

class IntentDefinition_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<IntentDefinition>?>) : KCollectionPropertyPath<T, IntentDefinition?,
        IntentDefinition_<T>>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,IntentDefinition::name)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,IntentDefinition::namespace)

    val applications: KCollectionSimplePropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,IntentDefinition::applications)

    val entities: EntityDefinition_Col<T>
        get() = EntityDefinition_Col(this,IntentDefinition::entities)

    val entitiesRegexp: KMapSimplePropertyPath<T, Locale?, LinkedHashSet<EntitiesRegexp>?>
        get() = org.litote.kmongo.property.KMapSimplePropertyPath<T, java.util.Locale?,
                java.util.LinkedHashSet<fr.vsct.tock.nlp.core.EntitiesRegexp>?>(this,IntentDefinition::entitiesRegexp)

    val mandatoryStates: KCollectionSimplePropertyPath<T, String?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T,
                kotlin.String?>(this,IntentDefinition::mandatoryStates)

    val sharedIntents: KCollectionSimplePropertyPath<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.IntentDefinition>?>(this,IntentDefinition::sharedIntents)

    val label: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,IntentDefinition::label)

    val description: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,IntentDefinition::description)

    val category: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,IntentDefinition::category)

    val _id: KPropertyPath<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.IntentDefinition>?>(this,IntentDefinition::_id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): IntentDefinition_<T> =
            IntentDefinition_(this, customProperty(this, additionalPath))}

class IntentDefinition_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        IntentDefinition>?>) : KMapPropertyPath<T, K, IntentDefinition?,
        IntentDefinition_<T>>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,IntentDefinition::name)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,IntentDefinition::namespace)

    val applications: KCollectionSimplePropertyPath<T, Id<ApplicationDefinition>?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition>?>(this,IntentDefinition::applications)

    val entities: EntityDefinition_Col<T>
        get() = EntityDefinition_Col(this,IntentDefinition::entities)

    val entitiesRegexp: KMapSimplePropertyPath<T, Locale?, LinkedHashSet<EntitiesRegexp>?>
        get() = org.litote.kmongo.property.KMapSimplePropertyPath<T, java.util.Locale?,
                java.util.LinkedHashSet<fr.vsct.tock.nlp.core.EntitiesRegexp>?>(this,IntentDefinition::entitiesRegexp)

    val mandatoryStates: KCollectionSimplePropertyPath<T, String?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T,
                kotlin.String?>(this,IntentDefinition::mandatoryStates)

    val sharedIntents: KCollectionSimplePropertyPath<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.IntentDefinition>?>(this,IntentDefinition::sharedIntents)

    val label: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,IntentDefinition::label)

    val description: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,IntentDefinition::description)

    val category: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                kotlin.String?>(this,IntentDefinition::category)

    val _id: KPropertyPath<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath<T,
                org.litote.kmongo.Id<fr.vsct.tock.nlp.front.shared.config.IntentDefinition>?>(this,IntentDefinition::_id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): IntentDefinition_<T> =
            IntentDefinition_(this, customProperty(this, additionalPath))}
