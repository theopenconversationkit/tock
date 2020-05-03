/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

import {Component, ElementRef, OnInit, ViewChild} from "@angular/core";
import {Feature} from "../model/feature";
import {BotService} from "../bot-service";
import {StateService} from "../../core-nlp/state.service";
import {BotConfigurationService} from "../../core/bot-configuration.service";

@Component({
  selector: 'tock-application-feature',
  templateUrl: './application-feature.component.html',
  styleUrls: ['./application-feature.component.css']
})
export class ApplicationFeatureComponent implements OnInit {

  private currentApplicationUnsuscriber: any;
  features: Feature[] = [];
  create: boolean = false;
  feature: Feature = new Feature("", "", false);
  botApplicationConfigurationId: string;
  loadingApplicationsFeatures: boolean = false;
  @ViewChild('newCategory') newCategory: ElementRef;

  constructor(private state: StateService,
              private botService: BotService,
              private configurationService: BotConfigurationService) {
  }

  ngOnInit(): void {
    this.currentApplicationUnsuscriber = this.state.currentApplicationEmitter.subscribe(a => this.refresh());
    this.refresh();
  }

  prepareCreate() {
    this.create = true;
    setTimeout(_ => this.newCategory.nativeElement.focus());
  }

  cancelCreate() {
    this.create = false;
  }

  changeStartDateNew(newState: Date) {
    this.feature.startDate = newState;
  }

  changeEndDateNew(newState: Date) {
    this.feature.endDate = newState;
  }

  toggleNew(newState: boolean) {
    this.feature.enabled = newState;
  }

  changeStartDate(f: Feature, newState) {
    f.startDate = newState;
    this.update(f);
  }

  changeEndDate(f: Feature, newState) {
    f.endDate = newState;
    this.update(f);
  }

  refresh() {
    if (this.state.currentApplication) {
      this.loadingApplicationsFeatures = true;
      this.botService.getFeatures(this.state.currentApplication.name).subscribe(f => {
        f.forEach(feature => {
          if(feature.applicationId) {
            feature.configuration = this.configurationService.findApplicationConfigurationByApplicationId(feature.applicationId);
          }
        });
        this.features = f;
        this.loadingApplicationsFeatures = false;
      });
    }
  }

  addFeature() {
    const conf = this.configurationService.findApplicationConfigurationById(this.botApplicationConfigurationId);
    if (conf) {
      this.feature.applicationId = conf.applicationId;
    }
    this.botService.addFeature(this.state.currentApplication.name, this.feature).subscribe(
      _ => {
        this.refresh();
        this.create = false;
        this.botApplicationConfigurationId = undefined;
        this.feature.applicationId = undefined;
      }
    );
  }

  toggle(f: Feature, newState) {
    f.enabled = newState;
    if (!newState) {
      f.startDate = null;
      f.endDate = null;
    }
    this.botService.toggleFeature(this.state.currentApplication.name, f).subscribe();
  }

  update(f: Feature) {
    this.botService.updateDateAndEnableFeature(this.state.currentApplication.name, f).subscribe(_ => this.refresh());
  }

  deleteFeature(f: Feature) {
    this.botService.deleteFeature(this.state.currentApplication.name, f.category, f.name, f.applicationId)
      .subscribe(_ => this.refresh());
  }

}
