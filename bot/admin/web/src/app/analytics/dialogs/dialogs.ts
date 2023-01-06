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
import { PaginatedQuery } from '../../model/commons';
import { DialogReport, PlayerId } from '../../shared/model/dialog-data';
import { PaginatedResult } from '../../model/nlp';
import { ConnectorType } from '../../core/model/configuration';

export class DialogReportQuery extends PaginatedQuery {
  constructor(
    public namespace: string,
    public applicationName: string,
    public language: string,
    public start: number,
    public size: number,
    public exactMatch: boolean,
    public playerId?: PlayerId,
    public dialogId?: string,
    public text?: string,
    public intentName?: string,
    public connectorType?: ConnectorType,
    public displayTests?: boolean
  ) {
    super(namespace, applicationName, language, start, size);
  }
}

export class DialogReportQueryResult implements PaginatedResult<DialogReport> {
  constructor(public total: number, public start: number, public end: number, public rows: DialogReport[]) {}

  static fromJSON(json?: any): DialogReportQueryResult {
    const value = Object.create(DialogReportQueryResult.prototype);

    const result = Object.assign(value, json, {
      rows: DialogReport.fromJSONArray(json.dialogs)
    });

    return result;
  }
}
