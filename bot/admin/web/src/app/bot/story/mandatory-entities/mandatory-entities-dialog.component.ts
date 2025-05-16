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

import { Component, OnInit } from '@angular/core';
import { NbToastrService, NbDialogService, NbDialogRef } from '@nebular/theme';
import { take } from 'rxjs';

import { StateService } from '../../../core-nlp/state.service';
import { AnswerConfigurationType, IntentName, MandatoryEntity, SimpleAnswerConfiguration } from '../../model/story';
import { EntityDefinition, Intent, IntentsCategory } from '../../../model/nlp';
import { IntentDialogComponent } from '../../../language-understanding/intent-dialog/intent-dialog.component';
import { CreateEntityDialogComponent } from '../create-entity-dialog/create-entity-dialog.component';

@Component({
  selector: 'tock-mandatory-entities-dialog',
  templateUrl: './mandatory-entities-dialog.component.html',
  styleUrls: ['./mandatory-entities-dialog.component.scss']
})
export class MandatoryEntitiesDialogComponent implements OnInit {
  entities: MandatoryEntity[];
  defaultCategory: string;

  newEntity: MandatoryEntity;
  intentCategories: IntentsCategory[] = [];
  currentIntentCategories: IntentsCategory[] = [];
  currentEditedIntent: string;

  constructor(
    public stateService: StateService,
    private nbToastrService: NbToastrService,
    private nbDialogService: NbDialogService,
    private nbDialogRef: NbDialogRef<MandatoryEntitiesDialogComponent>
  ) {}

  ngOnInit() {
    this.stateService.currentIntentsCategories.pipe(take(1)).subscribe((c) => {
      this.intentCategories = c;
      this.currentIntentCategories = c;
    });

    this.entities = this.entities
      ? this.entities.slice(0).map((a) => {
          const newA = a.clone();
          newA.intentDefinition = this.stateService.findIntentByName(a.intent.name);
          return newA;
        })
      : [];

    this.stateService.entities.pipe(take(1)).subscribe((allEntities) => {
      this.entities = this.entities.map((e) => {
        e.entity = allEntities.find((a) => a.role === e.role) ?? allEntities.find((a) => a.entityTypeName === e.entityType);
        return e;
      });
    });

    this.setNewEntity();

    this.newEntity.category = this.defaultCategory;
  }

  private setNewEntity() {
    const c = this.newEntity ? this.newEntity.category : null;
    this.newEntity = new MandatoryEntity(
      '',
      '',
      new IntentName(''),
      [new SimpleAnswerConfiguration([])],
      AnswerConfigurationType.simple,
      c ? c : ''
    );
  }

  onIntentChange(entity: MandatoryEntity, name: string) {
    if (this.currentEditedIntent !== name) {
      this.currentEditedIntent = name;
      const intent = name.trim().toLowerCase();
      let target = this.intentCategories
        .map(
          (c) =>
            new IntentsCategory(
              c.category,
              c.intents.filter(
                (i) => i.intentLabel().toLowerCase().startsWith(intent) && (!entity.role || i.entities.find((e) => e.role === entity.role))
              )
            )
        )
        .filter((c) => c.intents.length !== 0);
      if (target.length === 0) {
        target = this.intentCategories
          .map(
            (c) =>
              new IntentsCategory(
                c.category,
                c.intents.filter((i) => i.intentLabel().toLowerCase().startsWith(intent))
              )
          )
          .filter((c) => c.intents.length !== 0);
      }

      this.currentIntentCategories = target;
    }
  }

  validateIntent(entity: MandatoryEntity) {
    setTimeout((_) => {
      const intentName = entity.intent.name.trim();
      if (intentName.length !== 0 && (!entity.intentDefinition || entity.intentDefinition.name !== intentName)) {
        const intent = this.stateService.findIntentByName(intentName);
        if (intent) {
          entity.intentDefinition = intent;
        } else {
          const dialogRef = this.nbDialogService.open(IntentDialogComponent, {
            context: {
              create: true,
              category: this.defaultCategory,
              name: intentName,
              label: intentName
            }
          });
          dialogRef.onClose.subscribe((result) => {
            if (result.name) {
              entity.intentDefinition = new Intent(
                result.name,
                this.stateService.currentApplication.namespace,
                [],
                [this.stateService.currentApplication._id],
                [],
                [],
                result.label,
                result.description,
                result.category
              );
            } else {
              entity.intent.name = entity.intentDefinition ? entity.intentDefinition.name : '';
            }
          });
        }
      }
    }, 200);
  }

  selectEntity(e: MandatoryEntity) {
    const dialogRef = this.nbDialogService.open(CreateEntityDialogComponent, {
      context: {}
    });
    dialogRef.onClose.subscribe((result) => {
      if (result && result !== 'cancel') {
        const name = result.name;
        const role = result.role;
        e.entity = new EntityDefinition(name, role);
        e.role = role;
        e.entityType = name;
      }
    });
  }

  removeEntity(e: MandatoryEntity) {
    this.entities.splice(this.entities.indexOf(e), 1);
  }

  addEntity() {
    const invalidMessage = this.newEntity.currentAnswer().invalidMessage();
    if (invalidMessage) {
      this.nbToastrService.show(`Error: ${invalidMessage}`, 'ERROR', { duration: 5000 });
    } else {
      if (this.newEntity.entityType.length === 0) {
        this.nbToastrService.show(`Error: Please select an entity`, 'ERROR', { duration: 5000 });
      } else {
        this.entities.push(this.newEntity);
        this.setNewEntity();
      }
    }
  }

  cancel() {
    this.nbDialogRef.close({});
  }

  save() {
    if (this.newEntity.currentAnswer().invalidMessage() === null && this.newEntity.entityType.length !== 0) {
      this.entities.push(this.newEntity);
    }
    this.nbDialogRef.close({ entities: this.entities });
  }
}
