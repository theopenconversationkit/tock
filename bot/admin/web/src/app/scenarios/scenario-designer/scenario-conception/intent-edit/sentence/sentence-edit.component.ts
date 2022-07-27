import { Overlay, OverlayRef } from '@angular/cdk/overlay';
import { TemplatePortal } from '@angular/cdk/portal';
import {
  Component,
  EventEmitter,
  HostListener,
  Input,
  OnDestroy,
  OnInit,
  Output,
  QueryList,
  TemplateRef,
  ViewChild,
  ViewChildren,
  ViewContainerRef
} from '@angular/core';
import { FormArray, FormControl } from '@angular/forms';
import { NbContextMenuDirective } from '@nebular/theme';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { DialogService } from '../../../../../core-nlp/dialog.service';
import { StateService } from '../../../../../core-nlp/state.service';
import { EntityDefinition, qualifiedName, qualifiedRole, Sentence } from '../../../../../model/nlp';
import { CreateEntityDialogComponent } from '../../../../../sentence-analysis/create-entity-dialog/create-entity-dialog.component';
import {
  SelectedResult,
  Token
} from '../../../../../sentence-analysis/highlight/highlight.component';
import { getContrastYIQ } from '../../../../commons/utils';
import { TickContext } from '../../../../models';
import { ContextCreateComponent } from '../../../scenario-conception/context-create/context-create.component';
import { SentenceExtended, TempSentenceExtended } from '../intent-edit.component';

@Component({
  selector: 'scenario-sentence-edit',
  templateUrl: './sentence-edit.component.html',
  styleUrls: ['./sentence-edit.component.scss']
})
export class SentenceEditComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  @Input() sentence: SentenceExtended | TempSentenceExtended;
  @Input() contexts: TickContext[];
  @Input() contextsEntities: FormArray;

  @Input() allEntities: [];
  @Output() componentActivated = new EventEmitter();
  @Output() entityAdded = new EventEmitter();
  @Output() storeModifiedSentence = new EventEmitter();

  @ViewChildren(NbContextMenuDirective) tokensButtons: QueryList<NbContextMenuDirective>;

  qualifiedName = qualifiedName;
  getContrastYIQ = getContrastYIQ;

  constructor(
    protected state: StateService,
    private dialogService: DialogService,
    public overlay: Overlay,
    public viewContainerRef: ViewContainerRef
  ) {}

  ngOnInit(): void {
    this.initTokens();
  }

  setActive(): void {
    this.componentActivated.emit(this);
  }

  outsideClick(): void {
    this.txtSelection = false;
    this.hideTokenMenu();
  }

  overlayRef: OverlayRef | null;
  @ViewChild('userMenu') userMenu: TemplateRef<any>;

  hideTokenMenu(): void {
    if (this.overlayRef) this.overlayRef.detach();
  }

  displayTokenMenu(event, token): void {
    event.stopPropagation();
    this.hideTokenMenu();
    if (!token.entity) return;

    const positionStrategy = this.overlay
      .position()
      .flexibleConnectedTo(event.target)
      .withPositions([
        {
          originX: 'start',
          originY: 'bottom',
          overlayX: 'start',
          overlayY: 'top'
        },
        {
          originX: 'start',
          originY: 'center',
          overlayX: 'end',
          overlayY: 'center'
        },
        {
          originX: 'end',
          originY: 'center',
          overlayX: 'start',
          overlayY: 'center'
        }
      ]);

    this.overlayRef = this.overlay.create({
      positionStrategy,
      scrollStrategy: this.overlay.scrollStrategies.reposition()
    });

    this.overlayRef.attach(
      new TemplatePortal(this.userMenu, this.viewContainerRef, {
        $implicit: token
      })
    );
  }

  addContext(token): void {
    const modal = this.dialogService.openDialog(ContextCreateComponent, {
      context: {}
    });
    const validate = modal.componentRef.instance.validate
      .pipe(takeUntil(this.destroy))
      .subscribe((contextDef) => {
        this.contextsEntities.push(
          new FormControl({
            name: contextDef.name,
            type: 'string',
            entityType: token.entity.type,
            entityRole: token.entity.role
          })
        );
        validate.unsubscribe();
        modal.close();
        this.hideTokenMenu();
      });
  }

  associateContextWithEntity(token, context): void {
    const exists = this.getContextOfEntity({
      entity: { type: token.entity.type, role: token.entity.role }
    });
    if (exists) return;

    const newContextDef = {
      ...context,
      entityType: token.entity.type,
      entityRole: token.entity.role
    };
    this.contextsEntities.push(new FormControl(newContextDef));
    this.hideTokenMenu();
  }

  dissociateContextFromEntity(token): void {
    let index = this.getContextIndexOfEntity(token);
    let context = JSON.parse(JSON.stringify(this.getContextOfEntity(token)));
    delete context.entityType;
    delete context.entityRole;
    this.contextsEntities.setControl(index, new FormControl(context));
    this.hideTokenMenu();
  }

  getContextIndexOfEntity(token): number {
    return this.contextsEntities.value.findIndex((ctx) => {
      return (
        token.entity?.type &&
        token.entity?.type == ctx.entityType &&
        token.entity?.role == ctx.entityRole
      );
    });
  }
  getContextOfEntity(token): TickContext {
    return this.contextsEntities.value.find((ctx) => {
      return (
        token.entity?.type &&
        token.entity?.type == ctx.entityType &&
        token.entity?.role == ctx.entityRole
      );
    });
  }

  getTokenTooltip(token): string {
    if (!token.entity) return 'Select a part of this sentence to associate an entity';
    const entity = new EntityDefinition(token.entity.type, token.entity.role);
    const ctx = this.getContextOfEntity(token);
    if (ctx) {
      return `The entity "${entity.qualifiedName(
        this.state.user
      )}" is associated with the context "${ctx.name}" (click to edit)`;
    }
    return `Entity "${entity.qualifiedName(this.state.user)}" (click to edit)`;
  }

  initTokens(): void {
    let i = 0;
    let entityIndex = 0;
    let text;
    let entities;
    if (this.sentence instanceof Sentence) {
      text = this.sentence.getText();
      entities = this.sentence.getEntities();
    } else {
      text = this.sentence.query;
      entities = this.sentence.classification.entities;
    }

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
    this.sentence._tokens = result;
  }

  txtSelection: boolean = false;
  txtSelectionStart: number;
  txtSelectionEnd: number;

  @HostListener('mouseup', ['$event'])
  textSelected(event: MouseEvent): void {
    event.stopPropagation();
    this.hideTokenMenu();
    this.setActive();

    const windowsSelection = window.getSelection();
    if (windowsSelection.rangeCount > 0) {
      const selection: Range = windowsSelection.getRangeAt(0);
      let start: number = selection.startOffset;
      let end: number = selection.endOffset;

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

      const span: HTMLElement = selection.startContainer.parentElement;
      this.txtSelectionStart = -1;
      this.txtSelectionEnd = -1;
      this.findSelected(span.parentNode, new SelectedResult(span, start, end));
    }
  }

  private findSelected(node, result): void {
    if (this.txtSelectionStart == -1) {
      if (node.nodeType === Node.TEXT_NODE) {
        const content = node.textContent;
        result.alreadyCount += content.length;
      } else {
        for (const child of node.childNodes) {
          if (node.nodeType === Node.ELEMENT_NODE) {
            if (node === result.selectedNode) {
              this.txtSelectionStart = result.alreadyCount + result.startOffset;
              this.txtSelectionEnd = this.txtSelectionStart + result.endOffset - result.startOffset;
              this.txtSelection = true;
            } else {
              this.findSelected(child, result);
            }
          }
        }
      }
    }
  }

  removeEntityFromSentence(token): void {
    if (this.sentence instanceof Sentence) {
      const entitiesCopy = JSON.parse(JSON.stringify(this.sentence.classification.entities)).filter(
        (e) => {
          return !(
            token.entity.type === e.type &&
            token.entity.role === e.role &&
            token.start === e.start &&
            token.end === e.end
          );
        }
      );
      this.storeModifiedSentence.emit({ sentence: this.sentence, entities: entitiesCopy });
    } else {
      this.sentence.classification.entities = this.sentence.classification.entities.filter((e) => {
        return !(
          token.entity.type === e.type &&
          token.entity.role === e.role &&
          token.start === e.start &&
          token.end === e.end
        );
      });
      this.hideTokenMenu();
      this.initTokens();
    }
  }

  addEntityToSentence(entity: EntityDefinition): void {
    if (this.txtSelectionStart < this.txtSelectionEnd) {
      this.txtSelection = false;

      let text: string;
      if (this.sentence instanceof Sentence) {
        text = this.sentence.text;
      } else {
        text = this.sentence.query;
      }

      if (this.txtSelectionStart >= 0 && this.txtSelectionEnd <= text.length) {
        //trim spaces
        for (let i = this.txtSelectionEnd - 1; i >= this.txtSelectionStart; i--) {
          if (text[i].trim().length === 0) {
            this.txtSelectionEnd--;
          } else {
            break;
          }
        }
        for (let i = this.txtSelectionStart; i < this.txtSelectionEnd; i++) {
          if (text[i].trim().length === 0) {
            this.txtSelectionStart++;
          } else {
            break;
          }
        }
        if (this.txtSelectionStart < this.txtSelectionEnd) {
          const tempEntity = {
            type: entity.entityTypeName,
            role: entity.role,
            start: this.txtSelectionStart,
            end: this.txtSelectionEnd,
            entityColor: entity.entityColor,
            qualifiedRole: qualifiedRole(entity.entityTypeName, entity.role),
            subEntities: []
          };

          if (this.sentence instanceof Sentence) {
            let entitiesCopy = JSON.parse(JSON.stringify(this.sentence.classification.entities));
            entitiesCopy.push(tempEntity);
            entitiesCopy.sort((e1, e2) => e1.start - e2.start);
            this.storeModifiedSentence.emit({ sentence: this.sentence, entities: entitiesCopy });
          } else {
            this.sentence.classification.entities.push(tempEntity);
            this.sentence.classification.entities.sort((e1, e2) => e1.start - e2.start);
          }
        }
        this.initTokens();
      }

      this.txtSelection = false;
    }
  }

  getEntityTooltip(entity: EntityDefinition): string {
    return `Assign the entity "${entity.qualifiedName(this.state.user)}" to the selected text`;
  }

  callEntitiesModal(event: MouseEvent): void {
    event.stopPropagation();
    const modal = this.dialogService.openDialog(CreateEntityDialogComponent, {
      context: {}
    });
    modal.onClose.pipe(takeUntil(this.destroy)).subscribe((res) => {
      if (res && res !== 'cancel') {
        const entity = new EntityDefinition(res.name, res.role);
        this.addEntityToSentence(entity);
        this.entityAdded.emit();
      }
    });
  }

  ngOnDestroy(): void {
    this.hideTokenMenu();
    this.destroy.next();
    this.destroy.complete();
  }
}
