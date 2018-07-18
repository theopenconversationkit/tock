package fr.vsct.tock.nlp.front.shared.config

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KPropertyPath

class Classification_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Classification?>) : KPropertyPath<T, Classification?>(previous,property) {
    val intentId: KProperty1<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,Classification::intentId)

    val entities: ClassifiedEntity_Col<T>
        get() = ClassifiedEntity_Col(this,Classification::entities)
    companion object {
        val IntentId: KProperty1<Classification, Id<IntentDefinition>?>
            get() = Classification::intentId
        val Entities: ClassifiedEntity_Col<Classification>
            get() = ClassifiedEntity_Col<Classification>(null,Classification::entities)}
}

class Classification_Col<T>(
        previous: KPropertyPath<T, *>?,
        property: KProperty1<*, Collection<Classification>?>,
        additionalPath: String? = null
) : KCollectionPropertyPath<T, Classification?>(previous,property,additionalPath) {
    override val arrayProjection: Classification_Col<T>
        @Suppress("UNCHECKED_CAST")
        get() = Classification_Col(null, this as KProperty1<*, Collection<Classification>?>, "$")

    val intentId: KProperty1<T, Id<IntentDefinition>?>
        get() = org.litote.kmongo.property.KPropertyPath(this,Classification::intentId)

    val entities: ClassifiedEntity_Col<T>
        get() = ClassifiedEntity_Col(this,Classification::entities)
}
