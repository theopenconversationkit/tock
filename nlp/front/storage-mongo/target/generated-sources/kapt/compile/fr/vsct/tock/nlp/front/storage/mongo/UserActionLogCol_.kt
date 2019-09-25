package fr.vsct.tock.nlp.front.storage.mongo

import fr.vsct.tock.nlp.front.shared.config.ApplicationDefinition
import fr.vsct.tock.shared.jackson.AnyValueWrapper
import java.time.Instant
import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Namespace: KProperty1<UserActionLogMongoDAO.UserActionLogCol, String?>
    get() = UserActionLogMongoDAO.UserActionLogCol::namespace
private val __ApplicationId: KProperty1<UserActionLogMongoDAO.UserActionLogCol,
        Id<ApplicationDefinition>?>
    get() = UserActionLogMongoDAO.UserActionLogCol::applicationId
private val __Login: KProperty1<UserActionLogMongoDAO.UserActionLogCol, String?>
    get() = UserActionLogMongoDAO.UserActionLogCol::login
private val __ActionType: KProperty1<UserActionLogMongoDAO.UserActionLogCol, String?>
    get() = UserActionLogMongoDAO.UserActionLogCol::actionType
private val __NewData: KProperty1<UserActionLogMongoDAO.UserActionLogCol, AnyValueWrapper?>
    get() = UserActionLogMongoDAO.UserActionLogCol::newData
private val __Error: KProperty1<UserActionLogMongoDAO.UserActionLogCol, Boolean?>
    get() = UserActionLogMongoDAO.UserActionLogCol::error
private val __Date: KProperty1<UserActionLogMongoDAO.UserActionLogCol, Instant?>
    get() = UserActionLogMongoDAO.UserActionLogCol::date
internal class UserActionLogCol_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        UserActionLogMongoDAO.UserActionLogCol?>) : KPropertyPath<T,
        UserActionLogMongoDAO.UserActionLogCol?>(previous,property) {
    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val login: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Login)

    val actionType: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ActionType)

    val newData: KPropertyPath<T, AnyValueWrapper?>
        get() = KPropertyPath(this,__NewData)

    val error: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Error)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    companion object {
        val Namespace: KProperty1<UserActionLogMongoDAO.UserActionLogCol, String?>
            get() = __Namespace
        val ApplicationId: KProperty1<UserActionLogMongoDAO.UserActionLogCol,
                Id<ApplicationDefinition>?>
            get() = __ApplicationId
        val Login: KProperty1<UserActionLogMongoDAO.UserActionLogCol, String?>
            get() = __Login
        val ActionType: KProperty1<UserActionLogMongoDAO.UserActionLogCol, String?>
            get() = __ActionType
        val NewData: KProperty1<UserActionLogMongoDAO.UserActionLogCol, AnyValueWrapper?>
            get() = __NewData
        val Error: KProperty1<UserActionLogMongoDAO.UserActionLogCol, Boolean?>
            get() = __Error
        val Date: KProperty1<UserActionLogMongoDAO.UserActionLogCol, Instant?>
            get() = __Date}
}

internal class UserActionLogCol_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<UserActionLogMongoDAO.UserActionLogCol>?>) : KCollectionPropertyPath<T,
        UserActionLogMongoDAO.UserActionLogCol?, UserActionLogCol_<T>>(previous,property) {
    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val login: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Login)

    val actionType: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ActionType)

    val newData: KPropertyPath<T, AnyValueWrapper?>
        get() = KPropertyPath(this,__NewData)

    val error: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Error)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): UserActionLogCol_<T> =
            UserActionLogCol_(this, customProperty(this, additionalPath))}

internal class UserActionLogCol_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Map<K, UserActionLogMongoDAO.UserActionLogCol>?>) : KMapPropertyPath<T, K,
        UserActionLogMongoDAO.UserActionLogCol?, UserActionLogCol_<T>>(previous,property) {
    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val applicationId: KPropertyPath<T, Id<ApplicationDefinition>?>
        get() = KPropertyPath(this,__ApplicationId)

    val login: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Login)

    val actionType: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ActionType)

    val newData: KPropertyPath<T, AnyValueWrapper?>
        get() = KPropertyPath(this,__NewData)

    val error: KPropertyPath<T, Boolean?>
        get() = KPropertyPath(this,__Error)

    val date: KPropertyPath<T, Instant?>
        get() = KPropertyPath(this,__Date)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): UserActionLogCol_<T> =
            UserActionLogCol_(this, customProperty(this, additionalPath))}
