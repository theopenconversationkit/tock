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

import { ApplicationScopedQuery } from '../../model/commons';
import { AnswerConfigurationType } from '../../bot/model/story';

export class DialogFlowRequest extends ApplicationScopedQuery {
  constructor(
    public override namespace: string,
    public override applicationName: string,
    public override language: string,
    public botId: string,
    public botConfigurationName: string,
    public botConfigurationId?: string,
    public from?: string,
    public to?: string,
    public includeTestConfigurations?: boolean,
    public intent?: string
  ) {
    super(namespace, applicationName, language);
  }

  equals(r: DialogFlowRequest): boolean {
    return (
      r &&
      this.namespace === r.namespace &&
      this.applicationName === r.applicationName &&
      this.language === r.language &&
      this.botId === r.botId &&
      this.botConfigurationName === r.botConfigurationName &&
      this.botConfigurationId === r.botConfigurationId &&
      this.from === r.from &&
      this.to === r.to &&
      this.includeTestConfigurations === r.includeTestConfigurations &&
      this.intent === r.intent
    );
  }
}

export class ApplicationDialogFlow {
  constructor(public states: DialogFlowStateData[], public transitions: DialogFlowStateTransitionData[]) {}

  static fromJSON(json: any): ApplicationDialogFlow {
    const value = Object.create(ApplicationDialogFlow.prototype);
    const result = Object.assign(value, json, {
      states: DialogFlowStateData.fromJSONArray(json.states),
      transitions: DialogFlowStateTransitionData.fromJSONArray(json.transitions)
    });
    return result;
  }
}

export class DialogFlowStateData {
  constructor(
    public storyDefinitionId: string,
    public intent: string,
    public entities: string[],
    public step?: string,
    public storyType?: AnswerConfigurationType,
    public storyName?: string,
    public count?: number,
    public _id?: string
  ) {}

  static fromJSON(json: any): DialogFlowStateData {
    const value = Object.create(DialogFlowStateData.prototype);
    const result = Object.assign(value, json, {
      storyType: AnswerConfigurationType[json.storyType]
    });
    return result;
  }

  static fromJSONArray(json?: Array<any>): DialogFlowStateData[] {
    return json ? json.map(DialogFlowStateData.fromJSON) : [];
  }
}

export class DialogFlowStateTransitionData {
  constructor(
    public nextStateId: string,
    public newEntities: string[],
    public type: DialogFlowStateTransitionType,
    public previousStateId?: string,
    public step?: string,
    public count?: number
  ) {}

  static fromJSON(json: any): DialogFlowStateTransitionData {
    const value = Object.create(DialogFlowStateTransitionData.prototype);
    const result = Object.assign(value, json, {
      type: DialogFlowStateTransitionType[json.type]
    });
    return result;
  }

  static fromJSONArray(json?: Array<any>): DialogFlowStateTransitionData[] {
    return json ? json.map(DialogFlowStateTransitionData.fromJSON) : [];
  }
}

export enum DialogFlowStateTransitionType {
  nlp,
  choice,
  attachment,
  location,
  other
}
