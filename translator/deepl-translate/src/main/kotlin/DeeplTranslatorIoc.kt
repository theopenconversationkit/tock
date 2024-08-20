/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

package ai.tock.translator.deepl

import ai.tock.translator.TranslatorEngine
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.provider

/**
 * The default Deepl translator module, for use in a Kodein injector.
 */
val deeplTranslatorModule = configureDeeplTranslatorModule()

fun configureDeeplTranslatorModule(client: DeeplClient = OkHttpDeeplClient()) = Kodein.Module {
    bind<TranslatorEngine>(overrides = true) with provider { DeeplTranslatorEngine(client) }
}
