package fr.vsct.tock.bot.test

import ch.tutteli.atrium.api.cc.en_GB.returnValueOf
import ch.tutteli.atrium.api.cc.en_GB.toBe
import ch.tutteli.atrium.creating.Assert

fun Assert<BotBusMockLog>.toBeSimpleTextMessage(expectedText : String)
        = returnValueOf(BotBusMockLog::text).toBe(expectedText)

