/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.tock.serverless

/**
 * The application of a function runtime that has no main method (AWS Lambda): it injects the modules and
 * installs the bots or the NLP API, without starting an HTTP server.
 *
 * ```
 * class MyBotApplication : ServerlessApplication {
 *     override fun install() {
 *         ServerlessBotLauncher.setup(botDynamoModule)
 *         BotRepository.registerBotProvider(MyBotProvider)
 *         ServerlessBotLauncher.install()
 *     }
 * }
 * ```
 *
 * Declared in `META-INF/services/ai.tock.serverless.ServerlessApplication`, it is loaded once by
 * [ai.tock.serverless.lambda.LambdaHandler], when the function initializes (before the snapshot with SnapStart).
 */
interface ServerlessApplication {
    fun install()
}
