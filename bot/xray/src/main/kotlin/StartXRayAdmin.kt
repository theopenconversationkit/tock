/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.bot.xray

import ai.tock.bot.BotIoc
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
    BotIoc.setup()
    main()
}
