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

package ai.tock.shared.exception.rest

import ai.tock.shared.exception.error.ErrorMessageWrapper
import io.netty.handler.codec.http.HttpResponseStatus

/**
 * Base class for rest exceptions.
 */
open class RestException(
    val httpResponseBody: ErrorMessageWrapper = ErrorMessageWrapper(),
    val httpResponseStatus: HttpResponseStatus = HttpResponseStatus.INTERNAL_SERVER_ERROR)
    : Exception(httpResponseStatus.reasonPhrase())
