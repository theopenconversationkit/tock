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

package ai.tock.nlp.api

import ai.tock.nlp.front.ioc.FrontIoc
import ai.tock.shared.vertx.vertx
import com.github.salomonbrys.kodein.Kodein.Module

fun main(args: Array<String>) {
    startNlpService()
}

fun startNlpService(vararg modules: Module) {
    FrontIoc.setup(*modules)
    vertx.deployVerticle(NlpVerticle())
}
