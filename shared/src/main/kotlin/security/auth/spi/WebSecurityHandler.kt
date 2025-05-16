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

package ai.tock.shared.security.auth.spi

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

const val TOCK_USER_ID = "tock_user_id"

/**
 * Handler to manage authentication.
 * All implementations MUST call routingContext.next() in a blocking thread.
 *
 */
interface WebSecurityHandler : Handler<RoutingContext>
