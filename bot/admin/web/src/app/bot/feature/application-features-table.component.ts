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

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Feature } from '../model/feature';
import { BotService } from '../bot-service';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { StateService } from '../../core-nlp/state.service';
import { NbToastrService } from '@nebular/theme';

@Component({
  selector: 'tock-application-features-table',
  templateUrl: './application-features-table.component.html',
  styleUrls: ['./application-features-table.component.css']
})
export class ApplicationFeaturesTableComponent implements OnInit {
  @Input()
  title: string;

  @Input()
  botApplicationConfigurationId: string;

  @Input()
  tockCategory: boolean = false;

  @Input()
  features: Feature[] = [];

  @Output()
  onRefresh = new EventEmitter<boolean>();

  create: boolean = false;
  feature: Feature = new Feature('', '', false);

  constructor(
    private state: StateService,
    private botService: BotService,
    private configurationService: BotConfigurationService,
    private toastrService: NbToastrService
  ) {}

  ngOnInit(): void {
    this.feature.category = this.tockCategory ? 'tock' : 'myCategory';
  }

  prepareCreate() {
    this.create = true;
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

  toggle(f: Feature, newState) {
    f.enabled = newState;
    if (!newState) {
      f.startDate = null;
      f.endDate = null;
    }
    this.botService.toggleFeature(this.state.currentApplication.name, f).subscribe();
  }

  update(f: Feature) {
    this.botService
      .updateDateAndEnableFeature(this.state.currentApplication.name, f)
      .subscribe((_) => this.onRefresh.emit(true));
  }

  deleteFeature(f: Feature) {
    this.botService
      .deleteFeature(this.state.currentApplication.name, f.category, f.name, f.applicationId)
      .subscribe((_) => this.onRefresh.emit(true));
  }

  addFeature() {
    if (this.feature.name.trim().length === 0 || this.feature.category.trim().length === 0) {
      this.toastrService.show(`name and category are mandatory`, 'Error', { duration: 3000 });
    } else {
      const conf = this.configurationService.findApplicationConfigurationById(
        this.botApplicationConfigurationId
      );
      if (conf) {
        this.feature.applicationId = conf.applicationId;
      }

      this.botService
        .addFeature(this.state.currentApplication.name, this.feature)
        .subscribe((_) => {
          this.onRefresh.emit(true);
          this.create = false;
          this.botApplicationConfigurationId = undefined;
          this.feature.applicationId = undefined;
        });
    }
  }
}
