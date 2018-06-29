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

import {Component, OnInit} from "@angular/core";
import {TestPlan, XRayPlanExecutionConfiguration} from "../model/test";
import {TestService} from "../test.service";
import {StateService} from "tock-nlp-admin/src/app/core/state.service";
import {MdSnackBar} from "@angular/material";
import {BotConfigurationService} from "app/core/bot-configuration.service";
import {DialogReport} from "../../shared/model/dialog-data";

@Component({
  selector: 'tock-bot-test-plan',
  templateUrl: './test-plan.component.html',
  styleUrls: ['./test-plan.component.css']
})
export class TestPlanComponent implements OnInit {

  testPlans: TestPlan[];

  testPlanCreation: boolean;
  testPlanName: string;
  testBotConfigurationId: string;

  executePlan: boolean;

  xray: XRayPlanExecutionConfiguration;
  xrayBotConfigurationId: string;
  executeXray: boolean = false;

  constructor(private state: StateService,
              private test: TestService,
              private snackBar: MdSnackBar,
              public botConfiguration: BotConfigurationService) {
  }

  ngOnInit(): void {
    this.reload();
  }

  private reload() {
    this.test
      .getTestPlans()
      .subscribe(p => {
        this.botConfiguration.restConfigurations.subscribe(c => {
          p.forEach(plan => {
            const conf = c.find(c => c._id === plan.botApplicationConfigurationId);
            if (conf) {
              plan.botName = conf.name;
            }
          });
          this.testPlans = p;

          if (c.length !== 0)
            this.test.isXrayAvailable().subscribe(r => {
                if (r.success) {
                  this.xray = new XRayPlanExecutionConfiguration("", "", "");
                }
              }
            );
        });
      });
  }

  executeXRay() {
    if (this.xray.testPlanKey.trim().length === 0) {
      this.snackBar.open(`Please specify a plan key`, "Error", {duration: 2000})
    } else {
      this.executeXray = true;
      this.botConfiguration.restConfigurations.subscribe(c => {
        const conf = c.find(i => i._id === this.xrayBotConfigurationId);
        this.xray.configurationId = this.xrayBotConfigurationId;
        this.xray.testedBotId = conf ? conf.botId : c[0].botId;
        this.test.executeXRay(this.xray).subscribe(r => {
          this.executeXray = false;
          if (r.total === 0) {
            this.snackBar.open(`No tests executed for Plan ${this.xray.testPlanKey}`, "Execution", {duration: 2000})
          } else if (r.total === r.success) {
            this.snackBar.open(`${r.total} tests for Plan ${this.xray.testPlanKey} executed with success`, "Execution", {duration: 2000})
          } else {
            this.snackBar.open(`Plan ${this.xray.testPlanKey} executed with ${r.success} successful tests / ${r.total}`, "Execution", {duration: 2000})
          }
        });
      });
    }
  }

  prepareCreateTestPlan() {
    this.testPlanCreation = true;
    this.testBotConfigurationId = this.botConfiguration.restConfigurations.value[0]._id;
  }

  createTestPlan() {
    if (!this.testPlanName || this.testPlanName.trim().length === 0) {
      this.snackBar.open(`Please enter a valid name`, "Error", {duration: 5000});
      return;
    }
    const conf = this.botConfiguration.restConfigurations.value.find(c => c._id === this.testBotConfigurationId);
    this.test.saveTestPlan(
      new TestPlan(
        [],
        this.testPlanName.trim(),
        conf.applicationId,
        this.state.currentApplication.namespace,
        this.state.currentApplication.name,
        this.state.currentLocale,
        this.testBotConfigurationId,
        conf.ownerConnectorType
      )).subscribe(_ => {
      this.resetCreateTestPlan();
      this.reload();
      this.snackBar.open(`New test plan saved`, "New Test Plan", {duration: 2000})
    });
  }

  resetCreateTestPlan() {
    this.testPlanCreation = false;
  }

  deleteTestPlan(plan: TestPlan) {
    this.test.removeTestPlan(plan._id).subscribe(
      _ => {
        this.reload();
        this.snackBar.open(`Plan ${plan.name} deleted`, "Delete", {duration: 2000})
      });
  }

  exec(plan: TestPlan) {
    this.executePlan = true;
    this.test.runTestPlan(plan._id).subscribe(
      execution => {
        this.executePlan = false;
        this.showExecutions(plan);
        this.snackBar.open(`Plan ${plan.name} executed with ${execution.nbErrors === 0 ? 'success' : execution.nbErrors + ' errors'}`, "Execution", {duration: 2000})
      })
  }

  removeDialog(plan: TestPlan, dialog: DialogReport) {
    this.test
      .removeDialogFromTestPlan(plan._id, dialog.id)
      .subscribe(_ => {
        plan.dialogs = plan.dialogs.filter(d => d.id !== dialog.id);
        this.snackBar.open(`Dialog removed`, "Removal", {duration: 2000})
      });
  }

  showDialogs(plan: TestPlan) {
    plan.displayDialog = true;
  }

  hideDialogs(plan: TestPlan) {
    plan.displayDialog = false;
  }

  showExecutions(plan: TestPlan) {
    this.test.getTestPlanExecutions(plan._id)
      .subscribe(e => {
        plan.testPlanExecutions = e;
        //fill errors
        e.forEach(e => e.dialogs.forEach(d => plan.fillDialogExecutionReport(d)));
        plan.displayExecutions = true;
      });
  }

  hideExecutions(plan: TestPlan) {
    plan.displayExecutions = false;
  }
}
