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

import { BotApplicationConfiguration } from '../../core/model/configuration';

export class Feature {
  configuration: BotApplicationConfiguration;

  constructor(
    public category: string,
    public name: string,
    public enabled: boolean,
    public startDate: Date = null,
    public endDate: Date = null,
    public applicationId: string = null,
    public graduation: number = null,
  ) {}

  static fromJSON(json: any): Feature {
    const value = Object.create(Feature.prototype);
    const result = Object.assign(value, json, {
      startDate: json.startDate ? new Date(json.startDate) : null,
      endDate: json.endDate ? new Date(json.endDate) : null
    });
    return result;
  }

  static fromJSONArray(json?: Array<any>): Feature[] {
    return json ? json.map(Feature.fromJSON) : [];
  }
}
