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

package ai.tock.bot.admin.test

/**
 * Status available for a test plan execution.
 * PENDING - the test plan is currently executed.
 * SUCCESS - the test plan has been executed and ended without any errors.
 * FAILED - the test plan has been executed but some errors occurred.
 * COMPLETE - the test plan has been executed but there is no more information about test success.
 */
enum class TestPlanExecutionStatus {
    PENDING,
    SUCCESS,
    FAILED,
    COMPLETE,
    UNKNOWN,
}
