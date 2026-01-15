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

export class AdminConfiguration {
  constructor(
    public botApiSupport: boolean,
    public compilerAvailable: boolean,
    public xrayAvailable: boolean,
    public botApiBaseUrl: string,
    public globalMessage: string
  ) {}

  static fromJSON(json?: any): AdminConfiguration {
    const value = Object.create(AdminConfiguration.prototype);

    const result = Object.assign(value, json, {});

    return result;
  }
}
