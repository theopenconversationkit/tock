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

package fr.vsct.tock.nlp.model.service.engine

import fr.vsct.tock.nlp.model.service.storage.NlpModelStream
import java.io.InputStream
import java.io.OutputStream

/**
 *
 */
interface NlpEngineModelIo {

    fun loadTokenizerModel(input : NlpModelStream) : Any

    fun loadIntentModel(input : NlpModelStream) : Any

    fun loadEntityModel(input : NlpModelStream) : Any


    fun copyTokenizerModel(model : Any, output : OutputStream)

    fun copyIntentModel(model : Any, output : OutputStream)

    fun copyEntityModel(model : Any, output : OutputStream)

}