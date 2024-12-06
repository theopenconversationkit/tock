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

import { ActionReport, BotMessage, DialogReport, PlayerId, PlayerType } from '../../shared/model/dialog-data';
import { ApplicationScopedQuery } from '../../model/commons';
import { ConnectorType, UserInterfaceType } from '../../core/model/configuration';

export class BotDialogRequest extends ApplicationScopedQuery {
  constructor(
    public botApplicationConfigurationId: string,
    public message: BotMessage,
    public override namespace: string,
    public override applicationName: string,
    public override language: string,
    public userIdModifier: string
  ) {
    super(namespace, applicationName, language);
  }
}

export class BotDialogResponse {
  constructor(
    public messages: BotMessage[],
    public hasNlpStats: boolean,
    public userActionId?: string,
    public userLocale?: string,
    public debug?: any
  ) {}

  static fromJSON(json?: any): BotDialogResponse {
    const value = Object.create(BotDialogResponse.prototype);

    const result = Object.assign(value, json, {
      messages: BotMessage.fromJSONArray(json.messages)
    });

    return result;
  }
}

export class TestMessage {
  constructor(
    public bot: boolean,
    public message?: BotMessage,
    public locale?: string,
    public actionId?: string,
    public hasNlpStats?: boolean
  ) {}
}

export class TestPlan {
  public testPlanExecutions: TestPlanExecution[];
  public botName: string;
  public displayDialog: boolean;
  public displayExecutions: boolean;

  constructor(
    public dialogs: TestDialogReport[],
    public name: string,
    public applicationId: string,
    public namespace: string,
    public nlpModel: string,
    public locale: string,
    public botApplicationConfigurationId: string,
    public targetConnectorType: ConnectorType,
    public _id?: string
  ) {}

  fillDialogExecutionReport(report: DialogExecutionReport) {
    if (report.error) {
      const d = this.dialogs.find((d) => report.dialogReportId === d.id);
      if (d) {
        const aIndex = d.actions.findIndex((a) => a.id === report.errorActionId);
        if (aIndex === -1) {
          report.dialogReport = new DialogReport(
            d.actions.map((d) => d.toActionReport()),
            d.id
          );
        } else {
          report.dialogReport = new DialogReport(
            d.actions.slice(0, Math.min(aIndex + 1, d.actions.length)).map((d) => d.toActionReport()),
            d.id
          );
        }
        report.dialogReport.displayActions = true;
      }
    }
  }

  static fromJSON(json?: any): TestPlan {
    const value = Object.create(TestPlan.prototype);

    const result = Object.assign(value, json, {
      dialogs: TestDialogReport.fromJSONArray(json.dialogs),
      targetConnectorType: ConnectorType.fromJSON(json.targetConnectorType)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): TestPlan[] {
    return json ? json.map(TestPlan.fromJSON) : [];
  }
}

export class TestPlanExecution {
  displayExecution: boolean;

  constructor(
    public testPlanId: string,
    public dialogs: DialogExecutionReport[],
    public nbErrors: number,
    public date: Date,
    public duration: Date,
    public status: string,
    public _id: string
  ) {}

  static fromJSON(json?: any): TestPlanExecution {
    const value = Object.create(TestPlanExecution.prototype);

    const result = Object.assign(value, json, {
      dialogs: DialogExecutionReport.fromJSONArray(json.dialogs)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): TestPlanExecution[] {
    return json ? json.map(TestPlanExecution.fromJSON) : [];
  }
}

export class DialogExecutionReport {
  dialogReport: DialogReport;

  constructor(
    public dialogReportId: string,
    public error: boolean,
    public errorActionId?: string,
    public returnedMessage?: BotMessage,
    public errorMessage?: string
  ) {}

  static fromJSON(json?: any): DialogExecutionReport {
    const value = Object.create(DialogExecutionReport.prototype);

    const result = Object.assign(value, json, {
      returnedMessage: BotMessage.fromJSON(json.returnedMessage)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): DialogExecutionReport[] {
    return json ? json.map(DialogExecutionReport.fromJSON) : [];
  }
}

export class TestDialogReport {
  displayActions: boolean;

  constructor(public actions: TestActionReport[], public id: string) {}

  toDialogReport(): DialogReport {
    return new DialogReport(
      this.actions.map((d) => d.toActionReport()),
      this.id
    );
  }

  static fromJSON(json?: any): TestDialogReport {
    const value = Object.create(TestDialogReport.prototype);

    const result = Object.assign(value, json, {
      actions: TestActionReport.fromJSONArray(json.actions)
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): TestDialogReport[] {
    return json ? json.map(TestDialogReport.fromJSON) : [];
  }
}

export class TestActionReport {
  constructor(
    public playerId: PlayerId,
    public date: Date,
    public messages: BotMessage[],
    public id: string,
    public userInterfaceType: UserInterfaceType,
    public connectorType?: ConnectorType
  ) {}

  toActionReport(): ActionReport {
    return new ActionReport(this.playerId, this.date, this.messages[0], this.id, true, this.connectorType);
  }

  isBot(): boolean {
    return this.playerId.type == PlayerType.bot;
  }

  static fromJSON(json?: any): TestActionReport {
    const value = Object.create(TestActionReport.prototype);

    const result = Object.assign(value, json, {
      playerId: PlayerId.fromJSON(json.playerId),
      messages: BotMessage.fromJSONArray(json.messages),
      connectorType: ConnectorType.fromJSON(json.connectorType),
      userInterfaceType: UserInterfaceType[json.userInterfaceType]
    });

    return result;
  }

  static fromJSONArray(json?: Array<any>): TestActionReport[] {
    return json ? json.map(TestActionReport.fromJSON) : [];
  }
}

export class XRayPlanExecutionConfiguration {
  constructor(public configurationId: string, public testPlanKey: string, public testedBotId: string) {}
}

export class XRayPlanExecutionResult {
  constructor(public success: number, public total: number) {}

  static fromJSON(json?: any): XRayPlanExecutionResult {
    const value = Object.create(XRayPlanExecutionResult.prototype);

    const result = Object.assign(value, json, {});

    return result;
  }
}

export class XRayTestPlan {
  constructor(public key: string, public fields: XRayField) {}
  static fromJSON(json?: any): XRayTestPlan {
    const value = Object.create(XRayTestPlan.prototype);

    const result = Object.assign(value, json, {});

    return result;
  }

  static fromJSONArray(json?: Array<any>): XRayTestPlan[] {
    return json ? json.map(XRayTestPlan.fromJSON) : [];
  }
}

export class XRayField {
  constructor(public summary: string) {}
  static fromJSON(json?: any): XRayField {
    const value = Object.create(XRayField.prototype);

    const result = Object.assign(value, json, {});

    return result;
  }
}
