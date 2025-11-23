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

package ai.tock.nlp.model

import ai.tock.nlp.core.CallContext
import ai.tock.nlp.core.NlpEngineType
import java.util.Locale

/**
 *
 */
data class TokenizerContext(
    override val language: Locale,
    override val engineType: NlpEngineType,
    override val applicationName: String,
) : ClassifierContext<TokenizerContext>, ClassifierContextKey {
    constructor(callContext: CallContext) : this(
        callContext.language,
        callContext.engineType,
        callContext.application.name,
    )

    constructor(intentContext: IntentContext) : this(
        intentContext.language,
        intentContext.engineType,
        intentContext.application.name,
    )

    constructor(entityContext: EntityContext) : this(
        entityContext.language,
        entityContext.engineType,
        entityContext.applicationName,
    )

    override fun key(): TokenizerContext {
        return this
    }

    override fun id(): String {
        return "$language-${engineType.name}"
    }
}
