package ai.tock.bot.mongo

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val ___id: KProperty1<GroupByIdContainer, GroupById?>
    get() = GroupByIdContainer::_id
internal class GroupByIdContainer_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        GroupByIdContainer?>) : KPropertyPath<T, GroupByIdContainer?>(previous,property) {
    val _id: GroupById_<T>
        get() = GroupById_(this,GroupByIdContainer::_id)

    companion object {
        val _id: GroupById_<GroupByIdContainer>
            get() = GroupById_(null,___id)}
}

internal class GroupByIdContainer_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<GroupByIdContainer>?>) : KCollectionPropertyPath<T, GroupByIdContainer?,
        GroupByIdContainer_<T>>(previous,property) {
    val _id: GroupById_<T>
        get() = GroupById_(this,GroupByIdContainer::_id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): GroupByIdContainer_<T> =
            GroupByIdContainer_(this, customProperty(this, additionalPath))}

internal class GroupByIdContainer_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, GroupByIdContainer>?>) : KMapPropertyPath<T, K, GroupByIdContainer?,
        GroupByIdContainer_<T>>(previous,property) {
    val _id: GroupById_<T>
        get() = GroupById_(this,GroupByIdContainer::_id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): GroupByIdContainer_<T> =
            GroupByIdContainer_(this, customProperty(this, additionalPath))}
