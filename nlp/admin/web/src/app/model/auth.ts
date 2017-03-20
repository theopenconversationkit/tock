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

export class User {
  constructor(public email: string,
    public organization: string) {
  }
}

export class AuthenticateRequest {
  constructor(public email: string, public password: string) {
  }
}

export class AuthenticateResponse {
  constructor(public authenticated: boolean,
    public email?: string,
    public organization?: string) {
  }

  static fromJSON(json: any): AuthenticateResponse {
    const value = Object.create(AuthenticateResponse.prototype);
    const result = Object.assign(value, json, {});

    return result;
  }

  toUser(): User {
    return new User(this.email, this.organization);
  }
}
