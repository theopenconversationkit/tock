package fr.vsct.tock.bot.admin.test

import fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration
import fr.vsct.tock.bot.connector.ConnectorType_
import fr.vsct.tock.bot.engine.message.Message
import java.util.Locale
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Collection
import kotlin.collections.Map
import kotlin.reflect.KProperty1
import org.litote.kmongo.Id
import org.litote.kmongo.property.KCollectionPropertyPath
import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapPropertyPath
import org.litote.kmongo.property.KPropertyPath

class TestPlan_<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, TestPlan?>) : KPropertyPath<T, TestPlan?>(previous,property) {
    val dialogs: KCollectionSimplePropertyPath<T, TestDialogReport?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, fr.vsct.tock.bot.admin.test.TestDialogReport?>(this,TestPlan::dialogs)

    val name_: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,TestPlan::name)

    val applicationId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,TestPlan::applicationId)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,TestPlan::namespace)

    val nlpModel: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,TestPlan::nlpModel)

    val botApplicationConfigurationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration>?>(this,TestPlan::botApplicationConfigurationId)

    val locale: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,TestPlan::locale)

    val startAction: KPropertyPath<T, Message?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.bot.engine.message.Message?>(this,TestPlan::startAction)

    val targetConnectorType: ConnectorType_<T>
        get() = ConnectorType_(this,TestPlan::targetConnectorType)

    val _id: KPropertyPath<T, Id<TestPlan>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.bot.admin.test.TestPlan>?>(this,TestPlan::_id)
    companion object {
        val Dialogs: KCollectionSimplePropertyPath<TestPlan, TestDialogReport?>
            get() = KCollectionSimplePropertyPath(null, TestPlan::dialogs)
        val Name: KProperty1<TestPlan, String?>
            get() = TestPlan::name
        val ApplicationId: KProperty1<TestPlan, String?>
            get() = TestPlan::applicationId
        val Namespace: KProperty1<TestPlan, String?>
            get() = TestPlan::namespace
        val NlpModel: KProperty1<TestPlan, String?>
            get() = TestPlan::nlpModel
        val BotApplicationConfigurationId: KProperty1<TestPlan, Id<BotApplicationConfiguration>?>
            get() = TestPlan::botApplicationConfigurationId
        val Locale: KProperty1<TestPlan, Locale?>
            get() = TestPlan::locale
        val StartAction: KProperty1<TestPlan, Message?>
            get() = TestPlan::startAction
        val TargetConnectorType: ConnectorType_<TestPlan>
            get() = ConnectorType_<TestPlan>(null,TestPlan::targetConnectorType)
        val _id: KProperty1<TestPlan, Id<TestPlan>?>
            get() = TestPlan::_id}
}

class TestPlan_Col<T>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Collection<TestPlan>?>) : KCollectionPropertyPath<T, TestPlan?, TestPlan_<T>>(previous,property) {
    val dialogs: KCollectionSimplePropertyPath<T, TestDialogReport?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, fr.vsct.tock.bot.admin.test.TestDialogReport?>(this,TestPlan::dialogs)

    val name_: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,TestPlan::name)

    val applicationId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,TestPlan::applicationId)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,TestPlan::namespace)

    val nlpModel: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,TestPlan::nlpModel)

    val botApplicationConfigurationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration>?>(this,TestPlan::botApplicationConfigurationId)

    val locale: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,TestPlan::locale)

    val startAction: KPropertyPath<T, Message?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.bot.engine.message.Message?>(this,TestPlan::startAction)

    val targetConnectorType: ConnectorType_<T>
        get() = ConnectorType_(this,TestPlan::targetConnectorType)

    val _id: KPropertyPath<T, Id<TestPlan>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.bot.admin.test.TestPlan>?>(this,TestPlan::_id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): TestPlan_<T> = TestPlan_(this, customProperty(this, additionalPath))}

class TestPlan_Map<T, K>(previous: KPropertyPath<T, *>?, property: KProperty1<*, Map<K, TestPlan>?>) : KMapPropertyPath<T, K, TestPlan?, TestPlan_<T>>(previous,property) {
    val dialogs: KCollectionSimplePropertyPath<T, TestDialogReport?>
        get() = org.litote.kmongo.property.KCollectionSimplePropertyPath<T, fr.vsct.tock.bot.admin.test.TestDialogReport?>(this,TestPlan::dialogs)

    val name_: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,TestPlan::name)

    val applicationId: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,TestPlan::applicationId)

    val namespace: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,TestPlan::namespace)

    val nlpModel: KPropertyPath<T, String?>
        get() = org.litote.kmongo.property.KPropertyPath<T, kotlin.String?>(this,TestPlan::nlpModel)

    val botApplicationConfigurationId: KPropertyPath<T, Id<BotApplicationConfiguration>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.bot.admin.bot.BotApplicationConfiguration>?>(this,TestPlan::botApplicationConfigurationId)

    val locale: KPropertyPath<T, Locale?>
        get() = org.litote.kmongo.property.KPropertyPath<T, java.util.Locale?>(this,TestPlan::locale)

    val startAction: KPropertyPath<T, Message?>
        get() = org.litote.kmongo.property.KPropertyPath<T, fr.vsct.tock.bot.engine.message.Message?>(this,TestPlan::startAction)

    val targetConnectorType: ConnectorType_<T>
        get() = ConnectorType_(this,TestPlan::targetConnectorType)

    val _id: KPropertyPath<T, Id<TestPlan>?>
        get() = org.litote.kmongo.property.KPropertyPath<T, org.litote.kmongo.Id<fr.vsct.tock.bot.admin.test.TestPlan>?>(this,TestPlan::_id)

    @Suppress("UNCHECKED_CAST")
    override fun memberWithAdditionalPath(additionalPath: String): TestPlan_<T> = TestPlan_(this, customProperty(this, additionalPath))}
