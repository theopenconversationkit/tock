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
import { ActionNlpStats, DialogReport, PlayerId } from '../../shared/model/dialog-data';
import { PaginatedResult } from '../../model/nlp';
import { ConnectorType } from '../../core/model/configuration';
import { AnnotationReason, AnnotationState } from '../../shared/components/annotation/annotations';
import { SortOrder } from '../../shared/model/misc';

export class DialogReportQuery extends PaginatedQuery {
  constructor(
    public override namespace: string,
    public override applicationName: string,
    public override language: string,
    public override start: number,
    public override size: number,
    public exactMatch: boolean,
    public playerId?: PlayerId,
    public dialogId?: string,
    public text?: string,
    public intentName?: string,
    public connectorType?: ConnectorType,
    public displayTests?: boolean,
    public ratings?: number[],
    public applicationId?: string,
    public intentsToHide?: string[],
    public isGenAiRagDialog?: boolean,
    public dialogSort?: SortOrder,
    public dialogCreationDateFrom?: Date,
    public dialogCreationDateTo?: Date,
    public withAnnotations?: boolean,
    public annotationStates?: AnnotationState[],
    public annotationReasons?: AnnotationReason[],
    public annotationSort?: SortOrder,
    public annotationCreationDateFrom?: Date,
    public annotationCreationDateTo?: Date
  ) {
    super(namespace, applicationName, language, start, size);
  }
}

export class DialogReportQueryResult implements PaginatedResult<DialogReport> {
  constructor(public total: number, public start: number, public end: number, public rows: DialogReport[]) {}

  nlpStats?: ActionNlpStats[];

  static fromJSON(json?: any): DialogReportQueryResult {
    const value = Object.create(DialogReportQueryResult.prototype);

    const result = Object.assign(value, json, {
      rows: DialogReport.fromJSONArray(json.dialogs)
    });

    return result;
  }
}
