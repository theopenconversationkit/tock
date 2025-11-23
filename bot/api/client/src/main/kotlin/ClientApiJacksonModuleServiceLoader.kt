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

package ai.tock.bot.api.client

import ai.tock.bot.api.model.message.bot.I18nText
import ai.tock.shared.jackson.addSerializer
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import org.litote.jackson.JacksonModuleServiceLoader

private object I18nTextSerializer : JsonSerializer<I18nText>() {
    override fun serialize(
        value: I18nText,
        gen: JsonGenerator,
        serializers: SerializerProvider,
    ) {
        gen.writeStartObject()
        gen.writeStringField(I18nText::text.name, value.text)
        if (value.args.isNotEmpty()) {
            serializers.defaultSerializeField(I18nText::args.name, value.args, gen)
        }
        if (!value.toBeTranslated) {
            gen.writeBooleanField(I18nText::toBeTranslated.name, false)
        }
        if (value.key != null) {
            gen.writeStringField(I18nText::key.name, value.key)
        }

        gen.writeEndObject()
    }
}

private object ClientApiJacksonConfiguration {
    val module: SimpleModule
        get() {
            val module = SimpleModule()
            module.addSerializer(I18nText::class, I18nTextSerializer)
            return module
        }
}

internal class ClientApiJacksonModuleServiceLoader : JacksonModuleServiceLoader {
    override fun module(): Module = ClientApiJacksonConfiguration.module
}
