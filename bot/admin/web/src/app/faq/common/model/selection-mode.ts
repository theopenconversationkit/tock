/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

/**
 * Selection Mode for training view checkboxes
 *
 * SELECT_ALWAYS:
 *  no need to recompute each checkbox state as they are always selected
 *  in addition future items which will appear when scrolling will be considered selected
 *
 * SELECT_NEVER:
 *  no need to recompute each checkbox state as they are always not selected
 *  in addition future items which will appear when scrolling will be considered not selected
 *
 * SELECT_SOME:
 *  we dont know for sure which checkbox si selected or not
 *  in addition future items which will appear when scrolling will be considered not selected
 */
export enum SelectionMode {
  SELECT_ALWAYS = 'SELECT_ALWAYS',
  SELECT_NEVER = 'SELECT_NEVER',
  SELECT_SOME = 'SELECT_SOME'
}
