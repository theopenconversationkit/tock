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

import { saveAs } from 'file-saver';
import { Component, ElementRef, OnInit } from '@angular/core';
import { StateService } from '../core-nlp/state.service';
import { EntityDefinition, Intent, IntentsCategory } from '../model/nlp';
import { ConfirmDialogComponent } from '../shared-nlp/confirm-dialog/confirm-dialog.component';
import { NlpService } from '../nlp-tabs/nlp.service';
import { ApplicationService } from '../core-nlp/applications.service';
import { AddStateDialogComponent } from './add-state/add-state-dialog.component';
import { UserRole } from '../model/auth';
import { IntentDialogComponent } from '../sentence-analysis/intent-dialog/intent-dialog.component';
import { BehaviorSubject } from 'rxjs';
import { DialogService } from '../core-nlp/dialog.service';
import { AddSharedIntentDialogComponent } from './add-shared-intent/add-shared-intent-dialog.component';

interface TreeNode<T> {
  data: T;
  children?: TreeNode<T>[];
  expanded?: boolean;
}

@Component({
  selector: 'tock-intents',
  templateUrl: './intents.component.html',
  styleUrls: ['./intents.component.css']
})
export class IntentsComponent implements OnInit {
  UserRole = UserRole;
  intentsCategories: BehaviorSubject<IntentsCategory[]> = new BehaviorSubject([]);
  expandedCategories: Set<string> = new Set(['default']);
  selectedIntent: Intent;

  intentColumn = 'Intent';
  descriptionColumn = 'Description';
  entitiesColumn = 'Entities';
  sharedIntentsColumn = 'Shared Intents';
  mandatoryStatesColumn = 'Mandatory States';
  actionsColumn = 'Actions';
  allColumns = [
    this.intentColumn,
    this.descriptionColumn,
    this.entitiesColumn,
    this.sharedIntentsColumn,
    this.mandatoryStatesColumn,
    this.actionsColumn
  ];
  nodes: TreeNode<any>[];

  constructor(
    public state: StateService,
    private nlp: NlpService,
    private dialog: DialogService,
    private applicationService: ApplicationService
  ) {}

  ngOnInit() {
    this.state.currentIntentsCategories.subscribe((it) => {
      this.nodes = Array.from(it, (element) => {
        return {
          expanded: element.category === 'default',
          data: {
            category: element.category,
            expandable: true
          },
          children: element.intents.map((s) => {
            return {
              data: s
            };
          })
        };
      });
      this.intentsCategories.next(it);
    });
  }

  updateIntent(intent: Intent) {
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

  deleteIntent(intent: Intent) {
    const dialogRef = this.dialog.openDialog(ConfirmDialogComponent, {
      context: {
        title: `Remove the Intent ${intent.name}`,
        subtitle: 'Are you sure?',
        action: 'Remove'
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result === 'remove') {
        this.nlp.removeIntent(this.state.currentApplication, intent).subscribe(
          (_) => {
            this.state.removeIntent(intent);
            this.dialog.notify(`Intent ${intent.name} removed`, 'Remove Intent');
          },
          (_) => this.dialog.notify(`Delete Intent ${intent.name} failed`)
        );
      }
    });
  }

  removeState(intent: Intent, state: string) {
    this.nlp.removeState(this.state.currentApplication, intent, state).subscribe(
      (_) => {
        intent.mandatoryStates.splice(intent.mandatoryStates.indexOf(state), 1);
        this.dialog.notify(`State ${state} removed from Intent ${intent.name}`, 'Remove State');
      },
      (_) => {
        this.dialog.notify(`Remove State failed`);
      }
    );
  }

  addState(intent: Intent) {
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
            this.dialog.notify(
              `State ${response.name} added for Intent ${intent.name}`,
              'Add State'
            );
          },
          (_) => {
            intent.mandatoryStates.splice(intent.mandatoryStates.length - 1, 1);
            this.dialog.notify(`Add State failed`);
          }
        );
      }
    });
  }

  removeEntity(intent: Intent, entity: EntityDefinition) {
    const entityName = entity.qualifiedName(this.state.user);
    const dialogRef = this.dialog.openDialog(ConfirmDialogComponent, {
      context: {
        title: `Remove the Entity ${entityName}`,
        subtitle: 'Are you sure?',
        action: 'Remove'
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result === 'remove') {
        this.nlp
          .removeEntity(this.state.currentApplication, intent, entity)
          .subscribe((deleted) => {
            this.state.currentApplication.intentById(intent._id).removeEntity(entity);
            if (deleted) {
              this.state.removeEntityTypeByName(entity.entityTypeName);
            }
            this.dialog.notify(`Entity ${entityName} removed from intent`, 'Remove Entity');
          });
      }
    });
  }

  removeSharedIntent(intent: Intent, intentId: string) {
    this.selectedIntent = null;
    this.nlp.removeSharedIntent(this.state.currentApplication, intent, intentId).subscribe(
      (_) => {
        intent.sharedIntents.splice(intent.sharedIntents.indexOf(intentId), 1);
        this.dialog.notify(`Shared Intent removed from Intent ${intent.name}`, 'Remove Intent');
      },
      (_) => {
        this.dialog.notify(`Remove Shared Intent failed`);
      }
    );
  }

  displayAddSharedIntentDialog(intent: Intent) {
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

  addSharedIntent(intent: Intent, intentId: string) {
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

  downloadSentencesDump(intent: Intent) {
    this.applicationService
      .getSentencesDumpForIntent(
        this.state.currentApplication,
        intent,
        this.state.currentLocale,
        this.state.hasRole(UserRole.technicalAdmin)
      )
      .subscribe((blob) => {
        saveAs(blob, intent.name + '_sentences.json');
        this.dialog.notify(`Dump provided`, 'Dump');
      });
  }
}
