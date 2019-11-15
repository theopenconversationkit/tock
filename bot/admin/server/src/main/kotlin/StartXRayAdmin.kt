package ai.tock.bot.admin

import ai.tock.bot.BotIoc
import ai.tock.bot.admin.test.xray.XrayService
import ai.tock.nlp.front.ioc.FrontIoc
import mu.KotlinLogging
import java.util.Properties

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        val p = Properties()
        p.load(XrayService::class.java.getResourceAsStream("/${args[0]}.properties"))
        logger.info { "set properties: $p" }
        p.forEach { e -> System.setProperty(e.key.toString(), e.value.toString()) }
    }
    FrontIoc.setup(BotIoc.coreModules)
    ai.tock.bot.admin.test.xray.main()
}
