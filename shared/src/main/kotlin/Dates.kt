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

package ai.tock.shared

import java.time.ZoneId

/**
 * The internal [defaultZoneId] for Tock - var only for tests.
 * property "tock_default_zone" with default value UTC is used.
 */
var internalDefaultZoneId: ZoneId = ZoneId.of(property("tock_default_zone", "UTC"))

/**
 * The default [ZoneId] used by Tock.
 */
val defaultZoneId: ZoneId get() = internalDefaultZoneId
