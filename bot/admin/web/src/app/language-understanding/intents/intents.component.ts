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

import { saveAs } from 'file-saver-es';
import { Component, OnInit } from '@angular/core';
import { AddStateDialogComponent } from './add-state/add-state-dialog.component';
import { AddSharedIntentDialogComponent } from './add-shared-intent/add-shared-intent-dialog.component';
import { IntentsFilter } from './intents-filters/intents-filters.component';
import { StateService } from '../../core-nlp/state.service';
import { UserRole } from '../../model/auth';
import { EntityDefinition, Intent, IntentsCategory } from '../../model/nlp';
import { NlpService } from '../../core-nlp/nlp.service';
import { DialogService } from '../../core-nlp/dialog.service';
import { ApplicationService } from '../../core-nlp/applications.service';
import { IntentDialogComponent } from '../intent-dialog/intent-dialog.component';
import { ChoiceDialogComponent } from '../../shared/components';
import { getExportFileName } from '../../shared/utils';

@Component({
  selector: 'tock-intents',
  templateUrl: './intents.component.html',
  styleUrls: ['./intents.component.scss']
})
export class IntentsComponent implements OnInit {
  UserRole = UserRole;
  selectedIntent: Intent;

  intentsCategories: IntentsCategory[];

  constructor(
    public state: StateService,
    private nlp: NlpService,
    private dialog: DialogService,
    private applicationService: ApplicationService
  ) {}

  ngOnInit() {
    this.state.currentNamespaceIntentsCategories.subscribe((it) => {
      this.intentsCategories = it;
    });
  }

  filters: IntentsFilter;

  filteredIntents: Intent[];

  filterIntents(filters: IntentsFilter) {
    this.filters = filters;
    this.updateFilteredIntents();
  }

  updateFilteredIntents(): void {
    if (this.filters?.search?.trim().length) {
      let allIntents = [];
      this.intentsCategories.forEach((cat) => {
        allIntents = [...allIntents, ...cat.intents];
      });
      const searchStr = this.filters.search.toLowerCase();
      this.filteredIntents = allIntents.filter((intent) => {
        return intent.label?.toLowerCase().search(searchStr) > -1 || intent.name?.toLowerCase().search(searchStr) > -1;
      });
    } else {
      this.filteredIntents = undefined;
    }
  }

  updateIntent(intent: Intent): void {
    const dialogRef = this.dialog.openDialog(IntentDialogComponent, {
      context: {
        name: intent.name,
        label: intent.label,
        description: intent.description,
        category: intent.category
      }
    });

    dialogRef.onClose.subscribe((result) => {
      if (result?.name) {
        this.nlp
          .saveIntent(
            new Intent(
              intent.name,
              this.state.user.organization,
              [],
              [this.state.currentApplication._id],
              [],
              [],
              result.label,
              result.description,
              result.category,
              intent._id
            )
          )
          .subscribe((_intent) => {
            this.state.updateIntent(_intent);
          });
      }
    });
  }

  deleteIntent(intent: Intent): void {
    const action = 'remove';
    const dialogRef = this.dialog.openDialog(ChoiceDialogComponent, {
      context: {
        title: `Remove the Intent ${intent.name}`,
        subtitle: 'Are you sure?',
        actions: [
          { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
          { actionName: action, buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result === action) {
        this.nlp.removeIntent(this.state.currentApplication, intent).subscribe(
          (_) => {
            this.state.removeIntent(intent);
            this.dialog.notify(`Intent ${intent.name} removed`, 'Remove Intent');
            this.updateFilteredIntents();
          },
          (_) => this.dialog.notify(`Delete Intent ${intent.name} failed`)
        );
      }
    });
  }

  removeState(event: { intent: Intent; state: string }): void {
    this.nlp.removeState(this.state.currentApplication, event.intent, event.state).subscribe(
      (_) => {
        event.intent.mandatoryStates.splice(event.intent.mandatoryStates.indexOf(event.state), 1);
        this.dialog.notify(`State ${event.state} removed from Intent ${event.intent.name}`, 'Remove State');
      },
      (_) => {
        this.dialog.notify(`Remove State failed`);
      }
    );
  }

  addState(intent: Intent): void {
    const dialogRef = this.dialog.openDialog(AddStateDialogComponent, {
      context: {
        title: `Add a state for intent \"${intent.name}\"`
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result && result !== 'cancel') {
        intent.mandatoryStates.push(result.name);
        this.nlp.saveIntent(intent).subscribe(
          (response) => {
            this.dialog.notify(`State ${response.name} added for Intent ${intent.name}`, 'Add State');
          },
          (_) => {
            intent.mandatoryStates.splice(intent.mandatoryStates.length - 1, 1);
            this.dialog.notify(`Add State failed`);
          }
        );
      }
    });
  }

  removeEntity(event: { intent: Intent; entity: EntityDefinition }): void {
    const entityName = event.entity.qualifiedName(this.state.user);
    const action = 'remove';
    const dialogRef = this.dialog.openDialog(ChoiceDialogComponent, {
      context: {
        title: `Remove the Entity ${entityName}`,
        subtitle: 'Are you sure?',
        actions: [
          { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
          { actionName: action, buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result === action) {
        this.nlp.removeEntity(this.state.currentApplication, event.intent, event.entity).subscribe((deleted) => {
          this.state.currentApplication.intentById(event.intent._id).removeEntity(event.entity);
          if (deleted) {
            this.state.removeEntityTypeByName(event.entity.entityTypeName);
          }
          this.dialog.notify(`Entity ${entityName} removed from intent`, 'Remove Entity');
        });
      }
    });
  }

  removeSharedIntent(event: { intent: Intent; intentId: string }): void {
    this.selectedIntent = null;
    this.nlp.removeSharedIntent(this.state.currentApplication, event.intent, event.intentId).subscribe(
      (_) => {
        event.intent.sharedIntents.splice(event.intent.sharedIntents.indexOf(event.intentId), 1);
        this.dialog.notify(`Shared Intent removed from Intent ${event.intent.name}`, 'Remove Intent');
      },
      (_) => {
        this.dialog.notify(`Remove Shared Intent failed`);
      }
    );
  }

  displayAddSharedIntentDialog(intent: Intent): void {
    this.selectedIntent = intent;
    const dialogRef = this.dialog.openDialog(AddSharedIntentDialogComponent, {
      context: {
        title: `Add a shared intent to the intent \"${intent.name}\"`
      }
    });

    dialogRef.onClose.subscribe((result) => {
      if (result && result !== 'cancel') {
        this.addSharedIntent(this.selectedIntent, result.intent);
      } else {
        this.selectedIntent = null;
      }
    });
  }

  addSharedIntent(intent: Intent, intentId: string): void {
    if (intent.sharedIntents.indexOf(intentId) === -1) {
      this.selectedIntent = null;
      intent.sharedIntents.push(intentId);
      this.nlp.saveIntent(intent).subscribe(
        (_) => {
          this.dialog.notify(`Shared intent added for Intent ${intent.name}`, 'Add Shared Intent');
        },
        (_) => {
          intent.mandatoryStates.splice(intent.mandatoryStates.length - 1, 1);
          this.dialog.notify(`Add Shared Intent failed`);
        }
      );
    }
  }

  downloadSentencesDump(intent: Intent): void {
    this.applicationService
      .getSentencesDumpForIntent(
        this.state.currentApplication,
        intent,
        this.state.currentLocale,
        this.state.hasRole(UserRole.technicalAdmin)
      )
      .subscribe((blob) => {
        const exportFileName = getExportFileName(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          'Sentences',
          'json',
          intent.name
        );
        saveAs(blob, exportFileName);
        this.dialog.notify(`Dump provided`, 'Dump');
      });
  }

  expandedCategory: string = 'default';

  isCategoryExpanded(category: IntentsCategory): boolean {
    return category.category.toLowerCase() === this.expandedCategory.toLowerCase();
  }

  collapsedChange(category: IntentsCategory): void {
    this.expandedCategory = category.category;
  }
}
