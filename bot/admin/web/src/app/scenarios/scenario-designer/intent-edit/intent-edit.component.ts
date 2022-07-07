import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  OnDestroy,
  OnInit,
  Output,
  QueryList,
  ViewChild,
  ViewChildren
} from '@angular/core';
import { takeUntil } from 'rxjs/operators';
import { NbContextMenuDirective, NbDialogRef, NbMenuService } from '@nebular/theme';
import { StateService } from 'src/app/core-nlp/state.service';
import { scenarioItem, TempSentence, TickContext } from '../../models';
import { SelectedResult, Token } from '../../../sentence-analysis/highlight/highlight.component';
import { EntityDefinition, Intent, qualifiedName, Sentence } from '../../../model/nlp';
import { filter } from 'rxjs/operators';
import { FormArray, FormControl, FormGroup } from '@angular/forms';
import { getContrastYIQ } from '../../commons/utils';
import { DialogService } from '../../../core-nlp/dialog.service';
import { ContextCreateComponent } from '../context-create/context-create.component';
import { Subject } from 'rxjs';

export type SentenceExtended = Sentence & { _tokens?: Token[] };
export type TempSentenceExtended = TempSentence & { _tokens?: Token[] };

@Component({
  selector: 'scenario-intent-edit',
  templateUrl: './intent-edit.component.html',
  styleUrls: ['./intent-edit.component.scss']
})
export class IntentEditComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  @Input() item: scenarioItem;
  @Input() contexts: TickContext[];
  @Output() saveModifications = new EventEmitter();
  @ViewChildren(NbContextMenuDirective) tokensButtons: QueryList<NbContextMenuDirective>;
  @ViewChild('addSentenceInput') addSentenceInput: ElementRef;

  qualifiedName = qualifiedName;
  getContrastYIQ = getContrastYIQ;

  constructor(
    public dialogRef: NbDialogRef<IntentEditComponent>,
    protected state: StateService,
    private nbMenuService: NbMenuService,
    private dialogService: DialogService
  ) {}

  _sentences = [];

  ngOnInit(): void {
    if (this.item.intentDefinition?.sentences?.length) {
      this.item.intentDefinition?.sentences.forEach((sentence) => {
        const clone = JSON.parse(JSON.stringify(sentence));
        this.initTokens(clone);
        this.sentences.push(new FormControl(clone));
      });
    }
    if (this.item.intentDefinition?._sentences?.length) {
      this.item.intentDefinition?._sentences.forEach((sentence) => {
        this.initTokens(sentence);
        this._sentences.push(sentence);
      });
    }

    this.contexts.forEach((ctx) => {
      this.contextsEntities.push(new FormControl(ctx));
    });

    this.nbMenuService
      .onItemClick()
      .pipe(
        takeUntil(this.destroy),
        filter(({ tag }) => tag === 'contextsMenu')
      )
      .subscribe((menuBag) => this.associateContextWithEntity(menuBag));
  }

  form: FormGroup = new FormGroup({
    sentences: new FormArray([]),
    contextsEntities: new FormArray([])
  });

  get sentences(): FormArray {
    return this.form.get('sentences') as FormArray;
  }
  get contextsEntities(): FormArray {
    return this.form.get('contextsEntities') as FormArray;
  }

  dissociateIntent() {
    delete this.item.intentDefinition;
    this.cancel();
  }

  addSentence() {
    const eventTarget = this.addSentenceInput.nativeElement as HTMLInputElement;

    if (eventTarget.value.trim()) {
      const app = this.state.currentApplication;
      const language = this.state.currentLocale;
      const MySentence = new TempSentence(
        app.namespace,
        app.name,
        language,
        eventTarget.value.trim(),
        false,
        ''
      );
      this.initTokens(MySentence);
      this.sentences.push(new FormControl(MySentence));
      eventTarget.value = '';
    }
  }

  isExistingSentence(sentence) {
    return sentence instanceof Sentence;
  }

  removeSentence(sentence) {
    this.sentences.removeAt(this.sentences.value.findIndex((stce) => stce === sentence));
    this.form.markAsDirty();
  }

  save() {
    this.saveModifications.emit(this.form.value);
  }

  cancel(): void {
    this.dialogRef.close();
  }

  contextItems = [
    {
      title: 'Choose a context to associate',
      expanded: false,
      children: []
    }
  ];

  @HostListener('click', ['$event.target'])
  hideContextsMenus() {
    this.tokensButtons.forEach((tb) => tb.hide());
  }

  displayContextsMenu(event: MouseEvent, token: Token) {
    event.stopPropagation();
    this.hideContextsMenus();
    if (!token.entity) return;
    const exists = this.getContextOfEntity({
      entity: { type: token.entity.type, role: token.entity.role }
    });
    if (exists) return;

    let childrens: any[] = this.contexts.map((ctx) => {
      return {
        title: ctx.name,
        icon: 'attach-outline',
        context: <TickContext>ctx,
        token: token
      };
    });

    childrens.push({
      title: 'Add a new context',
      icon: 'plus-outline',
      addContext: true,
      token: token
    });

    this.contextItems = [
      {
        title: 'Choose a context to associate',
        expanded: true,
        children: childrens
      }
    ];

    let ClickedButton = this.tokensButtons.find((tButton) => {
      return tButton['hostRef'].nativeElement.innerText == token.text;
    });
    if (ClickedButton) ClickedButton.show();
  }

  addContext(menuBag): void {
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
            entityType: menuBag.item.token.entity.type,
            entityRole: menuBag.item.token.entity.role
          })
        );
        validate.unsubscribe();
        modal.close();
      });
  }

  associateContextWithEntity(menuBag): void {
    const item = menuBag.item;

    if (item.addContext) {
      this.addContext(menuBag);
      return;
    }

    const exists = this.getContextOfEntity({
      entity: { type: item.token.entity.type, role: item.token.entity.role }
    });
    if (exists) return;

    const newContextDef = {
      ...item.context,
      entityType: item.token.entity.type,
      entityRole: item.token.entity.role
    };
    this.contextsEntities.push(new FormControl(newContextDef));
  }

  dissociateContextFromEntity(token) {
    let index = this.getContextIndexOfEntity(token);
    let context = JSON.parse(JSON.stringify(this.getContextOfEntity(token)));
    delete context.entityType;
    delete context.entityRole;
    this.contextsEntities.setControl(index, new FormControl(context));
  }

  getContextIndexOfEntity(token) {
    return this.contextsEntities.value.findIndex((ctx) => {
      return (
        token.entity?.type &&
        token.entity?.type == ctx.entityType &&
        token.entity?.role == ctx.entityRole
      );
    });
  }
  getContextOfEntity(token) {
    return this.contextsEntities.value.find((ctx) => {
      return (
        token.entity?.type &&
        token.entity?.type == ctx.entityType &&
        token.entity?.role == ctx.entityRole
      );
    });
  }

  getTokenTooltip(token) {
    const ctx = this.getContextOfEntity(token);
    if (ctx)
      return `This entity is associated with the context ${ctx.name} (click on attach to dissociate)`;
    return 'No context associated';
  }

  private initTokens(sentence: SentenceExtended | TempSentenceExtended) {
    let i = 0;
    let entityIndex = 0;
    let text;
    let entities;
    if (sentence instanceof Sentence) {
      text = sentence.getText();
      entities = sentence.getEntities();
    } else {
      text = sentence.query;
      entities = sentence.classification.entities;
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
    sentence._tokens = result;
  }

  txtSelectionSentence: TempSentenceExtended;
  txtSelectionStart: number;
  txtSelectionEnd: number;

  textSelected(sentence: TempSentenceExtended): void {
    const windowsSelection = window.getSelection();
    if (windowsSelection.rangeCount > 0) {
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
      this.txtSelectionStart = -1;
      this.txtSelectionEnd = -1;
      this.findSelected(span.parentNode, new SelectedResult(span, start, end));

      // this.txtSelectionStart = start;
      // this.txtSelectionEnd = end;
      this.txtSelectionSentence = sentence;
    }
  }

  private findSelected(node, result) {
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
            } else {
              this.findSelected(child, result);
            }
          }
        }
      }
    }
  }

  collectAllEntities() {
    let intents = new Set();
    this._sentences.forEach((_sentence) => {
      if (_sentence.classification) {
        const intent = this.state.currentApplication.intentById(_sentence.classification.intentId);
        intents.add(intent);
      }
    });

    let entities = new Set();
    intents.forEach((intent: Intent) => {
      intent.entities.forEach((entity) => {
        entities.add(entity);
      });
    });

    return [...entities];
  }

  addEntityToSentence(entity: EntityDefinition) {
    if (this.txtSelectionStart < this.txtSelectionEnd) {
      // this.edited = false;
      const text = this.txtSelectionSentence.query;
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
          this.txtSelectionSentence.classification.entities.push({
            type: entity.entityTypeName,
            role: entity.role,
            start: this.txtSelectionStart,
            end: this.txtSelectionEnd,
            entityColor: entity.entityColor
          });
          this.txtSelectionSentence.classification.entities.sort((e1, e2) => e1.start - e2.start);
        }
        this.initTokens(this.txtSelectionSentence);
      }

      this.txtSelectionSentence = undefined;
    }
  }

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }
}
