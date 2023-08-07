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

package ai.tock.bot.engine.config.rag


/**
 * Exceptions related to rag handler exception intent handling
 * @param message error message
 */
sealed class RagHandlerException(override val message: String) : RuntimeException(message)
class ServerErrorException(message: String): RagHandlerException("Server Error Exception : $message")
class PromptException(message: String): RagHandlerException("Prompt Exception")
class TimeoutException(message: String): RagHandlerException("Timeout Exception")
class EmptyQueryException(message: String): RagHandlerException("Empty Query Exception")