import {
  Component,
  ElementRef,
  EventEmitter,
  HostBinding,
  HostListener,
  Inject,
  Input,
  OnInit,
  Output,
  QueryList,
  ViewChild,
  ViewChildren
} from '@angular/core';
import { NbContextMenuDirective, NbDialogRef, NbMenuService } from '@nebular/theme';
import { StateService } from 'src/app/core-nlp/state.service';
import { scenarioItem, TickContext } from '../../models';
import { Token } from '../../../sentence-analysis/highlight/highlight.component';
import { ClassifiedEntity, ParseQuery, Sentence, SentenceStatus } from '../../../model/nlp';
import { filter, map } from 'rxjs/operators';
import { FormArray, FormControl, FormGroup } from '@angular/forms';
import { THIS_EXPR } from '@angular/compiler/src/output/output_ast';
import { getContrastYIQ } from '../../commons/utils';

export type SentenceExtended = Sentence & { _tokens?: Token[] };
export type ParseQueryExtended = ParseQuery & { _tokens?: Token[] };

@Component({
  selector: 'scenario-intent-edit',
  templateUrl: './intent-edit.component.html',
  styleUrls: ['./intent-edit.component.scss']
})
export class IntentEditComponent implements OnInit {
  @Input() item: scenarioItem;
  @Input() contexts: TickContext[];
  @Output() saveModifications = new EventEmitter();
  @ViewChildren(NbContextMenuDirective) tokensButtons: QueryList<NbContextMenuDirective>;
  @ViewChild('addSentenceInput') addSentenceInput: ElementRef;

  constructor(
    public dialogRef: NbDialogRef<IntentEditComponent>,
    protected state: StateService,
    private nbMenuService: NbMenuService
  ) {}

  _sentences = [];

  getContrastYIQ = getContrastYIQ;

  ngOnInit(): void {
    if (this.item.intentDefinition?.sentences?.length) {
      this.item.intentDefinition?.sentences.forEach((sentence) => {
        this.initTokens(sentence);
        this.sentences.push(new FormControl(sentence));
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
      .pipe(filter(({ tag }) => tag === 'contextsMenu'))
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
      const MySentence = new ParseQuery(
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

  private initTokens(sentence: SentenceExtended | ParseQueryExtended) {
    let i = 0;
    let entityIndex = 0;
    let text;
    let entities;

    if (sentence instanceof Sentence) {
      text = sentence.getText();
      entities = sentence.getEntities();
    } else {
      text = sentence.query;
      entities = [];
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

    this.contextItems = [
      {
        title: 'Choose a context to associate',
        expanded: true,
        children: this.contexts.map((ctx) => {
          return {
            title: ctx.name,
            context: <TickContext>ctx,
            token: token
          };
        })
      }
    ];

    let ClickedButton = this.tokensButtons.find((tButton) => {
      return tButton['hostRef'].nativeElement.innerText == token.text;
    });
    if (ClickedButton) ClickedButton.show();
  }

  associateContextWithEntity(menuBag): void {
    const item = menuBag.item;
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
    let context = this.getContextOfEntity(token);
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
    if (ctx) return `This entity is associated with the context ${ctx.name}`;
    return 'No context associated';
  }
}
