package ai.tock.bot.admin.annotation

import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __State: KProperty1<BotAnnotation, BotAnnotationState?>
    get() = BotAnnotation::state
private val __Reason: KProperty1<BotAnnotation, BotAnnotationReasonType?>
    get() = BotAnnotation::reason
private val __Description: KProperty1<BotAnnotation, String?>
    get() = BotAnnotation::description
private val __GroundTruth: KProperty1<BotAnnotation, String?>
    get() = BotAnnotation::groundTruth
private val __Events: KProperty1<BotAnnotation, List<BotAnnotationEvent>?>
    get() = BotAnnotation::events
private val __CreationDate: KProperty1<BotAnnotation, Instant?>
    get() = BotAnnotation::creationDate
private val __LastUpdateDate: KProperty1<BotAnnotation, Instant?>
    get() = BotAnnotation::lastUpdateDate
class BotAnnotation_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, BotAnnotation?>) :
        KPropertyPath<T, BotAnnotation?>(previous,property) {
    val state: KPropertyPath<T, BotAnnotationState?>
        get() = KPropertyPath(this,__State)

    val reason: KPropertyPath<T, BotAnnotationReasonType?>
        get() = KPropertyPath(this,__Reason)

    val description: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Description)

    val groundTruth: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__GroundTruth)

    val events: KCollectionSimplePropertyPath<T, BotAnnotationEvent?>
        get() = KCollectionSimplePropertyPath(this,BotAnnotation::events)

    val creationDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__CreationDate)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUpdateDate)

    companion object {
        val State: KProperty1<BotAnnotation, BotAnnotationState?>
            get() = __State
        val Reason: KProperty1<BotAnnotation, BotAnnotationReasonType?>
            get() = __Reason
        val Description: KProperty1<BotAnnotation, String?>
            get() = __Description
        val GroundTruth: KProperty1<BotAnnotation, String?>
            get() = __GroundTruth
        val Events: KCollectionSimplePropertyPath<BotAnnotation, BotAnnotationEvent?>
            get() = KCollectionSimplePropertyPath(null, __Events)
        val CreationDate: KProperty1<BotAnnotation, Instant?>
            get() = __CreationDate
        val LastUpdateDate: KProperty1<BotAnnotation, Instant?>
            get() = __LastUpdateDate}
}

class BotAnnotation_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<BotAnnotation>?>) : KCollectionPropertyPath<T, BotAnnotation?,
        BotAnnotation_<T>>(previous,property) {
    val state: KPropertyPath<T, BotAnnotationState?>
        get() = KPropertyPath(this,__State)

    val reason: KPropertyPath<T, BotAnnotationReasonType?>
        get() = KPropertyPath(this,__Reason)

    val description: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Description)

    val groundTruth: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__GroundTruth)

    val events: KCollectionSimplePropertyPath<T, BotAnnotationEvent?>
        get() = KCollectionSimplePropertyPath(this,BotAnnotation::events)

    val creationDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__CreationDate)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUpdateDate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): BotAnnotation_<T> =
            BotAnnotation_(this, customProperty(this, additionalPath))}

class BotAnnotation_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K,
        BotAnnotation>?>) : KMapPropertyPath<T, K, BotAnnotation?,
        BotAnnotation_<T>>(previous,property) {
    val state: KPropertyPath<T, BotAnnotationState?>
        get() = KPropertyPath(this,__State)

    val reason: KPropertyPath<T, BotAnnotationReasonType?>
        get() = KPropertyPath(this,__Reason)

    val description: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Description)

    val groundTruth: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__GroundTruth)

    val events: KCollectionSimplePropertyPath<T, BotAnnotationEvent?>
        get() = KCollectionSimplePropertyPath(this,BotAnnotation::events)

    val creationDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__CreationDate)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUpdateDate)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): BotAnnotation_<T> =
            BotAnnotation_(this, customProperty(this, additionalPath))}
