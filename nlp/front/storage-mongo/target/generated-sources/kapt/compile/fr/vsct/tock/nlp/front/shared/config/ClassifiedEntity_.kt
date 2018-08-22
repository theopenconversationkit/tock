package fr.vsct.tock.nlp.front.shared.config

import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

class ClassifiedEntity_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, ClassifiedEntity?>) : KPropertyPath<T, ClassifiedEntity?>(previous,property) {
    val type: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ClassifiedEntity::type)

    val role: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ClassifiedEntity::role)

    val start: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,ClassifiedEntity::start)

    val end: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,ClassifiedEntity::end)

    val subEntities: ClassifiedEntity_Col<T>
        get() = ClassifiedEntity_Col(this,ClassifiedEntity::subEntities)
    companion object {
        val Type: KProperty1<ClassifiedEntity, String?>
            get() = ClassifiedEntity::type
        val Role: KProperty1<ClassifiedEntity, String?>
            get() = ClassifiedEntity::role
        val Start: KProperty1<ClassifiedEntity, Int?>
            get() = ClassifiedEntity::start
        val End: KProperty1<ClassifiedEntity, Int?>
            get() = ClassifiedEntity::end
        val SubEntities: ClassifiedEntity_Col<ClassifiedEntity>
            get() = ClassifiedEntity_Col<ClassifiedEntity>(null,ClassifiedEntity::subEntities)}
}

class ClassifiedEntity_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<ClassifiedEntity>?>) : KCollectionPropertyPath<T, ClassifiedEntity?, ClassifiedEntity_<T>>(previous,property) {
    val type: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ClassifiedEntity::type)

    val role: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ClassifiedEntity::role)

    val start: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,ClassifiedEntity::start)

    val end: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,ClassifiedEntity::end)

    val subEntities: ClassifiedEntity_Col<T>
        get() = ClassifiedEntity_Col(this,ClassifiedEntity::subEntities)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ClassifiedEntity_<T> = ClassifiedEntity_(this, customProperty(this, additionalPath))}

class ClassifiedEntity_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, ClassifiedEntity>?>) : KMapPropertyPath<T, K, ClassifiedEntity?, ClassifiedEntity_<T>>(previous,property) {
    val type: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ClassifiedEntity::type)

    val role: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,ClassifiedEntity::role)

    val start: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,ClassifiedEntity::start)

    val end: KPropertyPath<T, Int?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.Int?>(this,ClassifiedEntity::end)

    val subEntities: ClassifiedEntity_Col<T>
        get() = ClassifiedEntity_Col(this,ClassifiedEntity::subEntities)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ClassifiedEntity_<T> = ClassifiedEntity_(this, customProperty(this, additionalPath))}
