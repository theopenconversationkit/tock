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

import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Subject } from 'rxjs';
import { NbDialogService } from '@nebular/theme';
import { UserRole } from '../../../model/auth';
import { EntityDefinition, Intent } from '../../../model/nlp';
import { getContrastYIQ } from '../../../shared/utils';
import { StateService } from '../../../core-nlp/state.service';
import { IntentStoryDetailsComponent } from '../../../shared/components';

@Component({
  selector: 'tock-intents-list',
  templateUrl: './intents-list.component.html',
  styleUrls: ['./intents-list.component.scss']
})
export class IntentsListComponent implements OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  UserRole = UserRole;

  @Input() intents: Intent[];

  @Output() onRemoveEntity = new EventEmitter();
  @Output() onRemoveSharedIntent = new EventEmitter();
  @Output() onDisplayAddSharedIntentDialog = new EventEmitter();
  @Output() onRemoveState = new EventEmitter();
  @Output() onAddState = new EventEmitter();
  @Output() onUpdateIntent = new EventEmitter();
  @Output() onDownloadSentencesDump = new EventEmitter();
  @Output() onDeleteIntent = new EventEmitter();

  getContrastYIQ = getContrastYIQ;

  constructor(public state: StateService, private nbDialogService: NbDialogService) {}

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  removeEntity(intent: Intent, entity: EntityDefinition): void {
    this.onRemoveEntity.emit({ intent, entity });
  }

  removeSharedIntent(intent: Intent, intentId: string): void {
    this.onRemoveSharedIntent.emit({ intent, intentId });
  }

  displayAddSharedIntentDialog(intent: Intent): void {
    this.onDisplayAddSharedIntentDialog.emit(intent);
  }

  removeState(intent: Intent, state: string): void {
    this.onRemoveState.emit({ intent, state });
  }

  addState(intent: Intent): void {
    this.onAddState.emit(intent);
  }

  updateIntent(intent: Intent): void {
    this.onUpdateIntent.emit(intent);
  }

  downloadSentencesDump(intent: Intent): void {
    this.onDownloadSentencesDump.emit(intent);
  }

  deleteIntent(intent: Intent): void {
    this.onDeleteIntent.emit(intent);
  }

  displayIntentStoryDetails(intent: Intent) {
    const modal = this.nbDialogService.open(IntentStoryDetailsComponent, {
      context: {
        intentId: intent._id
      }
    });
  }
}
