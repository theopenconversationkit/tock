package ai.tock.bot.mongo

import java.time.Instant
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KMapSimplePropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __CreationDate: KProperty1<UserTimelineCol.UserStateWrapper, Instant?>
    get() = UserTimelineCol.UserStateWrapper::creationDate
private val __LastUpdateDate: KProperty1<UserTimelineCol.UserStateWrapper, Instant?>
    get() = UserTimelineCol.UserStateWrapper::lastUpdateDate
private val __Flags: KProperty1<UserTimelineCol.UserStateWrapper, Map<String,
        UserTimelineCol.TimeBoxedFlagWrapper>?>
    get() = UserTimelineCol.UserStateWrapper::flags
internal class UserStateWrapper_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        UserTimelineCol.UserStateWrapper?>) : KPropertyPath<T,
        UserTimelineCol.UserStateWrapper?>(previous,property) {
    val creationDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__CreationDate)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUpdateDate)

    val flags: KMapSimplePropertyPath<T, String?, UserTimelineCol.TimeBoxedFlagWrapper?>
        get() = KMapSimplePropertyPath(this,UserTimelineCol.UserStateWrapper::flags)

    companion object {
        val CreationDate: KProperty1<UserTimelineCol.UserStateWrapper, Instant?>
            get() = __CreationDate
        val LastUpdateDate: KProperty1<UserTimelineCol.UserStateWrapper, Instant?>
            get() = __LastUpdateDate
        val Flags: KMapSimplePropertyPath<UserTimelineCol.UserStateWrapper, String?,
                UserTimelineCol.TimeBoxedFlagWrapper?>
            get() = KMapSimplePropertyPath(null, __Flags)}
}

internal class UserStateWrapper_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<UserTimelineCol.UserStateWrapper>?>) : KCollectionPropertyPath<T,
        UserTimelineCol.UserStateWrapper?, UserStateWrapper_<T>>(previous,property) {
    val creationDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__CreationDate)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUpdateDate)

    val flags: KMapSimplePropertyPath<T, String?, UserTimelineCol.TimeBoxedFlagWrapper?>
        get() = KMapSimplePropertyPath(this,UserTimelineCol.UserStateWrapper::flags)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): UserStateWrapper_<T> =
            UserStateWrapper_(this, customProperty(this, additionalPath))}

internal class UserStateWrapper_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, UserTimelineCol.UserStateWrapper>?>) : KMapPropertyPath<T, K,
        UserTimelineCol.UserStateWrapper?, UserStateWrapper_<T>>(previous,property) {
    val creationDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__CreationDate)

    val lastUpdateDate: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__LastUpdateDate)

    val flags: KMapSimplePropertyPath<T, String?, UserTimelineCol.TimeBoxedFlagWrapper?>
        get() = KMapSimplePropertyPath(this,UserTimelineCol.UserStateWrapper::flags)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): UserStateWrapper_<T> =
            UserStateWrapper_(this, customProperty(this, additionalPath))}
