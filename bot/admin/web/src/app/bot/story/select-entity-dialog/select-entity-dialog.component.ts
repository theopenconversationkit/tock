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

import { Component, Input, OnInit } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { take } from 'rxjs';

import { EntityType, IntentsCategory } from '../../../model/nlp';
import { StateService } from '../../../core-nlp/state.service';
import { IntentName } from '../../model/story';
import { NlpService } from '../../../core-nlp/nlp.service';

@Component({
  selector: 'tock-select-entity-dialog',
  templateUrl: './select-entity-dialog.component.html',
  styleUrls: ['./select-entity-dialog.component.scss']
})
export class SelectEntityDialogComponent implements OnInit {
  @Input() generate: boolean;
  @Input() selectedEntity: EntityType;
  @Input() role?: string;
  @Input() entityValue?: string;

  alreadySelectedEntity: string;
  entities: EntityType[] = [];
  intent: IntentName = new IntentName('');
  intentCategories: IntentsCategory[] = [];
  currentIntentCategories: IntentsCategory[] = [];
  currentEditedIntent: string;
  entityValues: string[] = [];

  constructor(
    private nbDialogRef: NbDialogRef<SelectEntityDialogComponent>,
    private nlpService: NlpService,
    public stateService: StateService
  ) {}

  ngOnInit() {
    this.stateService
      .entityTypesSortedByName()
      .pipe(take(1))
      .subscribe((e) => {
        this.entities = this.generate ? e.filter((entity) => entity.dictionary) : e;
        if (this.alreadySelectedEntity) {
          this.selectedEntity = e.find((en) => en.name == this.alreadySelectedEntity);
        }
        this.calculateEntityValues(this.selectedEntity);
      });
    this.stateService.currentIntentsCategories.pipe(take(1)).subscribe((c) => {
      this.intentCategories = c;
      this.currentIntentCategories = c;
    });
  }

  onIntentChange(name: string) {
    if (this.currentEditedIntent !== name) {
      this.currentEditedIntent = name;
      const intent = name.trim().toLowerCase();
      let target = this.intentCategories
        .map(
          (c) =>
            new IntentsCategory(
              c.category,
              c.intents.filter((i) => i.intentLabel().toLowerCase().startsWith(intent))
            )
        )
        .filter((c) => c.intents.length !== 0);

      this.currentIntentCategories = target;
    }
  }

  selectEntityType(entityType: EntityType) {
    this.selectedEntity = entityType;
    this.entityValue = null;
    this.role = entityType.simpleName();
    this.calculateEntityValues(entityType);
  }

  selectEntityValue(value: string) {
    if (this.entityValue === value) {
      this.entityValue = null;
    } else {
      this.entityValue = value;
    }
  }

  private calculateEntityValues(entityType: EntityType) {
    if (entityType && !this.generate) {
      this.nlpService
        .getDictionary(entityType)
        .pipe(take(1))
        .subscribe((dictionary) => {
          this.entityValues = dictionary.values.map((v) => v.value);
        });
    }
  }

  cancel() {
    this.nbDialogRef.close({});
  }

  validateEntity() {
    this.nbDialogRef.close({
      intent: this.intent,
      entity: this.selectedEntity,
      role: this.role
    });
  }

  validateEntityValue() {
    this.nbDialogRef.close({
      entity: this.selectedEntity,
      role: this.role,
      value: this.entityValue
    });
  }

  removeEntityValue() {
    this.nbDialogRef.close({
      entity: this.selectedEntity
    });
  }
}
