/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import {Entry} from "../../../model/commons";

export class FaqDefinitionFilter {
  constructor(
    public enabled?: Boolean,
    public search?: string,
    public sort?: Entry<string, boolean>[],
    public tags?: string[],
  ) {
  }

  clone(): FaqDefinitionFilter {
    return {...this}; // shallow copy
  }
}
