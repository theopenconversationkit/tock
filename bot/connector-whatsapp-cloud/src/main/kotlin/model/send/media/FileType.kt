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

package ai.tock.bot.connector.whatsapp.cloud.model.send.media

import com.fasterxml.jackson.annotation.JsonValue

enum class FileType(
    @JsonValue val type: String,
) {
    JPEG("image/jpeg"),
    PNG("image/png"),
    TEXT("text/plain"),
    PDF("application/pdf"),
    PPT("application/vnd.ms-powerpoint"),
    DOC("application/msword"),
    XLS("application/vnd.ms-excel"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    AAC("audio/aac"),
    MP4("audio/mp4"),
    MPEG("audio/mpeg"),
    AMR("audio/amr"),
    OGG("audio/ogg"),
    OPUS("audio/opus"),
    MP4_VIDEO("video/mp4"),
    THREEGP("video/3gp"),
    WEBP("image/webp"),
}
