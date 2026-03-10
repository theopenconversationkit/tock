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

package ai.tock.bot.admin.model.dataset

import ai.tock.bot.admin.model.ToValidate
import ai.tock.shared.intProperty

private const val DATASET_MAX_QUESTIONS_PROPERTY = "tock_datasets_max_questions"
private const val DEFAULT_MAX_QUESTIONS = 200

private val datasetMaxQuestions: Int by lazy {
    intProperty(DATASET_MAX_QUESTIONS_PROPERTY, DEFAULT_MAX_QUESTIONS)
}

data class DatasetQuestionRequest(
    val id: String? = null,
    val question: String,
    val groundTruth: String? = null,
)

data class DatasetCreateRequest(
    val name: String,
    val description: String = "",
    val questions: List<DatasetQuestionRequest>,
) : ToValidate {
    override fun validate(): List<String> = validateDatasetRequest(name, questions)
}

data class DatasetUpdateRequest(
    val name: String,
    val description: String = "",
    val questions: List<DatasetQuestionRequest>,
) : ToValidate {
    override fun validate(): List<String> = validateDatasetRequest(name, questions)
}

data class DatasetRunCreateRequest(
    val language: String,
) : ToValidate {
    override fun validate(): List<String> {
        val trimmedLanguage = language.trim()
        return if (trimmedLanguage.isEmpty()) {
            listOf("language is required")
        } else {
            emptyList()
        }
    }
}

data class DatasetRunCancelRequest(
    val ignored: Boolean? = null,
)

private fun validateDatasetRequest(
    name: String,
    questions: List<DatasetQuestionRequest>,
): List<String> {
    val errors = mutableListOf<String>()

    if (name.isBlank()) {
        errors.add("Dataset name is required")
    }

    if (questions.isEmpty()) {
        errors.add("Dataset must contain at least one question")
    }

    if (questions.size > datasetMaxQuestions) {
        errors.add("Dataset must not contain more than $datasetMaxQuestions questions")
    }

    questions.forEachIndexed { index, question ->
        val questionLabel = "Question #${index + 1}"
        val trimmedQuestion = question.question.trim()

        if (trimmedQuestion.isEmpty()) {
            errors.add("$questionLabel is required")
        }
    }

    return errors
}
