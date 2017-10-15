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

import {AfterViewInit, Component, Input, OnChanges, OnInit, SimpleChange} from "@angular/core";
import {
  ClassifiedEntity,
  EntityContainer,
  EntityDefinition,
  EntityType,
  EntityWithSubEntities,
  Sentence
} from "../../model/nlp";
import {NlpService} from "../../nlp-tabs/nlp.service";
import {Intent} from "../../model/application";
import {StateService} from "../../core/state.service";
import {MdDialog, MdDialogConfig, MdSnackBar, MdSnackBarConfig} from "@angular/material";
import {CreateEntityDialogComponent} from "../create-entity-dialog/create-entity-dialog.component";
import {User} from "../../model/auth";
import {isNullOrUndefined} from "util";

@Component({
  selector: 'tock-highlight',
  templateUrl: 'highlight.component.html',
  styleUrls: ['highlight.component.css']
})
export class HighlightComponent implements OnInit, OnChanges, AfterViewInit {

  @Input() sentence: EntityContainer;
  @Input() readOnly: boolean = false;
  @Input() fontSize: string = "inherit";
  @Input() prefix: string = "s";
  @Input() leftPadding: number = 0;

  entityProvider: EntityProvider;
  selectedStart: number;
  selectedEnd: number;
  editable: boolean;
  edited: boolean;
  tokens: Token[];

  constructor(private nlp: NlpService,
              public state: StateService,
              private snackBar: MdSnackBar,
              private dialog: MdDialog) {
    this.editable = true;
    this.edited = false;
    this.selectedStart = -1;
    this.selectedEnd = -1;
  }

  private initTokens() {
    let i = 0;
    let entityIndex = 0;
    const text = this.sentence.getText();
    const entities = this.sentence.getEntities();
    const result: Token[] = [];
    while (i <= text.length) {
      if (entities.length > entityIndex) {
        const e = entities[entityIndex];
        if (e.start !== i) {
          result.push(new Token(i, text.substring(i, e.start), result.length));
        }
        result.push(new Token(e.start, text.substring(e.start, e.end), result.length, e));
        i = e.end;
        entityIndex++;
      } else {
        if (i != text.length) {
          result.push(new Token(i, text.substring(i, text.length), result.length));
        }
        break;
      }
    }
    this.tokens = result;
  }

  ngOnInit(): void {
    if (this.sentence instanceof Sentence) {
      this.entityProvider = new IntentEntityProvider(this.nlp, this.state, this.sentence as Sentence);
    } else {
      this.entityProvider = new SubEntityProvider(this.nlp, this.state, this.sentence as EntityWithSubEntities);
    }

    this.rebuild();
  }

  ngOnChanges(changes: { [key: string]: SimpleChange }): any {
    this.rebuild();
  }

  ngAfterViewInit(): void {
  }

  private handleParentSelect() {
    if (this.sentence instanceof EntityWithSubEntities) {
      const e = this.sentence as EntityWithSubEntities;
      if (e.hasSelection()) {
        setTimeout(_ => {
          this.selectedStart = e.startSelection;
          this.selectedEnd = e.endSelection;
          const r = document.createRange();
          const tokenMatch = this.tokens.find(t => t.start <= e.startSelection && t.end >= e.endSelection);
          if (!tokenMatch) {
            return;
          }
          let token = document.getElementById(this.prefix + tokenMatch.index).firstChild;
          r.setStart(token, this.selectedStart - tokenMatch.start);
          r.setEnd(token, this.selectedEnd - tokenMatch.start);
          window.getSelection().addRange(r);
          this.select();
        });
      }
    }
  }

  select() {
    const selection = window.getSelection().getRangeAt(0);
    let start = selection.startOffset;
    let end = selection.endOffset;
    if (selection.startContainer !== selection.endContainer) {
      if (!selection.startContainer.childNodes[0]) {
        return;
      }
      end = selection.startContainer.childNodes[0].textContent.length - start;
    } else {
      if (start > end) {
        const tmp = start;
        start = end;
        end = tmp;
      }
    }
    if (start === end) {
      return;
    }
    const span = selection.startContainer.parentElement;
    this.selectedStart = -1;
    this.selectedEnd = -1;
    this.findSelected(span.parentNode, new SelectedResult(span, start, end));

    const overlap = this.sentence.overlappedEntity(this.selectedStart, this.selectedEnd);
    if (overlap) {
      this.sentence
        .addEditedSubEntities(overlap)
        .setSelection(this.selectedStart - overlap.start, this.selectedEnd - overlap.start);
      window.getSelection().removeAllRanges();
    } else if (this.entityProvider.isValid()) {
      this.edited = true;
    }
  }

  addEntity() {
    let dialogRef = this.dialog.open(CreateEntityDialogComponent,
      {
        data: {
          entityProvider: this.entityProvider
        }
      } as MdDialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result !== "cancel") {
        let name = result.name;
        let role = result.role;
        const existingEntityType = this.state.findEntityTypeByName(name);
        if (existingEntityType) {
          const entity = new EntityDefinition(name, role);
          this.entityProvider.addEntity(entity, this);
        } else {
          this.nlp.createEntityType(name).subscribe(e => {
            if (e) {
              const entity = new EntityDefinition(e.name, role);
              const entities = this.state.entityTypes.getValue().slice(0);
              entities.push(e);
              this.state.entityTypes.next(entities);
              this.entityProvider.addEntity(entity, this);
            } else {
              this.snackBar.open(`Error when creating Entity Type ${name}`, "Error", {duration: 1000} as MdSnackBarConfig);
            }
          });
        }
      }
    });
  }

  notifyAddEntity(entity: EntityDefinition) {
    this.onSelect(entity);
    this.snackBar.open(`Entity Type ${entity.qualifiedRole} added`, "Entity added", {duration: 1000} as MdSnackBarConfig)

  }

  private rebuild() {
    this.edited = false;
    if (this.entityProvider) {
      this.entityProvider.reload();
    }
    this.initTokens();
    this.handleParentSelect();
  }

  onClose() {
    this.edited = false;
    window.getSelection().removeAllRanges();
  }

  onSelect(entity: EntityDefinition) {
    if (this.selectedStart < this.selectedEnd) {
      this.edited = false;
      const e = new ClassifiedEntity(entity.entityTypeName, entity.role, this.selectedStart, this.selectedEnd, []);
      this.sentence.addEntity(e);
      this.initTokens();
    }
  }

  private findSelected(node, result) {
    if (this.selectedStart == -1) {
      if (node.nodeType === 3) {
        const content = node.textContent;
        result.alreadyCount += content.length;
      } else {
        for (const child of node.childNodes) {
          if (node.nodeType === 1) {
            if (node === result.selectedNode) {
              const textNode = node.childNodes[0];
              const content = textNode.textContent;
              this.selectedStart = result.alreadyCount + result.startOffset;
              this.selectedEnd = this.selectedStart + result.endOffset - result.startOffset;
            } else {
              this.findSelected(child, result);
            }
          }
        }
      }
    }
  }

}

export class SelectedResult {

  alreadyCount: number;

  constructor(public selectedNode: any, public startOffset: Number, public endOffset) {
    this.alreadyCount = 0;
  }
}

export class Token {

  public end: number;

  constructor(public start: number, public text: string, public index: number, public entity?: ClassifiedEntity) {
    this.end = this.start + text.length;
  }

  display(sentence: Sentence, user: User): string {
    if (!this.entity) {
      return "";
    } else {
      return this.entity.qualifiedName(user) + " = " + sentence.entityValue(this.entity);
    }
  }

  color(): string {
    if (this.entity) {
      return this.entity.entityColor;
    } else {
      return "";
    }
  }
}

export interface EntityProvider {

  reload()

  getEntities(): EntityDefinition[]

  isValid(): boolean

  hasEntityRole(role: string): boolean

  addEntity(entity: EntityDefinition, highlight: HighlightComponent)

}

export class IntentEntityProvider implements EntityProvider {

  constructor(private nlp: NlpService,
              private state: StateService,
              private sentence: Sentence,
              private intent?: Intent) {
  }


  addEntity(entity: EntityDefinition, highlight: HighlightComponent) {
    this.intent.addEntity(entity);
    this.nlp.saveIntent(this.intent).subscribe(_ => {
        highlight.notifyAddEntity(entity)
      }
    );
  }

  hasEntityRole(role: string): boolean {
    return this.intent.containsEntityRole(role);
  }

  isValid(): boolean {
    return this.intent && !this.intent.isUnknownIntent();
  }

  reload() {
    if (this.sentence && this.sentence.classification) {
      this.intent = this.state.currentApplication.intentById(this.sentence.classification.intentId);
    }
  }

  getEntities(): EntityDefinition[] {
    if (this.intent) {
      return this.intent.entities;
    } else {
      return [];
    }
  }
}

export class SubEntityProvider implements EntityProvider {

  constructor(private nlp: NlpService,
              private state: StateService,
              private entity: EntityWithSubEntities,
              private entityType?: EntityType,) {
  }

  addEntity(entity: EntityDefinition, highlight: HighlightComponent) {
    this.entityType.addEntity(entity);
    this.nlp.updateEntityType(this.entityType).subscribe(_ => {
        highlight.notifyAddEntity(entity)
      }
    );
  }

  hasEntityRole(role: string): boolean {
    return this.entityType.containsEntityRole(role);
  }

  isValid(): boolean {
    return !isNullOrUndefined(this.entityType);
  }

  reload() {
    if (this.entity) {
      this.entityType = this.state.findEntityTypeByName(this.entity.type);
    }
  }

  getEntities(): EntityDefinition[] {
    if (this.entityType) {
      return this.entityType.subEntities;
    } else {
      return [];
    }
  }
}



