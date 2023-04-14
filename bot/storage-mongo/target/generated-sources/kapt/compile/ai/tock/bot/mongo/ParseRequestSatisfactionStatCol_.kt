package ai.tock.bot.mongo

import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Rating: KProperty1<ParseRequestSatisfactionStatCol, Int?>
    get() = ParseRequestSatisfactionStatCol::rating
private val __Count: KProperty1<ParseRequestSatisfactionStatCol, Int?>
    get() = ParseRequestSatisfactionStatCol::count
internal class ParseRequestSatisfactionStatCol_<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, ParseRequestSatisfactionStatCol?>) : KPropertyPath<T,
        ParseRequestSatisfactionStatCol?>(previous,property) {
    val rating: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Rating)

    val count: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Count)

    companion object {
        val Rating: KProperty1<ParseRequestSatisfactionStatCol, Int?>
            get() = __Rating
        val Count: KProperty1<ParseRequestSatisfactionStatCol, Int?>
            get() = __Count}
}

internal class ParseRequestSatisfactionStatCol_Col<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Collection<ParseRequestSatisfactionStatCol>?>) : KCollectionPropertyPath<T,
        ParseRequestSatisfactionStatCol?, ParseRequestSatisfactionStatCol_<T>>(previous,property) {
    val rating: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Rating)

    val count: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Count)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String):
            ParseRequestSatisfactionStatCol_<T> = ParseRequestSatisfactionStatCol_(this,
            customProperty(this, additionalPath))}

internal class ParseRequestSatisfactionStatCol_Map<T, K>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Map<K, ParseRequestSatisfactionStatCol>?>) : KMapPropertyPath<T, K,
        ParseRequestSatisfactionStatCol?, ParseRequestSatisfactionStatCol_<T>>(previous,property) {
    val rating: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Rating)

    val count: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__Count)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String):
            ParseRequestSatisfactionStatCol_<T> = ParseRequestSatisfactionStatCol_(this,
            customProperty(this, additionalPath))}
