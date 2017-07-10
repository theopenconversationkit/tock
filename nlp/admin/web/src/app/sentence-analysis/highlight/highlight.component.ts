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

import {Component, Input, OnChanges, OnInit, SimpleChange} from "@angular/core";
import {ClassifiedEntity, EntityDefinition, Sentence} from "../../model/nlp";
import {NlpService} from "../../nlp-tabs/nlp.service";
import {Intent} from "../../model/application";
import {StateService} from "../../core/state.service";
import {MdDialog, MdSnackBar} from "@angular/material";
import {CreateEntityDialogComponent} from "../create-entity-dialog/create-entity-dialog.component";
import {User} from "../../model/auth";

@Component({
  selector: 'tock-highlight',
  templateUrl: 'highlight.component.html',
  styleUrls: ['highlight.component.css']
})
export class HighlightComponent implements OnInit, OnChanges {

  @Input() sentence: Sentence;
  @Input() readOnly: boolean = false;

  intent: Intent;
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
    const text = this.sentence.text;
    const entities = this.sentence.classification.entities;
    const result: Token[] = [];
    while (i <= text.length) {
      if (entities.length > entityIndex) {
        const e = entities[entityIndex];
        if (e.start !== i) {
          result.push(new Token(text.substring(i, e.start), result.length));
        }
        result.push(new Token(text.substring(e.start, e.end), result.length, e));
        i = e.end;
        entityIndex++;
      } else {
        if (i != text.length) {
          result.push(new Token(text.substring(i, text.length), result.length));
        }
        break;
      }
    }
    this.tokens = result;
  }


  ngOnInit(): void {
    this.rebuild();
  }

  ngOnChanges(changes: { [key: string]: SimpleChange }): any {
    this.rebuild();
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

    if (this.sentence.overlapEntity(this.selectedStart, this.selectedEnd)) {
      //removehighlight is not ok as it could remove existing highligthing
      this.rebuild();
    } else if (this.intent && !this.intent.isUnknownIntent()) {
      this.edited = true;
    }
  }

  addEntity() {
    let dialogRef = this.dialog.open(CreateEntityDialogComponent,
      {
        data: {
          intent: this.intent
        }
      });
    dialogRef.afterClosed().subscribe(result => {
      if (result !== "cancel") {
        let name = result.name;
        let role = result.role;
        const existingEntityType = this.state.findEntityTypeByName(name);
        if (existingEntityType) {
          const entity = new EntityDefinition(name, role);
          this.intent.addEntity(entity);
          this.nlp.saveIntent(this.intent).subscribe(_ => {
              this.onSelect(entity);
              this.snackBar.open(`Entity Type ${entity.qualifiedRole} added`, "Entity added", {duration: 1000})
            }
          );
        } else {
          this.nlp.createEntityType(name).subscribe(e => {
            if (e) {
              const entity = new EntityDefinition(e.name, role);
              this.intent.addEntity(entity);
              this.state.entityTypes.push(e);
              this.nlp.saveIntent(this.intent).subscribe(_ => {
                  this.onSelect(entity);
                  this.snackBar.open(`Entity Type ${entity.qualifiedRole} added`, "Entity added", {duration: 1000})
                }
              );
            } else {
              this.snackBar.open(`Error when creating Entity Type ${name}`, "Error", {duration: 1000});
            }
          });
        }
      }
    });
  }

  private rebuild() {
    this.edited = false;
    this.retrieveIntent();
    this.initTokens();
  }

  private retrieveIntent() {
    if (this.sentence.classification) {
      this.intent = this.state.currentApplication.intentById(this.sentence.classification.intentId);
    }
  }

  onClose() {
    this.edited = false;
  }

  onSelect(entity: EntityDefinition) {
    if (this.selectedStart < this.selectedEnd) {
      this.edited = false;
      const e = new ClassifiedEntity(entity.entityTypeName, entity.role, this.selectedStart, this.selectedEnd);
      this.sentence.classification.entities.push(e);
      this.sentence.classification.entities.sort((e1, e2) => e1.start - e2.start);
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
  constructor(public text: string, public index: number, public entity?: ClassifiedEntity) {
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



