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
import { NbDialogRef } from '@nebular/theme';
import { StateService } from 'src/app/core-nlp/state.service';
import { ScenarioItem, TempSentence, ScenarioContext, ScenarioVersionExtended } from '../../../models';
import { Token } from '../../../../sentence-analysis/highlight/highlight.component';
import { entityColor, EntityDefinition, Intent, qualifiedName, qualifiedRole, Sentence } from '../../../../model/nlp';
import { FormArray, FormControl, FormGroup } from '@angular/forms';
import { Observable, of, Subject } from 'rxjs';
import { SentenceEditComponent } from './sentence/sentence-edit.component';
import { getContrastYIQ, getScenarioActionDefinitions, normalizedSnakeCaseUpper } from '../../../commons/utils';
import { deepCopy } from '../../../../shared/utils';

export type SentenceExtended = Sentence & { _tokens?: Token[] };
export type TempSentenceExtended = TempSentence & { _tokens?: Token[] };

const CONTEXT_NAME_MINLENGTH = 5;

@Component({
  selector: 'scenario-intent-edit',
  templateUrl: './intent-edit.component.html',
  styleUrls: ['./intent-edit.component.scss']
})
export class IntentEditComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  @Input() scenario: ScenarioVersionExtended;
  @Input() item: ScenarioItem;
  @Input() contexts: ScenarioContext[];
  @Input() isReadonly: boolean;

  @Output() saveModifications = new EventEmitter();
  @Output() onRemoveDefinition = new EventEmitter();

  @ViewChildren(SentenceEditComponent) sentencesComponents: QueryList<SentenceEditComponent>;
  @ViewChild('addSentenceInput') addSentenceInput: ElementRef;
  @ViewChild('addContextInput') addContextInput: ElementRef;

  private qualifiedName = qualifiedName;

  constructor(public dialogRef: NbDialogRef<IntentEditComponent>, protected state: StateService) {}

  _sentences: Sentence[] = [];

  ngOnInit(): void {
    this.form.patchValue({ primary: this.item.intentDefinition.primary });
    if (this.item.main || this.isReadonly) {
      this.form.get('primary').disable();
    }

    if (this.item.intentDefinition?.sentences?.length) {
      this.item.intentDefinition?.sentences.forEach((sentence) => {
        const clone = deepCopy(sentence);
        this.sentences.push(new FormControl(clone));
      });
    }
    if (this.item.intentDefinition?._sentences?.length) {
      this.item.intentDefinition?._sentences.forEach((sentence) => {
        this._sentences.push(sentence);
      });
    }

    this.contexts.forEach((ctx) => {
      this.contextsEntities.push(new FormControl(ctx));
    });

    if (this.item.intentDefinition?.outputContextNames?.length) {
      this.item.intentDefinition.outputContextNames.forEach((contextName) => {
        this.outputContextNames.push(new FormControl(contextName));
      });
    }

    this.collectAllEntities();
  }

  @HostListener('mouseup', ['$event'])
  hideContextsMenus() {
    this.sentencesComponents.forEach((sc) => sc.outsideClick());
  }

  componentActivated(componentInstance) {
    this.sentencesComponents.forEach((sc) => {
      if (sc !== componentInstance) {
        sc.outsideClick();
      }
    });
  }

  entityAdded() {
    this.collectAllEntities();
  }

  storeModifiedSentence(data) {
    const app = this.state.currentApplication;
    const language = this.state.currentLocale;
    let sentenceCopy = new TempSentence(app.namespace, app.name, language, data.sentence.text, false, '');
    sentenceCopy.classification.entities = data.entities;
    this.sentences.push(new FormControl(sentenceCopy));
  }

  isSentenceModified(sentence): boolean {
    return this.sentences.value.find((s) => s.query === sentence.text) ? true : false;
  }

  copyLabelToSentences(): void {
    this.addSentence(this.item.intentDefinition.label);
  }

  isLabelASentence(): boolean {
    return this.isStringAlreadyASentence(this.item.intentDefinition.label);
  }

  isStringAlreadyASentence(string: string): boolean {
    let isInSentences = this.sentences.value.find((s) => {
      return s.query === string;
    });
    if (isInSentences) return true;

    let isIn_Sentences = this._sentences.find((stnc) => {
      return stnc.text === string;
    });

    return isIn_Sentences ? true : false;
  }

  form: FormGroup = new FormGroup({
    sentences: new FormArray([]),
    contextsEntities: new FormArray([]),
    primary: new FormControl(false),
    outputContextNames: new FormArray([])
  });

  get sentences(): FormArray {
    return this.form.get('sentences') as FormArray;
  }
  get contextsEntities(): FormArray {
    return this.form.get('contextsEntities') as FormArray;
  }
  get outputContextNames(): FormArray {
    return this.form.get('outputContextNames') as FormArray;
  }

  get primary(): FormControl {
    return this.form.get('primary') as FormControl;
  }

  dissociateIntent() {
    this.onRemoveDefinition.emit();
  }

  addSentence(sentenceString?) {
    let eventTarget;
    if (!sentenceString) {
      eventTarget = this.addSentenceInput.nativeElement as HTMLInputElement;
      sentenceString = eventTarget.value.trim();
    }

    if (sentenceString) {
      if (!this.isStringAlreadyASentence(sentenceString)) {
        const app = this.state.currentApplication;
        const language = this.state.currentLocale;
        const newSentence = new TempSentence(app.namespace, app.name, language, sentenceString, false, '');
        this.sentences.push(new FormControl(newSentence));
      }
      if (eventTarget) eventTarget.value = '';
    }
  }

  isExistingSentence(sentence) {
    return sentence instanceof Sentence;
  }

  removeSentence(sentence) {
    this.sentences.removeAt(this.sentences.value.findIndex((stce) => stce === sentence));
    this.form.markAsDirty();
  }

  entitiesCache = [];
  collectAllEntities() {
    let entities = [];

    let intents = new Set();
    this._sentences.forEach((_sentence) => {
      if (_sentence.classification) {
        const intent = this.state.currentApplication.intentById(_sentence.classification.intentId);
        intents.add(intent);
      }
    });

    intents.forEach((intent: Intent) => {
      intent.entities.forEach((entity) => {
        this.pushUnicEntity(entities, entity);
      });
    });

    this.sentences.value.forEach((formControl) => {
      formControl.classification?.entities.forEach((e) => {
        const entity = new EntityDefinition(e.type, e.role);
        this.pushUnicEntity(entities, entity);
      });
    });

    this.contexts.forEach((ctx) => {
      if (ctx.entityType && ctx.entityRole) {
        const entity = new EntityDefinition(ctx.entityType, ctx.entityRole);
        this.pushUnicEntity(entities, entity);
      }
    });

    this.entitiesCache = entities;
  }

  pushUnicEntity(entities, entity) {
    if (entities.some((e) => e.entityTypeName === entity.entityTypeName && e.role === entity.role)) return;
    entities.push(entity);
  }

  contextsAutocompleteValues: Observable<string[]>;

  updateContextsAutocompleteValues(event?: KeyboardEvent): void {
    let results = this.contexts.map((ctx) => ctx.name);

    results = results.filter((r: string) => {
      return !this.outputContextNames.value.includes(r);
    });

    if (event) {
      const targetEvent = event.target as HTMLInputElement;
      results = results.filter((ctxName: string) => ctxName.includes(targetEvent.value.toUpperCase()));
    }

    this.contextsAutocompleteValues = of(results);
  }

  outputContextsAddError: any = {};

  resetContextsAddErrors(): void {
    this.outputContextsAddError = {};
  }

  addContext(): void {
    const eventTarget = this.addContextInput.nativeElement as HTMLInputElement;
    const ctxName = normalizedSnakeCaseUpper(eventTarget.value);

    this.resetContextsAddErrors();

    if (!ctxName) return;

    if (ctxName.length < CONTEXT_NAME_MINLENGTH) {
      this.outputContextsAddError = {
        errors: { minlength: { requiredLength: CONTEXT_NAME_MINLENGTH } }
      };
      return;
    }

    const actions = getScenarioActionDefinitions(this.scenario);

    if (actions.find((act) => act.name === ctxName)) {
      this.outputContextsAddError = { errors: { custom: 'A context cannot have the same name as an action' } };
      return;
    }

    if (this.outputContextNames.value.find((ctx: string) => ctx == ctxName)) {
      this.outputContextsAddError = {
        errors: { custom: `This output context is already associated with this intent` }
      };
      return;
    }

    this.outputContextNames.push(new FormControl(ctxName));
    eventTarget.value = '';
    this.form.markAsDirty();
  }

  removeContext(contextName: string): void {
    this.outputContextNames.removeAt(this.outputContextNames.value.findIndex((ctx: string) => ctx === contextName));
    this.form.markAsDirty();
  }

  getContextByName(context: string): ScenarioContext[] {
    return [
      this.contexts.find((ctx) => {
        return ctx.name == context;
      })
    ];
  }

  getContextEntityColor(context: ScenarioContext): string | undefined {
    if (context.entityType) return entityColor(qualifiedRole(context.entityType, context.entityRole));
  }

  getContextEntityContrast(context: ScenarioContext): string | undefined {
    if (context.entityType) return getContrastYIQ(entityColor(qualifiedRole(context.entityType, context.entityRole)));
  }

  save() {
    // here we use getRawValue because we need the value of the potentially disabled primary field
    this.saveModifications.emit(this.form.getRawValue());
  }

  cancel(): void {
    this.dialogRef.close();
  }

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }
}
