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

private val ___id: KProperty1<DialogFlowConfiguration, String?>
    get() = DialogFlowConfiguration::_id
private val __CurrentProcessedLevel: KProperty1<DialogFlowConfiguration, Int?>
    get() = DialogFlowConfiguration::currentProcessedLevel
internal class DialogFlowConfiguration_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        DialogFlowConfiguration?>) : KPropertyPath<T, DialogFlowConfiguration?>(previous,property) {
    val _id: KPropertyPath<T, String?>
        get() = KPropertyPath(this,___id)

    val currentProcessedLevel: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__CurrentProcessedLevel)

    companion object {
        val _id: KProperty1<DialogFlowConfiguration, String?>
            get() = ___id
        val CurrentProcessedLevel: KProperty1<DialogFlowConfiguration, Int?>
            get() = __CurrentProcessedLevel}
}

internal class DialogFlowConfiguration_Col<T>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Collection<DialogFlowConfiguration>?>) : KCollectionPropertyPath<T,
        DialogFlowConfiguration?, DialogFlowConfiguration_<T>>(previous,property) {
    val _id: KPropertyPath<T, String?>
        get() = KPropertyPath(this,___id)

    val currentProcessedLevel: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__CurrentProcessedLevel)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DialogFlowConfiguration_<T> =
            DialogFlowConfiguration_(this, customProperty(this, additionalPath))}

internal class DialogFlowConfiguration_Map<T, K>(previous: KPropertyPath<T, *>?, property:
        KProperty1<*, Map<K, DialogFlowConfiguration>?>) : KMapPropertyPath<T, K,
        DialogFlowConfiguration?, DialogFlowConfiguration_<T>>(previous,property) {
    val _id: KPropertyPath<T, String?>
        get() = KPropertyPath(this,___id)

    val currentProcessedLevel: KPropertyPath<T, Int?>
        get() = KPropertyPath(this,__CurrentProcessedLevel)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): DialogFlowConfiguration_<T> =
            DialogFlowConfiguration_(this, customProperty(this, additionalPath))}
