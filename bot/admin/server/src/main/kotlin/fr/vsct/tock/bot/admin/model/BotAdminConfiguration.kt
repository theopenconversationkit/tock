/*
 * Copyright (C) 2017/2019 VSCT
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

package fr.vsct.tock.bot.admin.model

import fr.vsct.tock.bot.admin.kotlin.compiler.client.KotlinCompilerClient
import fr.vsct.tock.bot.admin.test.xray.XrayConfiguration
import fr.vsct.tock.shared.booleanProperty

data class BotAdminConfiguration(
    val botApiSupport:Boolean = booleanProperty("tock_bot_api", false),
    val compilerAvailable:Boolean = !KotlinCompilerClient.compilerDisabled,
    val xrayAvailable:Boolean = XrayConfiguration.isXrayAvailable()
)