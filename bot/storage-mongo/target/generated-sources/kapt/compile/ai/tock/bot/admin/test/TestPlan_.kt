package ai.tock.bot.admin.test

import ai.tock.bot.admin.bot.BotApplicationConfiguration
import ai.tock.bot.connector.ConnectorType
import ai.tock.bot.connector.ConnectorType_
import ai.tock.bot.engine.message.Message
import java.util.Locale
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

private val __Dialogs: KProperty1<TestPlan, List<TestDialogReport>?>
    get() = TestPlan::dialogs
private val __Name: KProperty1<TestPlan, String?>
    get() = TestPlan::name
private val __ApplicationId: KProperty1<TestPlan, String?>
    get() = TestPlan::applicationId
private val __Namespace: KProperty1<TestPlan, String?>
    get() = TestPlan::namespace
private val __NlpModel: KProperty1<TestPlan, String?>
    get() = TestPlan::nlpModel
private val __BotApplicationConfigurationId: KProperty1<TestPlan, Id<BotApplicationConfiguration>?>
    get() = TestPlan::botApplicationConfigurationId
private val __Locale: KProperty1<TestPlan, Locale?>
    get() = TestPlan::locale
private val __StartAction: KProperty1<TestPlan, Message?>
    get() = TestPlan::startAction
private val __TargetConnectorType: KProperty1<TestPlan, ConnectorType?>
    get() = TestPlan::targetConnectorType
private val ___id: KProperty1<TestPlan, Id<TestPlan>?>
    get() = TestPlan::_id
class TestPlan_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, TestPlan?>) :
        KPropertyPath<T, TestPlan?>(previous,property) {
    val dialogs: KCollectionSimplePropertyPath<T, TestDialogReport?>
        get() = KCollectionSimplePropertyPath(this,TestPlan::dialogs)

    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val applicationId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationId)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val nlpModel: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__NlpModel)

    val botApplicationConfigurationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__BotApplicationConfigurationId)

    val locale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Locale)

    val startAction: KPropertyPath<T, Message?>
        get() = KPropertyPath(this,__StartAction)

    val targetConnectorType: ConnectorType_<T>
        get() = ConnectorType_(this,TestPlan::targetConnectorType)

    val _id: KPropertyPath<T, Id<TestPlan>?>
        get() = KPropertyPath(this,___id)

    companion object {
        val Dialogs: KCollectionSimplePropertyPath<TestPlan, TestDialogReport?>
            get() = KCollectionSimplePropertyPath(null, __Dialogs)
        val Name: KProperty1<TestPlan, String?>
            get() = __Name
        val ApplicationId: KProperty1<TestPlan, String?>
            get() = __ApplicationId
        val Namespace: KProperty1<TestPlan, String?>
            get() = __Namespace
        val NlpModel: KProperty1<TestPlan, String?>
            get() = __NlpModel
        val BotApplicationConfigurationId: KProperty1<TestPlan, Id<BotApplicationConfiguration>?>
            get() = __BotApplicationConfigurationId
        val Locale: KProperty1<TestPlan, Locale?>
            get() = __Locale
        val StartAction: KProperty1<TestPlan, Message?>
            get() = __StartAction
        val TargetConnectorType: ConnectorType_<TestPlan>
            get() = ConnectorType_(null,__TargetConnectorType)
        val _id: KProperty1<TestPlan, Id<TestPlan>?>
            get() = ___id}
}

class TestPlan_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*,
        Collection<TestPlan>?>) : KCollectionPropertyPath<T, TestPlan?,
        TestPlan_<T>>(previous,property) {
    val dialogs: KCollectionSimplePropertyPath<T, TestDialogReport?>
        get() = KCollectionSimplePropertyPath(this,TestPlan::dialogs)

    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val applicationId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationId)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val nlpModel: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__NlpModel)

    val botApplicationConfigurationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__BotApplicationConfigurationId)

    val locale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Locale)

    val startAction: KPropertyPath<T, Message?>
        get() = KPropertyPath(this,__StartAction)

    val targetConnectorType: ConnectorType_<T>
        get() = ConnectorType_(this,TestPlan::targetConnectorType)

    val _id: KPropertyPath<T, Id<TestPlan>?>
        get() = KPropertyPath(this,___id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): TestPlan_<T> = TestPlan_(this,
            customProperty(this, additionalPath))}

class TestPlan_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, TestPlan>?>)
        : KMapPropertyPath<T, K, TestPlan?, TestPlan_<T>>(previous,property) {
    val dialogs: KCollectionSimplePropertyPath<T, TestDialogReport?>
        get() = KCollectionSimplePropertyPath(this,TestPlan::dialogs)

    val name_: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Name)

    val applicationId: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__ApplicationId)

    val namespace: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__Namespace)

    val nlpModel: KPropertyPath<T, String?>
        get() = KPropertyPath(this,__NlpModel)

    val botApplicationConfigurationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = KPropertyPath(this,__BotApplicationConfigurationId)

    val locale: KPropertyPath<T, Locale?>
        get() = KPropertyPath(this,__Locale)

    val startAction: KPropertyPath<T, Message?>
        get() = KPropertyPath(this,__StartAction)

    val targetConnectorType: ConnectorType_<T>
        get() = ConnectorType_(this,TestPlan::targetConnectorType)

    val _id: KPropertyPath<T, Id<TestPlan>?>
        get() = KPropertyPath(this,___id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): TestPlan_<T> = TestPlan_(this,
            customProperty(this, additionalPath))}
