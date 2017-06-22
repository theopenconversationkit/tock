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
import {TestPlan, TestPlanExecution} from "../model/test";
import {TestService} from "../test.service";
import {StateService} from "tock-nlp-admin/src/app/core/state.service";
import {MdSnackBar} from "@angular/material";
import {BotConfigurationService} from "app/core/bot-configuration.service";

@Component({
  selector: 'tock-bot-test-plan',
  templateUrl: './test-plan.component.html',
  styleUrls: ['./test-plan.component.css']
})
export class TestPlanComponent implements OnInit {

  testPlans: TestPlan[];
  testPlanExecutions: TestPlanExecution[];

  testPlanCreation: boolean;
  testPlanName: string;
  testBotConfigurationId: string;

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
        this.botConfiguration.configurations.subscribe(c => {
          p.forEach(plan => {
            const conf = c.find(c => c._id === plan.botApplicationConfigurationId);
            if (conf) {
              plan.botName = conf.name;
            }
          });
          this.testPlans = p
        })
      });
  }

  prepareCreateTestPlan() {
    this.testPlanCreation = true;
    this.testBotConfigurationId = this.botConfiguration.configurations.value[0]._id;
  }

  createTestPlan() {
    if (!this.testPlanName || this.testPlanName.trim().length === 0) {
      this.snackBar.open(`Please enter a valid name`, "Error", {duration: 5000});
      return;
    }
    const conf = this.botConfiguration.configurations.value.find(c => c._id === this.testBotConfigurationId);
    this.test.saveTestPlan(
      new TestPlan(
        [],
        this.testPlanName.trim(),
        conf.applicationId,
        this.state.currentApplication.namespace,
        this.state.currentApplication.name,
        this.state.currentLocale,
        this.testBotConfigurationId
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
    this.test.runTestPlan(plan._id).subscribe(
      execution => {
        this.snackBar.open(`Plan ${plan.name} executed with ${execution.nbErrors === 0 ? 'success' : execution.nbErrors + ' errors'}`, "Execution", {duration: 2000})
      })
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
        plan.displayExecutions = true;
      });
  }

  hideExecutions(plan: TestPlan) {
    plan.displayExecutions = false;
  }
}
