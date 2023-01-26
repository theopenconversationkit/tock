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

import { ChangeDetectorRef, Component, ElementRef, HostListener, Input, OnChanges, OnInit, SimpleChange, ViewChild } from '@angular/core';
import { ClassifiedEntity, EntityContainer, EntityDefinition, EntityType, EntityWithSubEntities, Intent, Sentence } from '../../model/nlp';
import { NlpService } from '../../nlp-tabs/nlp.service';
import { StateService } from '../../core-nlp/state.service';
import { CreateEntityDialogComponent } from '../create-entity-dialog/create-entity-dialog.component';
import { User, UserRole } from '../../model/auth';
import { CoreConfig } from '../../core-nlp/core.config';
import { Router } from '@angular/router';
import { isNullOrUndefined } from '../../model/commons';
import { DialogService } from '../../core-nlp/dialog.service';
import { NbDialogService } from '@nebular/theme';

@Component({
  selector: 'tock-highlight',
  templateUrl: 'highlight.component.html',
  styleUrls: ['highlight.component.css']
})
export class HighlightComponent implements OnInit, OnChanges {
  @Input() sentence: EntityContainer;
  @Input() readOnly: boolean = false;
  @Input() fontSize: string = 'inherit';
  @Input() prefix: string = 's';
  @Input() leftPadding: number = 0;
  @Input() displayActions: boolean = true;

  entityProvider: EntityProvider;
  selectedStart: number;
  selectedEnd: number;
  editable: boolean;
  edited: boolean;
  tokens: Token[];
  currentDblClick: boolean;

  //used to copy to clipboard
  @ViewChild('copy') tmpTextArea: ElementRef;

  // the tokens container
  @ViewChild('tokensContainer') tokensContainer: ElementRef;

  constructor(
    private nlp: NlpService,
    public state: StateService,
    private nbDialogService: NbDialogService,
    private dialog: DialogService,
    private router: Router,
    public coreConfig: CoreConfig,
    private changeDetectorRef: ChangeDetectorRef
  ) {
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

  private handleParentSelect() {
    if (this.sentence instanceof EntityWithSubEntities) {
      const e = this.sentence as EntityWithSubEntities;
      if (e.hasSelection()) {
        setTimeout((_) => {
          this.selectedStart = e.startSelection;
          this.selectedEnd = e.endSelection;

          const tokenMatch = this.tokens.find((t) => t.start <= e.startSelection && t.end >= e.endSelection);
          if (!tokenMatch) {
            return;
          }
          const r = document.createRange();
          const tokenId = this.prefix + tokenMatch.index;
          const c: Node = this.tokensContainer.nativeElement;

          let token;
          c.childNodes.forEach((s) => {
            if ((s as Element).id === tokenId) token = s.firstChild;
          });
          if (token) {
            r.setStart(token, this.selectedStart - tokenMatch.start);
            r.setEnd(token, this.selectedEnd - tokenMatch.start);
            window.getSelection().removeAllRanges();
            window.getSelection().addRange(r);
            this.select();
          }
        });
      }
    }
  }

  @HostListener('window:keyup', ['$event'])
  keyup(event: KeyboardEvent) {
    if (event.shiftKey && (event.keyCode === 39 || event.keyCode === 37)) {
      this.select();
    }
  }

  remove(event: MouseEvent, token: Token) {
    if (token && token.entity && event.altKey) {
      this.currentDblClick = true;
      this.sentence.removeEntity(token.entity);
      setTimeout((_) => {
        this.sentence.cleanupEditedSubEntities();
        this.rebuild();
        this.currentDblClick = false;
      });
      event.stopPropagation();
    }
  }

  select() {
    setTimeout((_) => {
      const windowsSelection = window.getSelection();
      if (windowsSelection.rangeCount > 0 && !this.currentDblClick) {
        const selection = windowsSelection.getRangeAt(0);
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
          if (this.state.currentApplication.supportSubEntities) {
            this.sentence.addEditedSubEntities(overlap).setSelection(this.selectedStart - overlap.start, this.selectedEnd - overlap.start);
          }
          window.getSelection().removeAllRanges();
        } else if (this.entityProvider.isValid()) {
          this.edited = true;
        }
      }
    });
  }

  addEntity() {
    const dialogRef = this.nbDialogService.open(CreateEntityDialogComponent, {
      context: {
        entityProvider: this.entityProvider
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result && result !== 'cancel') {
        const name = result.name;
        const role = result.role;
        const existingEntityType = this.state.findEntityTypeByName(name);
        if (existingEntityType) {
          const entity = new EntityDefinition(name, role);
          const result = this.entityProvider.addEntity(entity, this);
          if (result) {
            this.dialog.notify(result);
          }
        } else {
          this.nlp.createEntityType(name).subscribe((e) => {
            if (e) {
              const entity = new EntityDefinition(e.name, role);
              const entities = this.state.entityTypes.getValue().slice(0);
              entities.push(e);
              this.state.entityTypes.next(entities);
              const result = this.entityProvider.addEntity(entity, this);
              if (result) {
                this.dialog.notify(result);
              }
            } else {
              this.dialog.notify(`Error when creating Entity Type ${name}`);
            }
          });
        }
      }
    });
  }

  notifyAddEntity(entity: EntityDefinition) {
    this.onSelect(entity);
    this.dialog.notify(`Entity Type ${entity.qualifiedRole} added`, 'Entity added');
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
      const text = this.sentence.getText();
      if (this.selectedStart >= 0 && this.selectedEnd <= text.length) {
        //trim spaces
        for (let i = this.selectedEnd - 1; i >= this.selectedStart; i--) {
          if (text[i].trim().length === 0) {
            this.selectedEnd--;
          } else {
            break;
          }
        }
        for (let i = this.selectedStart; i < this.selectedEnd; i++) {
          if (text[i].trim().length === 0) {
            this.selectedStart++;
          } else {
            break;
          }
        }
        if (this.selectedStart < this.selectedEnd) {
          const e = new ClassifiedEntity(entity.entityTypeName, entity.role, this.selectedStart, this.selectedEnd, []);
          this.sentence.addEntity(e);
        }
        this.initTokens();
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

  isRootSentence(): boolean {
    return this.sentence instanceof Sentence;
  }

  answerToSentence() {
    this.router.navigate([this.coreConfig.answerToSentenceUrl], {
      queryParams: {
        text: this.sentence.getText()
      }
    });
  }

  copyToClipboard() {
    const t = this.tmpTextArea.nativeElement;
    t.style.display = 'block';
    const text = this.sentence.getText();
    t.value = text;
    t.select();
    let successful = false;
    try {
      successful = document.execCommand('copy');
    } catch (err) {
      //do nothing
    }
    t.style.display = 'none';
    this.dialog.notify(successful ? `${text} copied to clipboard` : `Unable to copy to clipboard`, 'Clipboard');
  }

  canReveal(): boolean {
    return this.sentence instanceof Sentence && this.sentence.key && this.state.hasRole(UserRole.admin);
  }

  reveal() {
    const sentence = this.sentence as Sentence;
    this.nlp.revealSentence(sentence).subscribe((s) => {
      sentence.text = s.text;
      sentence.key = null;
      this.sentence = sentence.clone();
      this.ngOnInit();
      this.changeDetectorRef.detectChanges();
    });
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
      return '';
    } else {
      return this.entity.qualifiedName(user) + ' = ' + sentence.entityValue(this.entity);
    }
  }

  color(): string {
    if (this.entity) {
      return this.entity.entityColor;
    } else {
      return '';
    }
  }
}

export interface EntityProvider {
  reload();

  getEntities(): EntityDefinition[];

  isValid(): boolean;

  hasEntityRole(role: string): boolean;

  addEntity(entity: EntityDefinition, highlight: HighlightComponent): string;
}

export class IntentEntityProvider implements EntityProvider {
  constructor(private nlp: NlpService, private state: StateService, private sentence: Sentence, private intent?: Intent) {}

  addEntity(entity: EntityDefinition, highlight: HighlightComponent): string {
    this.intent.addEntity(entity);
    const allEntities = this.state.entities.getValue();
    if (!allEntities.some((e) => e.entityTypeName === entity.entityTypeName && e.role === entity.role)) {
      this.state.entities.next(this.state.currentApplication.allEntities());
    }
    this.nlp.saveIntent(this.intent).subscribe((_) => {
      highlight.notifyAddEntity(entity);
    });
    return null;
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
  constructor(
    private nlp: NlpService,
    private state: StateService,
    private entity: EntityWithSubEntities,
    private entityType?: EntityType
  ) {}

  addEntity(entity: EntityDefinition, highlight: HighlightComponent): string {
    if (
      this.entity.root.containsEntityType(entity.entityTypeName) ||
      this.containsEntityType(this.state.findEntityTypeByName(entity.entityTypeName), this.entity.root.type, new Set())
    ) {
      return 'adding recursive sub entity is not allowed';
    }
    this.entityType.addEntity(entity);
    this.nlp.updateEntityType(this.entityType).subscribe((_) => {
      highlight.notifyAddEntity(entity);
    });
    return null;
  }

  private containsEntityType(entityType: EntityType, entityTypeName: string, entityTypes: Set<string>): boolean {
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
