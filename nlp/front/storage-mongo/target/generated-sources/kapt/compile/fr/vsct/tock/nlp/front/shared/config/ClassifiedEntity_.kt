package fr.vsct.tock.nlp.front.shared.config

import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Type: KProperty1<ClassifiedEntity, String?>
    get() = ClassifiedEntity::type
private val __Role: KProperty1<ClassifiedEntity, String?>
    get() = ClassifiedEntity::role
private val __Start: KProperty1<ClassifiedEntity, Int?>
    get() = ClassifiedEntity::start
private val __End: KProperty1<ClassifiedEntity, Int?>
    get() = ClassifiedEntity::end
private val __SubEntities: KProperty1<ClassifiedEntity, List<ClassifiedEntity>?>
    get() = ClassifiedEntity::subEntities
class ClassifiedEntity_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        ClassifiedEntity?>) : KPropertyPath<T, ClassifiedEntity?>(previous,property) {
    val type: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Type)

    val role: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Role)

    val start: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Start)

    val end: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__End)

    val subEntities: ClassifiedEntity_Col<T>
        get() = ClassifiedEntity_Col(this,ClassifiedEntity::subEntities)

    companion object {
        val Type: KProperty1<ClassifiedEntity, String?>
            get() = __Type
        val Role: KProperty1<ClassifiedEntity, String?>
            get() = __Role
        val Start: KProperty1<ClassifiedEntity, Int?>
            get() = __Start
        val End: KProperty1<ClassifiedEntity, Int?>
            get() = __End
        val SubEntities: ClassifiedEntity_Col<ClassifiedEntity>
            get() = ClassifiedEntity_Col(null,__SubEntities)}
}

class ClassifiedEntity_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<ClassifiedEntity>?>) : KCollectionPropertyPath<T, ClassifiedEntity?,
        ClassifiedEntity_<T>>(previous,property) {
    val type: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Type)

    val role: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Role)

    val start: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Start)

    val end: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__End)

    val subEntities: ClassifiedEntity_Col<T>
        get() = ClassifiedEntity_Col(this,ClassifiedEntity::subEntities)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ClassifiedEntity_<T> =
            ClassifiedEntity_(this, customProperty(this, additionalPath))}

class ClassifiedEntity_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        ClassifiedEntity>?>) : KMapPropertyPath<T, K, ClassifiedEntity?,
        ClassifiedEntity_<T>>(previous,property) {
    val type: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Type)

    val role: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Role)

    val start: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Start)

    val end: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__End)

    val subEntities: ClassifiedEntity_Col<T>
        get() = ClassifiedEntity_Col(this,ClassifiedEntity::subEntities)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): ClassifiedEntity_<T> =
            ClassifiedEntity_(this, customProperty(this, additionalPath))}
