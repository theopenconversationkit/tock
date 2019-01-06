package fr.vsct.tock.nlp.front.shared.config

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __IntentId: KProperty1<Classification, Id<IntentDefinition>?>
    get() = Classification::intentId
private val __Entities: KProperty1<Classification, List<ClassifiedEntity>?>
    get() = Classification::entities
class Classification_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Classification?>) :
        KPropertyPath<T, Classification?>(previous,property) {
    val intentId: KPropertyPath<T, Id<IntentDefinition>?>
        get() = KPropertyPath<T, Id<IntentDefinition>?>(this,__IntentId)

    val entities: ClassifiedEntity_Col<T>
        get() = ClassifiedEntity_Col(this,Classification::entities)

    companion object {
        val IntentId: KProperty1<Classification, Id<IntentDefinition>?>
            get() = __IntentId
        val Entities: ClassifiedEntity_Col<Classification>
            get() = ClassifiedEntity_Col<Classification>(null,__Entities)}
}

class Classification_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<Classification>?>) : KCollectionPropertyPath<T, Classification?,
        Classification_<T>>(previous,property) {
    val intentId: KPropertyPath<T, Id<IntentDefinition>?>
        get() = KPropertyPath<T, Id<IntentDefinition>?>(this,__IntentId)

    val entities: ClassifiedEntity_Col<T>
        get() = ClassifiedEntity_Col(this,Classification::entities)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): Classification_<T> =
            Classification_(this, customProperty(this, additionalPath))}

class Classification_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        Classification>?>) : KMapPropertyPath<T, K, Classification?,
        Classification_<T>>(previous,property) {
    val intentId: KPropertyPath<T, Id<IntentDefinition>?>
        get() = KPropertyPath<T, Id<IntentDefinition>?>(this,__IntentId)

    val entities: ClassifiedEntity_Col<T>
        get() = ClassifiedEntity_Col(this,Classification::entities)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): Classification_<T> =
            Classification_(this, customProperty(this, additionalPath))}
