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

import { PaginatedQuery } from '../../model/commons';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { TestPlan } from '../../test/model/test';
import { DialogReport, PlayerId } from '../../shared/model/dialog-data';
import { PaginatedResult } from '../../model/nlp';

export class UserSearchQuery extends PaginatedQuery {
  constructor(
    public override namespace: string,
    public override applicationName: string,
    public override language: string,
    public override start: number,
    public override size: number,
    public name?: String,
    public from?: Date,
    public to?: Date,
    public flags?: string[],
    public displayTests?: boolean,
    public intent?: string
  ) {
    super(namespace, applicationName, language, start, size);
  }
}

export class UserReportQueryResult implements PaginatedResult<UserReport> {
  constructor(public total: number, public start: number, public end: number, public rows: UserReport[]) {}

  static fromJSON(json?: any): UserReportQueryResult {
    const value = Object.create(UserReportQueryResult.prototype);

    const result = Object.assign(value, json, {
      rows: UserReport.fromJSONArray(json.users)
    });

    return result;
  }
}

export class UserAnalyticsQueryResult {
  constructor(public usersData: any[][], public dates: Date[], public connectorsType: [], public intents: []) {}

  static fromJSON(json?: any): UserAnalyticsQueryResult {
    const value = Object.create(UserAnalyticsQueryResult.prototype);

    const result = Object.assign(value, json, {});

    return result;
  }
}

export class UserReport {
  displayDialogs: boolean = false;
  userDialog: DialogReport;
  botConfiguration: BotApplicationConfiguration;
  testPlans: TestPlan[];

  constructor(
    public playerId: PlayerId,
    public userPreferences: UserPreferences,
    public userState: UserState,
    public lastUpdateDate: Date,
    public applicationIds: string[],
    public lastActionText?: string
  ) {}

  static fromJSON(json?: any): UserReport {
    const value = Object.create(UserReport.prototype);

    const result = Object.assign(value, json, {
      playerId: PlayerId.fromJSON(json.playerId),
      userPreferences: UserPreferences.fromJSON(json.userPreferences),
      userState: UserState.fromJSON(json.userState)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): UserReport[] {
    return json ? json.map(UserReport.fromJSON) : [];
  }
}

export class UserPreferences {
  constructor(public firstName?: string, public lastName?: string, public locale?: string, public picture?: string) {}

  static fromJSON(json?: any): UserPreferences {
    const value = Object.create(UserPreferences.prototype);

    const result = Object.assign(value, json, {});

    return result;
  }
}

export class UserState {
  constructor(public creationDate: Date, public flags: string[]) {}

  static fromJSON(json?: any): UserState {
    const value = Object.create(UserState.prototype);

    const result = Object.assign(value, json, {});

    return result;
  }
}
