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
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KMapSimplePropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Name: KProperty1<IntentDefinition, String?>
    get() = IntentDefinition::name
private val __Namespace: KProperty1<IntentDefinition, String?>
    get() = IntentDefinition::namespace
private val __Applications: KProperty1<IntentDefinition, Set<Id<ApplicationDefinition>>?>
    get() = IntentDefinition::applications
private val __Entities: KProperty1<IntentDefinition, Set<EntityDefinition>?>
    get() = IntentDefinition::entities
private val __EntitiesRegexp: KProperty1<IntentDefinition, Map<Locale,
        LinkedHashSet<EntitiesRegexp>>?>
    get() = IntentDefinition::entitiesRegexp
private val __MandatoryStates: KProperty1<IntentDefinition, Set<String>?>
    get() = IntentDefinition::mandatoryStates
private val __SharedIntents: KProperty1<IntentDefinition, Set<Id<IntentDefinition>>?>
    get() = IntentDefinition::sharedIntents
private val __Label: KProperty1<IntentDefinition, String?>
    get() = IntentDefinition::label
private val __Description: KProperty1<IntentDefinition, String?>
    get() = IntentDefinition::description
private val __Category: KProperty1<IntentDefinition, String?>
    get() = IntentDefinition::category
private val ___id: KProperty1<IntentDefinition, Id<IntentDefinition>?>
    get() = IntentDefinition::_id
class IntentDefinition_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        IntentDefinition?>) : KPropertyPath<T, IntentDefinition?>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val applications: KCollectionSimplePropertyPath<T, Id<ApplicationDefinition>?>
        get() = KCollectionSimplePropertyPath(this,IntentDefinition::applications)

    val entities: EntityDefinition_Col<T>
        get() = EntityDefinition_Col(this,IntentDefinition::entities)

    val entitiesRegexp: KMapSimplePropertyPath<T, Locale?, LinkedHashSet<EntitiesRegexp>?>
        get() = KMapSimplePropertyPath(this,IntentDefinition::entitiesRegexp)

    val mandatoryStates: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,IntentDefinition::mandatoryStates)

    val sharedIntents: KCollectionSimplePropertyPath<T, Id<IntentDefinition>?>
        get() = KCollectionSimplePropertyPath(this,IntentDefinition::sharedIntents)

    val label: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Label)

    val description: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Description)

    val category: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Category)

    val _id: KPropertyPath<T, Id<IntentDefinition>?>
        get() = KPropertyPath(this,___id)

    companion object {
        val Name: KProperty1<IntentDefinition, String?>
            get() = __Name
        val Namespace: KProperty1<IntentDefinition, String?>
            get() = __Namespace
        val Applications: KCollectionSimplePropertyPath<IntentDefinition,
                Id<ApplicationDefinition>?>
            get() = KCollectionSimplePropertyPath(null, __Applications)
        val Entities: EntityDefinition_Col<IntentDefinition>
            get() = EntityDefinition_Col(null,__Entities)
        val EntitiesRegexp: KMapSimplePropertyPath<IntentDefinition, Locale?,
                LinkedHashSet<EntitiesRegexp>?>
            get() = KMapSimplePropertyPath(null, __EntitiesRegexp)
        val MandatoryStates: KCollectionSimplePropertyPath<IntentDefinition, String?>
            get() = KCollectionSimplePropertyPath(null, __MandatoryStates)
        val SharedIntents: KCollectionSimplePropertyPath<IntentDefinition, Id<IntentDefinition>?>
            get() = KCollectionSimplePropertyPath(null, __SharedIntents)
        val Label: KProperty1<IntentDefinition, String?>
            get() = __Label
        val Description: KProperty1<IntentDefinition, String?>
            get() = __Description
        val Category: KProperty1<IntentDefinition, String?>
            get() = __Category
        val _id: KProperty1<IntentDefinition, Id<IntentDefinition>?>
            get() = ___id}
}

class IntentDefinition_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<IntentDefinition>?>) : KCollectionPropertyPath<T, IntentDefinition?,
        IntentDefinition_<T>>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val applications: KCollectionSimplePropertyPath<T, Id<ApplicationDefinition>?>
        get() = KCollectionSimplePropertyPath(this,IntentDefinition::applications)

    val entities: EntityDefinition_Col<T>
        get() = EntityDefinition_Col(this,IntentDefinition::entities)

    val entitiesRegexp: KMapSimplePropertyPath<T, Locale?, LinkedHashSet<EntitiesRegexp>?>
        get() = KMapSimplePropertyPath(this,IntentDefinition::entitiesRegexp)

    val mandatoryStates: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,IntentDefinition::mandatoryStates)

    val sharedIntents: KCollectionSimplePropertyPath<T, Id<IntentDefinition>?>
        get() = KCollectionSimplePropertyPath(this,IntentDefinition::sharedIntents)

    val label: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Label)

    val description: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Description)

    val category: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Category)

    val _id: KPropertyPath<T, Id<IntentDefinition>?>
        get() = KPropertyPath(this,___id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): IntentDefinition_<T> =
            IntentDefinition_(this, customProperty(this, additionalPath))}

class IntentDefinition_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        IntentDefinition>?>) : KMapPropertyPath<T, K, IntentDefinition?,
        IntentDefinition_<T>>(previous,property) {
    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val applications: KCollectionSimplePropertyPath<T, Id<ApplicationDefinition>?>
        get() = KCollectionSimplePropertyPath(this,IntentDefinition::applications)

    val entities: EntityDefinition_Col<T>
        get() = EntityDefinition_Col(this,IntentDefinition::entities)

    val entitiesRegexp: KMapSimplePropertyPath<T, Locale?, LinkedHashSet<EntitiesRegexp>?>
        get() = KMapSimplePropertyPath(this,IntentDefinition::entitiesRegexp)

    val mandatoryStates: KCollectionSimplePropertyPath<T, String?>
        get() = KCollectionSimplePropertyPath(this,IntentDefinition::mandatoryStates)

    val sharedIntents: KCollectionSimplePropertyPath<T, Id<IntentDefinition>?>
        get() = KCollectionSimplePropertyPath(this,IntentDefinition::sharedIntents)

    val label: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Label)

    val description: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Description)

    val category: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Category)

    val _id: KPropertyPath<T, Id<IntentDefinition>?>
        get() = KPropertyPath(this,___id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): IntentDefinition_<T> =
            IntentDefinition_(this, customProperty(this, additionalPath))}
