/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.vsct.tock.nlp.model

import fr.vsct.tock.nlp.core.CallContext
import fr.vsct.tock.nlp.core.NlpEngineType
import java.util.Locale

/**
 *
 */
data class TokenizerContext(override val language: Locale,
                            override val engineType: NlpEngineType) : ClassifierContext<TokenizerContext>, ClassifierContextKey {

    constructor(callContext: CallContext) : this(callContext.language, callContext.engineType)

    constructor(intentContext: IntentContext) : this(intentContext.language, intentContext.engineType)

    constructor(entityContext: EntityCallContext) : this(entityContext.language, entityContext.engineType)

    constructor(entityContext: EntityBuildContext) : this(entityContext.language, entityContext.engineType)

    override fun key(): TokenizerContext {
        return this
    }

    override fun name(): String {
        return "$language-$engineType"
    }
}