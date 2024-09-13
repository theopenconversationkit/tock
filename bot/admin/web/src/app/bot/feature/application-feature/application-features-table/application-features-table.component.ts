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

import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Feature } from '../../../model/feature';
import { BotService } from '../../../bot-service';
import { BotConfigurationService } from '../../../../core/bot-configuration.service';
import { StateService } from '../../../../core-nlp/state.service';
import { NbDialogService } from '@nebular/theme';
import { CreateFeatureComponent } from '../create-feature/create-feature.component';
import { take } from 'rxjs';
import { ChoiceDialogComponent } from '../../../../shared/components';

@Component({
  selector: 'tock-application-features-table',
  templateUrl: './application-features-table.component.html',
  styleUrls: ['./application-features-table.component.scss']
})
export class ApplicationFeaturesTableComponent {
  @Input() type: 'tock' | 'application';

  @Input() features: Feature[] = [];

  @Output() onRefresh = new EventEmitter<boolean>();

  constructor(
    private state: StateService,
    private botService: BotService,
    private configurationService: BotConfigurationService,
    private nbDialogService: NbDialogService
  ) {}

  newFeature(): void {
    this.nbDialogService
      .open(CreateFeatureComponent, {
        context: {
          type: this.type
        }
      })
      .componentRef.instance.onSave.pipe(take(1))
      .subscribe((res) => {
        this.createFeature(res);
      });
  }

  createFeature(feature): void {
    const conf = this.configurationService.findApplicationConfigurationById(feature.botApplicationConfigurationId);

    if (conf) {
      feature.applicationId = conf.applicationId;
    }

    this.botService.addFeature(this.state.currentApplication.name, feature).subscribe((_) => {
      this.onRefresh.emit(true);
    });
  }

  changeStartDate(feature: Feature, newState): void {
    feature.startDate = newState;
    this.update(feature);
  }

  changeEndDate(feature: Feature, newState): void {
    feature.endDate = newState;
    this.update(feature);
  }

  changeGraduation(feature: Feature, event: FocusEvent): void {
    const graduation = (event.target as HTMLInputElement).value;
    if(graduation === '')
      feature.graduation = undefined
    else
      feature.graduation = Number(graduation);
    this.update(feature);
  }

  toggle(feature: Feature, newState): void {
    feature.enabled = newState;
    if (!newState) {
      feature.startDate = null;
      feature.endDate = null;
      feature.graduation = null;
    }
    this.botService.toggleFeature(this.state.currentApplication.name, feature).subscribe();
  }

  update(f: Feature): void {
    this.botService.updateDateAndEnableFeature(this.state.currentApplication.name, f).subscribe((_) => this.onRefresh.emit(true));
  }

  askDeleteFeature(feature: Feature): void {
    const confirmAction = 'Delete';
    const cancelAction = 'Cancel';

    const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
      context: {
        title: `Delete feature`,
        subtitle: `Are you sure you want to delete the feature ${feature.name}`,
        modalStatus: 'danger',
        actions: [
          { actionName: cancelAction, buttonStatus: 'basic' },
          { actionName: confirmAction, buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result?.toLowerCase() === confirmAction.toLowerCase()) {
        this.deleteFeature(feature);
      }
    });
  }

  deleteFeature(feature: Feature): void {
    this.botService
      .deleteFeature(this.state.currentApplication.name, feature.category, feature.name, feature.applicationId)
      .subscribe((_) => this.onRefresh.emit(true));
  }
}
