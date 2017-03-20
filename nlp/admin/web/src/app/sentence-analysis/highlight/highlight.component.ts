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

import {Component, Input, ElementRef, OnChanges, OnDestroy, AfterViewInit, SimpleChange} from "@angular/core";
import {Sentence, ClassifiedEntity, EntityDefinition} from "../../model/nlp";
import {NlpService} from "../../nlp-tabs/nlp.service";
import {Intent} from "../../model/application";
import {StateService} from "../../core/state.service";
import {MdDialog, MdSnackBar} from "@angular/material";
import {CreateEntityDialogComponent} from "../create-entity-dialog/create-entity-dialog.component";

declare var TextHighlighter: any;

@Component({
  selector: 'tock-highlight',
  templateUrl: 'highlight.component.html',
  styleUrls: ['highlight.component.css']
})
export class HighlightComponent implements AfterViewInit, OnDestroy, OnChanges {

  @Input() sentence: Sentence;

  intent: Intent;
  textHighlighter: any;
  highlightedNode: any;
  selectedStart: number;
  selectedEnd: number;
  editable: boolean;
  edited: boolean;

  constructor(private elementRef: ElementRef,
    private nlp: NlpService,
    private state: StateService,
    private snackBar: MdSnackBar,
    private dialog: MdDialog) {
    this.editable = true;
    this.edited = false;
    this.selectedStart = -1;
    this.selectedEnd = -1;
  }

  ngAfterViewInit() {
    const _this = this;
    this.textHighlighter = new TextHighlighter(this.elementToHighlight(), {
      color: "#F8BB86",
      onBeforeHighlight: function (range, hlts) {
        return true;
      },
      onAfterHighlight: function (range, hlts) {
        _this.afterHighlighting(range, hlts);
      },
      onRemoveHighlight: function (hlt) {
        return true;
      }
    });
    this.highlightEntities();
  }

  ngOnChanges(changes: {[key: string]: SimpleChange}): any {
    console.log("listen change " + changes);
    this.rebuild();
  }

  ngOnDestroy() {
    if (this.textHighlighter) {
      this.textHighlighter.destroy();
    }
  }

  private retrieveIntent() {
    if (this.sentence.classification) {
      this.intent = this.state.currentApplication.intentById(this.sentence.classification.intentId);
    }
  }

  private elementToHighlight(): HTMLElement {
    return this.elementRef.nativeElement.childNodes[0];
  }

  private addEntity() {
    let dialogRef = this.dialog.open(CreateEntityDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      if (result !== "cancel") {
        let name = result.type;
        if (name.indexOf(":") === -1) {
          name = `${this.state.user.organization}:${name.toLowerCase()}`;
        }
        let role = result.role;
        if (!role || role.length === 0) {
          role = name.split(":")[1];
        }
        const existingEntityType = this.state.findEntityTypeByName(name);
        if (existingEntityType) {
          const entity = new EntityDefinition(name, role);
          this.intent.addEntity(entity);
          this.nlp.saveIntent(this.intent).subscribe(i =>
            this.snackBar.open(`Entity Type ${entity.qualifiedRole} added`, "Entity added", {duration: 1000})
          );
        } else {
          this.nlp.createEntityType(result.type).subscribe(e => {
            if (e) {
              const entity = new EntityDefinition(e.name, role);
              this.intent.addEntity(entity);
              this.state.entityTypes.push(e);
              this.nlp.saveIntent(this.intent).subscribe(i =>
                this.snackBar.open(`Entity Type ${entity.qualifiedRole} added`, "Entity added", {duration: 1000})
              );
            } else {
              this.snackBar.open(`Error new try to create Entity Type ${result.type}`, "Error", {duration: 1000});
            }
          });
        }
      }
    });
  }

  private rebuild() {
    if (this.textHighlighter) {
      this.textHighlighter.removeHighlights();

      this.textHighlighter.el.innerText = this.sentence.text;
      this.edited = false;

      this.highlightEntities();
    }
  }

  onClose() {
    this.edited = false;
    this.textHighlighter.removeHighlights(this.highlightedNode);
  }

  onSelect(entity: EntityDefinition) {
    this.edited = false;
    this.textHighlighter.removeHighlights(this.highlightedNode);
    const e = new ClassifiedEntity(entity.entityTypeName, entity.role,
      this.selectedStart, this.selectedEnd, this.sentence.text.substring(this.selectedStart, this.selectedEnd));
    this.sentence.classification.entities.push(e);
    this.sentence.classification.entities.sort((e1, e2) => e1.start - e2.start);
    this.highlight(e);
  }

  private afterHighlighting(range, hlts) {
    this.selectedStart = -1;
    this.selectedEnd = -1;
    this.highlightedNode = hlts[0];
    this.findSelected(this.textHighlighter.el, new SelectedResult(this.highlightedNode));

    if (this.sentence.overlapEntity(this.selectedStart, this.selectedEnd)) {
      //removehighlight is not ok as it could remove existing highligthing
      this.rebuild();
    } else {
      this.edited = true;
    }
  }

  private highlight(entity: ClassifiedEntity) {
    const sentenceElement = this.textHighlighter.el;
    const range = document.createRange();
    //find
    this.fillRange(sentenceElement, new RangeResult(range, entity.start, entity.end));

    this.textHighlighter.setColor(entity.entityColor);
    const wrapper = TextHighlighter.createWrapper({
      color: entity.entityColor,
      highlightedClass: 'highlighted'
    });
    this.textHighlighter.highlightRange(range, wrapper);
    this.textHighlighter.setColor("#F8BB86");
  }

  private fillRange(node, result) {
    if (!result.found()) {
      if (node.nodeType === 3) {
        const toCount = result.toCount();
        const content = node.textContent;
        if (content.length >= toCount) {
          result.setRange(node, toCount);
          if (!result.found()) {
            const toEndCount = result.toCount();
            if (content.length >= toEndCount) {
              result.setRange(node, toEndCount);
            }
          }
        }
        result.alreadyCount += content.length;
        return;
      } else {
        for (const child of node.childNodes) {
          if (node.nodeType === 1) {
            this.fillRange(child, result);
          }
        }
      }
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
              this.selectedStart = result.alreadyCount;
              this.selectedEnd = result.alreadyCount + content.length;
            } else {
              this.findSelected(child, result);
            }
          }
        }
      }
    }
  }

  private highlightEntities() {
    //need this to bypass angular checks
    window.setTimeout(() => {
        if (this.sentence.classification) {
          this.retrieveIntent();
          for (const e of this.sentence.classification.entities) {
            this.highlight(e);
          }
        }
      }
    );
  }

}

export class SelectedResult {

  selectedNode: any;
  alreadyCount: number;

  constructor(selectedNode: any) {
    this.selectedNode = selectedNode;
    this.alreadyCount = 0;
  }
}

export class RangeResult {
  range: Range;
  startIndex: number;
  endIndex: number;
  alreadyCount: number;
  startFound: boolean;
  endFound: boolean;


  constructor(range: Range, startIndex: number, endIndex: number) {
    this.range = range;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
    this.alreadyCount = 0;
    this.startFound = false;
    this.endFound = false;
  }

  found() {
    return this.startFound && this.endFound;
  }

  setRange(node, index) {
    if (!this.startFound) {
      this.range.setStart(node, index);
      this.startFound = true;
    } else {
      this.range.setEnd(node, index);
      this.endFound = true;
    }
  }

  toCount() {
    if (!this.startFound) {
      return this.startIndex - this.alreadyCount;
    } else {
      return this.endIndex - this.alreadyCount;
    }
  }
}


