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

import { Component, Inject, OnInit } from '@angular/core';
import { TestDialogReport, TestPlan, XRayPlanExecutionConfiguration } from '../model/test';
import { TestService } from '../test.service';
import { StateService } from '../../core-nlp/state.service';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { ActionReport, DialogReport } from '../../shared/model/dialog-data';
import { BotSharedService } from '../../shared/bot-shared.service';
import { NbToastrService } from '@nebular/theme';
import { APP_BASE_HREF } from '@angular/common';
import { getDialogMessageUserAvatar, getDialogMessageUserQualifier } from '../../shared/utils';
import { SelectBotEvent } from '../../shared/components';
import { take } from 'rxjs';

@Component({
  selector: 'tock-bot-test-plan',
  templateUrl: './test-plan.component.html',
  styleUrls: ['./test-plan.component.css']
})
export class TestPlanComponent implements OnInit {
  testPlans: TestPlan[];
  runningTestPlan: TestPlan;

  testPlanCreation: boolean;
  testPlanName: string;
  testBotConfigurationId: string;
  testPlanId: string;
  testExecutionId: string;
  testExecutionStatus: string;

  executePlan: boolean;

  xray: XRayPlanExecutionConfiguration;
  xrayBotConfigurationId: string;
  executeXray: boolean = false;

  constructor(
    private state: StateService,
    private test: TestService,
    private toastrService: NbToastrService,
    public botConfiguration: BotConfigurationService,
    private shared: BotSharedService,
    @Inject(APP_BASE_HREF) public baseHref: string
  ) {}

  // loop to check test plan executions status
  ngOnInit(): void {
    this.reload();
    setInterval((_) => {
      if (null != this.testPlanId && null != this.testExecutionId && 'COMPLETE' !== this.testExecutionStatus) {
        this.getExecutionStatus(this.testPlanId, this.testExecutionId);
      }
      if ('COMPLETE' === this.testExecutionStatus) {
        this.executePlan = false;
      }
    }, 2000);
  }

  private reload() {
    this.test.getTestPlans().subscribe((p) => {
      this.botConfiguration.restConfigurations.subscribe((c) => {
        p.forEach((plan) => {
          const conf = c.find((c) => c._id === plan.botApplicationConfigurationId);
          if (conf) {
            plan.botName = conf.name;
          }
          this.showExecutions(plan);
        });
        this.testPlans = p;

        if (c.length !== 0)
          this.shared.getConfiguration().subscribe((r) => {
            if (r.xrayAvailable) {
              this.xray = new XRayPlanExecutionConfiguration('', '', '');
            }
          });
      });
    });
  }

  changeBotConfiguration(event: SelectBotEvent) {
    this.xrayBotConfigurationId = event ? event.configurationId : undefined;
  }

  executeXRay() {
    if (this.xray.testPlanKey.trim().length === 0) {
      this.toastrService.show(`Please specify a plan key`, 'Error', { duration: 2000 });
    } else {
      this.executeXray = true;
      this.botConfiguration.restConfigurations.subscribe((c) => {
        const conf = c.find((i) => i._id === this.xrayBotConfigurationId);
        this.xray.configurationId = this.xrayBotConfigurationId;
        this.xray.testedBotId = conf ? conf.botId : c[0].botId;
        this.test.executeXRay(this.xray).subscribe(
          (r) => {
            this.executeXray = false;
            this.reload();
            if (r.total === 0) {
              this.toastrService.show(`No tests executed for Plan ${this.xray.testPlanKey}`, 'Execution', { duration: 2000 });
            } else if (r.total === r.success) {
              this.toastrService.show(`${r.total} tests for Plan ${this.xray.testPlanKey} executed with success`, 'Execution', {
                duration: 2000
              });
            } else {
              this.toastrService.show(
                `Plan ${this.xray.testPlanKey} executed with ${r.success} successful tests / ${r.total}`,
                'Execution',
                { duration: 2000 }
              );
            }
          },
          (_) => (this.executeXray = false)
        );
      });
    }
  }

  prepareCreateTestPlan() {
    this.testPlanCreation = true;
    this.testBotConfigurationId = this.botConfiguration.restConfigurations.value[0]._id;
  }

  createTestPlan() {
    if (!this.testPlanName || this.testPlanName.trim().length === 0) {
      this.toastrService.show(`Please enter a valid name`, 'Error', { duration: 5000 });
      return;
    }
    const conf = this.botConfiguration.restConfigurations.value.find((c) => c._id === this.testBotConfigurationId);
    this.test
      .saveTestPlan(
        new TestPlan(
          [],
          this.testPlanName.trim(),
          conf.applicationId,
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          this.testBotConfigurationId,
          conf.ownerConnectorType
        )
      )
      .subscribe((_) => {
        this.resetCreateTestPlan();
        this.reload();
        this.toastrService.show(`New test plan saved`, 'New Test Plan', { duration: 2000 });
      });
  }

  resetCreateTestPlan() {
    this.testPlanCreation = false;
  }

  deleteTestPlan(plan: TestPlan) {
    this.test.removeTestPlan(plan._id).subscribe((_) => {
      this.reload();
      this.toastrService.show(`Plan ${plan.name} deleted`, 'Delete', { duration: 2000 });
    });
  }

  exec(plan: TestPlan) {
    this.executePlan = true;
    this.testExecutionStatus = 'PENDING';
    this.runningTestPlan = plan;
    const ref = this.toastrService.show(`Plan ${plan.name} is running.`, 'Execution', {
      duration: 20000
    });
    this.test.runTestPlan(plan._id).subscribe((execution) => {
      ref.close();
      this.testExecutionId = execution;
      this.showExecutions(plan);
      this.getExecutionStatus(this.testPlanId, this.testExecutionId);
    });
    this.testPlanId = plan._id;
  }

  removeDialog(plan: TestPlan, dialog: TestDialogReport) {
    this.test.removeDialogFromTestPlan(plan._id, dialog.id).subscribe((_) => {
      plan.dialogs = plan.dialogs.filter((d) => d.id !== dialog.id);
      this.toastrService.show(`Dialog removed`, 'Removal', { duration: 2000 });
    });
  }

  showDialogs(plan: TestPlan) {
    plan.displayDialog = true;
  }

  hideDialogs(plan: TestPlan) {
    plan.displayDialog = false;
  }

  showExecutions(plan: TestPlan) {
    this.test
      .getTestPlanExecutions(plan._id)
      .pipe(take(1))
      .subscribe((e) => {
        plan.testPlanExecutions = e;
        //fill errors
        e.forEach((e) => e.dialogs.forEach((d) => plan.fillDialogExecutionReport(d)));
        plan.displayExecutions = true;
      });
  }

  /**
   * Retrieve the status of the given test execution and store the status into the variable testExecutionStatus
   * which is used at the beginning of the file.
   *
   * @param testPlanId - Identifier of the running test plan
   * @param testExecutionId - Identifier of the test plan execution
   */
  getExecutionStatus(testPlanId: string, testExecutionId: string) {
    this.test
      .getTestPlanExecutionStatus(testPlanId, testExecutionId)
      .pipe(take(1))
      .subscribe((e) => (this.testExecutionStatus = e.status));
  }

  hideExecutions(plan: TestPlan) {
    plan.displayExecutions = false;
  }

  getUserName(action: ActionReport): string {
    return getDialogMessageUserQualifier(action.isBot());
  }

  getUserAvatar(action: ActionReport): string {
    return getDialogMessageUserAvatar(action.isBot());
  }
}
