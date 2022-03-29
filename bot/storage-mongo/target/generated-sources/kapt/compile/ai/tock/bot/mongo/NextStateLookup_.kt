package ai.tock.bot.mongo

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __NextState: KProperty1<NextStateLookup, DialogFlowStateCol?>
    get() = NextStateLookup::nextState
internal class NextStateLookup_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        NextStateLookup?>) : KPropertyPath<T, NextStateLookup?>(previous,property) {
    val nextState: DialogFlowStateCol_<T>
        get() = DialogFlowStateCol_(this,NextStateLookup::nextState)

    companion object {
        val NextState: DialogFlowStateCol_<NextStateLookup>
            get() = DialogFlowStateCol_(null,__NextState)}
}

internal class NextStateLookup_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<NextStateLookup>?>) : KCollectionPropertyPath<T, NextStateLookup?,
        NextStateLookup_<T>>(previous,property) {
    val nextState: DialogFlowStateCol_<T>
        get() = DialogFlowStateCol_(this,NextStateLookup::nextState)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): NextStateLookup_<T> =
            NextStateLookup_(this, customProperty(this, additionalPath))}

internal class NextStateLookup_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, NextStateLookup>?>) : KMapPropertyPath<T, K, NextStateLookup?,
        NextStateLookup_<T>>(previous,property) {
    val nextState: DialogFlowStateCol_<T>
        get() = DialogFlowStateCol_(this,NextStateLookup::nextState)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): NextStateLookup_<T> =
            NextStateLookup_(this, customProperty(this, additionalPath))}
