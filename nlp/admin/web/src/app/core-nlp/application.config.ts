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

import {Injectable} from "@angular/core";
import {UserRole} from "../model/auth";

@Injectable()
export class ApplicationConfig {

  /** url of the configuration menu */
  configurationUrl: string;
  /** url of the display dialogs if it exists */
  displayDialogUrl: string;
  /** url to answer to sentence if it exists */
  answerToSentenceUrl: string;
  /** url map for each default rights */
  roleMap: Map<UserRole, string>
}
