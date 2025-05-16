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

export class User {
  constructor(public email: string, public organization: string, public roles: UserRole[]) {}

  static fromJSON(json: any): User {
    const value = Object.create(User.prototype);
    const result = Object.assign(value, json, {
      roles: json.roles.map((r) => UserRole[r]),
      //fallback to namespace
      organization: json.organization ? json.organization : json.namespace
    });

    return result;
  }
}

export class AuthenticateRequest {
  constructor(public email: string, public password: string) {}
}

export class AuthenticateResponse {
  constructor(public authenticated: boolean, public email?: string, public organization?: string, public roles?: UserRole[]) {}

  static fromJSON(json: any): AuthenticateResponse {
    const value = Object.create(AuthenticateResponse.prototype);
    const result = Object.assign(value, json, {
      roles: json.roles.map((r) => UserRole[r])
    });

    return result;
  }

  toUser(): User {
    return new User(this.email, this.organization, this.roles);
  }
}

export enum UserRole {
  /**
   * A nlp user is allowed to qualify and search sentences, but not to update applications or builds.
   */
  nlpUser,
  /**
   * A bot user is allowed to modify answer & i18n, and to consult dialogs and conversations.
   */
  botUser,
  /**
   * An admin is allowed to update applications and builds, and to export/intent sentences dump.
   */
  admin,
  /**
   * A technical admin has access to all encrypted sentence, and to export/intent application dumps.
   */
  technicalAdmin
}
