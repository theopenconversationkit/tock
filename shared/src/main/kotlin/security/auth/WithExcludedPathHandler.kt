/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

package ai.tock.shared.security.auth

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

internal class WithExcludedPathHandler(
    val excluded: Set<Regex>,
    val handler: Handler<RoutingContext>
) : Handler<RoutingContext> {

    override fun handle(c: RoutingContext) {
        if (excluded.any { it.matches(c.request().path()) }) {
            c.next()
        } else {
            handler.handle(c)
        }
    }
}
