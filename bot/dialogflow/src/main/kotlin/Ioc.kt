/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.nlp.dialogflow

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import ai.tock.bot.engine.nlp.NlpController
import ai.tock.nlp.api.client.NlpClient

/**
 * The DialogFlow Nlp client module.
 */
val dialogFlowModule = Kodein.Module {
    bind<NlpClient>(overrides = true) with singleton { TockDialogflowNlpClient() }
    bind<NlpController>(overrides = true) with singleton { DialogflowNlp() }
}