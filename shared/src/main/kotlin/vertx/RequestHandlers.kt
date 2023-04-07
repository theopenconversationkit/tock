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

package ai.tock.shared.vertx

import ai.tock.shared.exception.ToRestException
import ai.tock.shared.exception.error.ErrorMessageWrapper
import ai.tock.shared.exception.rest.RestException
import io.vertx.ext.web.RoutingContext

// region request handlers
typealias RequestHandler<T,E> = (RoutingContext) -> RequestHandlerResult<T,E>

typealias BiRequestHandler<R,T,E> = (RoutingContext, R) -> RequestHandlerResult<T,E>

typealias TriRequestHandler<R,S,T,E> = (RoutingContext, R, S) -> RequestHandlerResult<T,E>

// endregion

// region RequestHandlerResult
sealed class RequestHandlerResult<T, E:Throwable> {
    fun <R> map(handleFailure: (E) -> Unit = {}, successMapper: (T) -> R): RequestHandlerResult<R, E> {
        return when(this) {
            is RequestSucceeded -> RequestSucceeded(successMapper(value))
            is RequestFailed -> {
                handleFailure(error)
                RequestFailed(error)
            }
        }
    }

    fun mapToSuccessUnit(handleFailure: (E) -> Unit = {}, handleSuccess: (T) -> Unit = {}): RequestHandlerResult<Unit, E> {
        return when(this) {
            is RequestSucceeded -> {
                handleSuccess(value)
                RequestSucceeded(Unit)
            }

            is RequestFailed -> {
                handleFailure(error)
                RequestSucceeded(Unit)
            }
        }
    }

}

data class RequestSucceeded<T, E:Throwable>(val value: T): RequestHandlerResult<T, E>()

data class RequestFailed<T, E:Throwable>(val error: E): RequestHandlerResult<T, E>()

// endregion

// region toRequestHandler functions
inline fun <T, reified E: ToRestException> toRequestHandler(crossinline requestHandler: (RoutingContext) -> T) : RequestHandler<T, E>{
    return { context ->
        try {
            RequestSucceeded(requestHandler(context))
        } catch (e: Throwable) {
            handleThrowable(e)
        }
    }
}

inline fun <T, R, reified E: ToRestException> toRequestHandler(crossinline requestHandler: (RoutingContext, R) -> T) : BiRequestHandler<R,T, E>{
    return { context, r ->
        try {
            RequestSucceeded(requestHandler(context, r))
        } catch (e: Throwable) {
            handleThrowable(e)
        }
    }
}

inline fun <T, R, S, reified E: ToRestException> toRequestHandler(crossinline requestHandler: (RoutingContext, R, S) -> T) : TriRequestHandler<R, S,T, E>{
    return { context, r, s ->
        try {
            RequestSucceeded(requestHandler(context, r, s))
        } catch (e: Throwable) {
           handleThrowable(e)
        }
    }
}

inline fun <T, reified E : ToRestException> handleThrowable(e: Throwable) : RequestHandlerResult<T, E> = if (e is E) {
    RequestFailed(e)
} else {
    throw RestException(ErrorMessageWrapper(e.message ?: ""))
}
// endregion