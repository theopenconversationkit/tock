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

import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  HostListener,
  Input,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';
import {
  ClassifiedEntity,
  EntityContainer,
  EntityDefinition,
  EntityType,
  EntityWithSubEntities,
  Intent,
  Sentence
} from '../../../../model/nlp';
import { StateService } from '../../../../core-nlp/state.service';
import { deepCopy, getContrastYIQ } from '../../../utils';
import { Token } from './token-view/token.model';
import { Subject, takeUntil } from 'rxjs';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { SentenceTrainingCreateEntityComponent } from './sentence-training-create-entity/sentence-training-create-entity.component';
import { NlpService } from '../../../../core-nlp/nlp.service';
import { SentenceTrainingService } from '../sentence-training.service';

interface ClassifiedEntityWithIndexes {
  entity: ClassifiedEntity;
  start: number;
  end: number;
}

interface Selection {
  start: number;
  end: number;
}

@Component({
  selector: 'tock-sentence-training-sentence',
  templateUrl: './sentence-training-sentence.component.html',
  styleUrls: ['./sentence-training-sentence.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SentenceTrainingSentenceComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  @Input() sentence: Sentence;
  @Input() prefix: string = 's';
  @Input() readOnly: boolean = false;
  @Input() fontsize: string = '';

  @ViewChild('tokensContainer') tokensContainer: ElementRef;

  tokens: Token[];

  selection: Selection;

  getContrastYIQ = getContrastYIQ;

  constructor(
    public state: StateService,
    private sentenceTrainingService: SentenceTrainingService,
    private cd: ChangeDetectorRef,
    private self: ElementRef,
    private nbDialogService: NbDialogService,
    private nlp: NlpService,
    private toastrService: NbToastrService
  ) {
    this.sentenceTrainingService.communication.pipe(takeUntil(this.destroy)).subscribe((evt) => {
      if (evt.type === 'documentClick') {
        if (this.selection && !this.self.nativeElement.contains(evt.event.target)) {
          this.selection = undefined;
          this.cd.markForCheck();
        }
      }
    });
  }

  ngOnInit(): void {
    this.initTokens();
  }

  initTokens(): void {
    this.tokens = this.parseTokens(this.sentence);
    this.cd.markForCheck();
  }

  parseTokens(sentence: EntityContainer): Token[] {
    const text: String = sentence.getText();
    let entities: ClassifiedEntity[] = sentence.getEntities();

    let i = 0;
    let entityIndex = 0;
    const result: Token[] = [];

    while (i <= text.length) {
      if (entities.length > entityIndex) {
        const entity = entities[entityIndex] as ClassifiedEntity;
        if (entity.start !== i) {
          result.push(new Token(i, text.substring(i, entity.start), sentence));
        }

        const token = new Token(entity.start, text.substring(entity.start, entity.end), sentence, entity);
        if (token.entity?.subEntities?.length) {
          token.subTokens = this.parseTokens(
            new EntityWithSubEntities(
              sentence.getText().substring(token.start, token.end),
              token.entity,
              sentence.rootEntity() ? sentence.rootEntity() : token.entity
            )
          );
        }
        result.push(token);

        i = entity.end;
        entityIndex++;
      } else {
        if (i != text.length) {
          result.push(new Token(i, text.substring(i, text.length), sentence));
        }
        break;
      }
    }

    return result;
  }

  deleteTokenEntity(token: Token): void {
    token.sentence.removeEntity(token.entity);
    this.initTokens();
  }

  @HostListener('mouseup', ['$event'])
  onMouseUp(event: MouseEvent): void {
    // readOnly  => Do nothing
    if (this.readOnly) return;

    // The current sentence is associated with intent unknown or ragExcluded. It is not allowed to assign entities to the unkown or ragExcluded intents => Do nothing
    if ([Intent.unknown, Intent.ragExcluded].includes(this.sentence.classification.intentId)) return;

    // User clicked on an entity to assign => Do nothing
    if ((event.target as HTMLElement).classList.contains('token-selector')) return;

    event.stopPropagation();

    const selection = window.getSelection();

    if (selection.rangeCount > 0) {
      const range = selection.getRangeAt(0);
      if (range.startContainer !== range.endContainer || range.startOffset === range.endOffset) {
        // empty selection or selection overlapping two entities => remove selection and exit
        selection.removeAllRanges();
        this.selection = undefined;
        return;
      }

      setTimeout(() => {
        // If a selection is valid, hide all other tokens delete menu
        this.sentenceTrainingService.documentClick(event);
      });

      const tokenTxt = range.startContainer.textContent;
      const selectedTxt = tokenTxt.substring(range.startOffset, range.endOffset);

      // Remove leading space
      for (let i = 0; i < selectedTxt.length; i++) {
        if (!selectedTxt[i].trim().length) {
          const newrange = document.createRange();
          newrange.setStart(range.startContainer, range.startOffset + 1);
          newrange.setEnd(range.startContainer, range.endOffset);
          selection.removeAllRanges();
          selection.addRange(newrange);
          this.onMouseUp(event);
          return;
        } else break;
      }

      // Remove trailing space
      for (let i = selectedTxt.length - 1; i > 0; i--) {
        if (!selectedTxt[i].trim().length) {
          const newrange = document.createRange();
          newrange.setStart(range.startContainer, range.startOffset);
          newrange.setEnd(range.startContainer, range.endOffset - 1);
          selection.removeAllRanges();
          selection.addRange(newrange);
          this.onMouseUp(event);
          return;
        } else break;
      }

      // Get the range offset relative to the whole sentence
      const rangeOffset = this.getRangeOffset(range);

      this.selection = {
        start: rangeOffset,
        end: rangeOffset + (range.endOffset - range.startOffset)
      };
    }
  }

  getRangeOffset(range: Range): number {
    const clonedRange = range.cloneRange();
    clonedRange.selectNodeContents(this.tokensContainer.nativeElement);
    clonedRange.setEnd(range.startContainer, range.startOffset);
    return clonedRange.toString().length;
  }

  getSelectionText(): string {
    return this.sentence.getText().substring(this.selection.start, this.selection.end);
  }

  getAssignableEntities(): EntityDefinition[] {
    const parentEntity = this.getParentEntity(this.sentence.classification.entities, this.selection.start, this.selection.end);

    if (!parentEntity) {
      const intent = this.state.currentApplication.intentById(this.sentence.classification.intentId);
      return intent?.entities ? intent.entities : [];
    } else {
      const entityType = this.state.findEntityTypeByName(parentEntity.entity.type);
      return entityType?.subEntities;
    }
  }

  assignEntity(entity: EntityDefinition, selection?: Selection): void {
    selection = selection || this.selection;

    const parentEntity = this.getParentEntity(this.sentence.classification.entities, selection.start, selection.end);

    if (!parentEntity) {
      const newentity = new ClassifiedEntity(entity.entityTypeName, entity.role, selection.start, selection.end, []);
      this.sentence.addEntity(newentity);
    } else {
      const newentity = new ClassifiedEntity(entity.entityTypeName, entity.role, parentEntity.start, parentEntity.end, []);
      parentEntity.entity.subEntities.push(newentity);
      parentEntity.entity.subEntities.sort((e1, e2) => e1.start - e2.start);
    }

    this.selection = undefined;
    this.initTokens();
  }

  getParentEntity(entities: ClassifiedEntity[], start: number, end: number): ClassifiedEntityWithIndexes {
    for (let i = 0; i < entities.length; i++) {
      const entity = entities[i];
      if (entity.start <= start && entity.end >= end) {
        return this.getParentSubentity(entity, start - entity.start, end - entity.start);
      }
    }

    return;
  }

  getParentSubentity(entity: ClassifiedEntity, start: number, end: number): ClassifiedEntityWithIndexes {
    for (let i = 0; i < entity.subEntities.length; i++) {
      const subentity = entity.subEntities[i];
      if (subentity.start <= start && subentity.end >= end) {
        return this.getParentSubentity(subentity, start - subentity.start, end - subentity.start);
      }
    }

    return { entity, start, end };
  }

  createEntity(): void {
    let selectionCopy: Selection = deepCopy(this.selection);
    let intentOrEntityType;

    const parentEntity: ClassifiedEntityWithIndexes = this.getParentEntity(
      this.sentence.classification.entities,
      selectionCopy.start,
      selectionCopy.end
    );

    if (!parentEntity) {
      intentOrEntityType = this.state.currentApplication.intentById(this.sentence.classification.intentId);
    } else {
      intentOrEntityType = this.state.findEntityTypeByName(parentEntity.entity.type);
    }

    const dialogRef = this.nbDialogService.open(SentenceTrainingCreateEntityComponent, {
      context: {
        intentOrEntityType: intentOrEntityType
      }
    });

    dialogRef.onClose.subscribe((result) => {
      if (result) {
        const name = result.name;
        const role = result.role;
        const existingEntityType = this.state.findEntityTypeByName(name);

        if (existingEntityType) {
          const entity = new EntityDefinition(name, role);
          if (!parentEntity) {
            this.addEntityToIntent(intentOrEntityType, entity, selectionCopy);
          } else {
            this.addEntityToEntityType(intentOrEntityType, entity, parentEntity, selectionCopy);
          }
        } else {
          this.nlp.createEntityType(name).subscribe((e) => {
            if (e) {
              const entity = new EntityDefinition(e.name, role);
              const entities = this.state.entityTypes.getValue().slice(0);
              entities.push(e);
              this.state.entityTypes.next(entities);

              if (!parentEntity) {
                this.addEntityToIntent(intentOrEntityType, entity, selectionCopy);
              } else {
                this.addEntityToEntityType(intentOrEntityType, entity, parentEntity, selectionCopy);
              }
            } else {
              this.toastrService.danger(`Error when creating Entity Type ${name}`, 'Error');
            }
          });
        }
      }
    });
  }

  addEntityToIntent(intent: Intent, entity: EntityDefinition, selection: Selection): void {
    intent.addEntity(entity);

    const allEntities = this.state.entities.getValue();
    if (!allEntities.some((e) => e.entityTypeName === entity.entityTypeName && e.role === entity.role)) {
      this.state.entities.next(this.state.currentApplication.allEntities());
    }

    this.nlp.saveIntent(intent).subscribe((_) => {
      this.assignEntity(entity, selection);
      this.toastrService.success(`Entity Type ${entity.qualifiedRole} added to this sentence intent`, 'Entity added');
    });
  }

  addEntityToEntityType(
    entityType: EntityType,
    entity: EntityDefinition,
    parentEntity: ClassifiedEntityWithIndexes,
    selection: Selection
  ): void {
    const entityWithSubEntities = new EntityWithSubEntities(
      this.sentence.getText().substring(selection.start, selection.end),
      parentEntity.entity,
      this.sentence.rootEntity() ? this.sentence.rootEntity() : parentEntity.entity
    );

    if (
      entityWithSubEntities.root.containsEntityType(entity.entityTypeName) ||
      this.containsEntityType(this.state.findEntityTypeByName(entity.entityTypeName), entityWithSubEntities.root.type)
    ) {
      this.toastrService.warning('adding recursive sub entity is not allowed', 'Operation not allowed');
      return;
    }

    entityType.addEntity(entity);

    this.nlp.updateEntityType(entityType).subscribe((_) => {
      this.assignEntity(entity, selection);
      this.toastrService.success(`Entity Type ${entity.qualifiedRole} added to this sentence intent`, 'Entity added');
    });
  }

  containsEntityType(entityType: EntityType, entityTypeName: string, entityTypes: Set<string> = new Set()): boolean {
    if (entityTypeName === entityType.name) {
      return true;
    }

    entityTypes.add(entityType.name);

    return (
      entityType.subEntities
        .filter((e) => !entityTypes.has(e.entityTypeName))
        .find((e) => this.containsEntityType(this.state.findEntityTypeByName(e.entityTypeName), entityTypeName, entityTypes)) !== undefined
    );
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
