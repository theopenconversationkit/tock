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

package ai.tock.nlp.front.service

import ai.tock.nlp.front.service.alexa.AlexaCodecService
import ai.tock.nlp.front.shared.ApplicationCodec
import ai.tock.nlp.front.shared.ApplicationConfiguration
import ai.tock.nlp.front.shared.ApplicationMonitor
import ai.tock.nlp.front.shared.ModelTester
import ai.tock.nlp.front.shared.ModelUpdater
import ai.tock.nlp.front.shared.Parser
import ai.tock.nlp.front.shared.codec.alexa.AlexaCodec
import ai.tock.shared.security.TockUserListener
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider

/**
 *
 */
val frontModule = Kodein.Module {
    bind<ApplicationConfiguration>() with provider { ApplicationConfigurationService }
    bind<Parser>() with provider { ParserService }
    bind<ModelUpdater>() with provider { ModelUpdaterService }
    bind<ApplicationCodec>() with provider { ApplicationCodecService }
    bind<ApplicationMonitor>() with provider { ApplicationMonitorService }
    bind<ModelTester>() with provider { ModelTesterService }
    bind<AlexaCodec>() with provider { AlexaCodecService }
    bind<TockUserListener>(overrides = true) with provider { AdminTockUserListener }
}
