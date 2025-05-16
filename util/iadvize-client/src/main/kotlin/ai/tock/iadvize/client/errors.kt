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

package ai.tock.iadvize.client

const val NOT_SUCCESS_MSG = "GraphQL request sent successfully but returns an unsuccessful response with status code"
sealed class IadvizeGraphQLError(override val message: String): Error(message)
class AuthenticationFailedError : Exception("Fail to retrieve a non null access token")
class DataNotFoundError : IadvizeGraphQLError("Data not found")
class NotSuccessResponseError(msg: String?, statusCode: Int) : IadvizeGraphQLError(msg?.let { "code : $statusCode \nerrors:[\n $it \n]"  } ?: "$NOT_SUCCESS_MSG [$statusCode]")
fun graphQlDataNotFoundError(): Nothing = throw DataNotFoundError()
fun graphQlNotSuccessResponseError(message: String?, statusCode: Int): Nothing = throw NotSuccessResponseError(message, statusCode)
fun authenticationFailedError(): Nothing = throw AuthenticationFailedError()
