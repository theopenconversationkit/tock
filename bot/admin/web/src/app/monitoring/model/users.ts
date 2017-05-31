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

import {PaginatedQuery} from "tock-nlp-admin/src/app/model/commons";

export class UserSearchQuery extends PaginatedQuery {

  constructor(public namespace: string,
              public applicationName: string,
              public language: string,
              public start: number,
              public size: number,
              public name?: String,
              public from?: Date,
              public to?: Date) {
    super(namespace, applicationName, language, start, size)
  }
}

export class UserReportQueryResult {

  constructor(public total: number,
              public start: number,
              public end: number,
              public users: UserReport[]) {
  }

  static fromJSON(json?: any): UserReportQueryResult {
    const value = Object.create(UserReportQueryResult.prototype);

    const result = Object.assign(value, json, {
      users: UserReport.fromJSONArray(json.users),
    });

    return result;
  }
}

export class UserReport {

  constructor(public playerId: PlayerId,
              public userPreferences: UserPreferences,
              public userState: UserState,
              public lastUpdateDate: Date,
              public lastActionText?: string) {
  }

  static fromJSON(json?: any): UserReport {
    const value = Object.create(UserReport.prototype);

    const result = Object.assign(value, json, {
      playerId: PlayerId.fromJSON(json.playerId),
      userPreferences: UserPreferences.fromJSON(json.userPreferences),
      userState: UserState.fromJSON(json.userState),
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): UserReport[] {
    return json ? json.map(UserReport.fromJSON) : [];
  }

}

export class UserPreferences {

  constructor(public firstName?: string,
              public lastName?: string,
              public email?: string,
              public timezone?: string,
              public locale?: string,
              public picture?: string,
              public gender?: string) {

  }

  static fromJSON(json?: any): UserPreferences {
    const value = Object.create(UserPreferences.prototype);

    const result = Object.assign(value, json, {});

    return result;
  }

}

export class UserState {

  constructor(public creationDate: Date,
              public flags: any) {
  }

  static fromJSON(json?: any): UserState {
    const value = Object.create(UserState.prototype);

    const result = Object.assign(value, json, {});

    return result;
  }

}

export class PlayerId {

  constructor(public id: string,
              public type: PlayerType = PlayerType.user) {
  }

  static fromJSON(json?: any): PlayerId {
    const value = Object.create(PlayerId.prototype);

    const result = Object.assign(value, json, {
      type: PlayerType[json.type],
    });

    return result;
  }

}

export enum PlayerType {
  user, bot
}



