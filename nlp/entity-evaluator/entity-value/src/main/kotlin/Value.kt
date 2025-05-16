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

package ai.tock.nlp.entity

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver

/**
 * It is recommended (but not mandatory) that values evaluated by EntityEvaluators
 * implement this interface.
 * Do not forget to call also [ValueResolverRepository.registerType] for each new value type
 * if you don't want to store class name of the value.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonTypeIdResolver(ValueTypeIdResolver::class)
interface Value
