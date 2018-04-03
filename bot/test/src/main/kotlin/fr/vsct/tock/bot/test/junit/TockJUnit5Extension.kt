package fr.vsct.tock.bot.test.junit

import fr.vsct.tock.bot.connector.ConnectorType
import fr.vsct.tock.bot.definition.BotDefinition
import fr.vsct.tock.bot.definition.StoryDefinition
import fr.vsct.tock.bot.engine.user.PlayerId
import fr.vsct.tock.bot.test.BotBusMock
import fr.vsct.tock.bot.test.BotBusMockContext
import fr.vsct.tock.bot.test.TestContext
import fr.vsct.tock.bot.test.TestLifecycle
import fr.vsct.tock.bot.test.newBusMockContext
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.Locale

open class TockJUnit5Extension<out T : TestContext>(
    val botDefinition: BotDefinition,
    @Suppress("UNCHECKED_CAST") val lifecycle: TestLifecycle<T> = TestLifecycle(TestContext() as T)
) : BeforeEachCallback, AfterEachCallback {

    /**
     * The [TestContext].
     */
    val testContext: T get() = lifecycle.testContext

    /**
     * Provides a mock initialized with the specified [StoryDefinition] and starts the story.
     */
    fun startNewBusMock(
        story: StoryDefinition = testContext.defaultStoryDefinition(botDefinition),
        connectorType: ConnectorType = testContext.defaultConnectorType(),
        locale: Locale = testContext.defaultLocale(),
        userId: PlayerId = testContext.defaultPlayerId()
    )
            : BotBusMock = newBusMock(story, connectorType, locale, userId).run()

    /**
     * Provides a mock initialized with the specified [StoryDefinition].
     */
    fun newBusMock(
        story: StoryDefinition = testContext.defaultStoryDefinition(botDefinition),
        connectorType: ConnectorType = testContext.defaultConnectorType(),
        locale: Locale = testContext.defaultLocale(),
        userId: PlayerId = testContext.defaultPlayerId()
    )
            : BotBusMock = BotBusMock(newBusMockContext(story, connectorType, locale, userId))

    /**
     * Provides a mock context initialized with the specified [StoryDefinition].
     */
    fun newBusMockContext(
        story: StoryDefinition = testContext.defaultStoryDefinition(botDefinition),
        connectorType: ConnectorType = testContext.defaultConnectorType(),
        locale: Locale = testContext.defaultLocale(),
        userId: PlayerId = testContext.defaultPlayerId()
    ): BotBusMockContext = botDefinition.newBusMockContext(testContext, story, connectorType, locale, userId)
        .apply {
            testContext.botBusMockContext = this
            lifecycle.configureTestIoc()
        }

    /**
     * Provides a mock context initialized with the current [testContext] and runs the bus.
     */
    fun startBusMock(): BotBusMock = busMock().run()

    /**
     * Provides a mock context initialized with the current [testContext].
     */
    fun busMock(): BotBusMock = BotBusMock(testContext.botBusMockContext)


    override fun beforeEach(p0: ExtensionContext) {
        lifecycle.start()
    }

    override fun afterEach(p0: ExtensionContext) {
        lifecycle.end()
    }

}