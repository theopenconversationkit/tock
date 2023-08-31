/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

package ai.tock.shared.exception.rest

import ai.tock.shared.exception.error.ErrorMessage
import ai.tock.shared.exception.error.ErrorMessageWrapper
import io.netty.handler.codec.http.HttpResponseStatus

/**
 * Http 400 exception.
 */
class BadRequestException(httpResponseBody: ErrorMessageWrapper) :
    RestException(httpResponseBody, HttpResponseStatus.BAD_REQUEST) {
    constructor(errorCode: Int, message: String) : this(ErrorMessageWrapper(setOf(ErrorMessage(errorCode.toString(), message))))
    constructor(message: String) : this(ErrorMessageWrapper(message))
    constructor(messages: Set<String>) : this(ErrorMessageWrapper(messages.map { ErrorMessage(message = it) }.toSet()))
}
