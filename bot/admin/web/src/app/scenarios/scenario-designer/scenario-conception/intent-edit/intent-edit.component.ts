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
import { NbDialogRef, NbMenuService } from '@nebular/theme';
import { StateService } from 'src/app/core-nlp/state.service';
import { scenarioItem, TempSentence, TickContext } from '../../../models';
import { Token } from '../../../../sentence-analysis/highlight/highlight.component';
import { EntityDefinition, Intent, Sentence } from '../../../../model/nlp';
import { FormArray, FormControl, FormGroup } from '@angular/forms';
import { DialogService } from '../../../../core-nlp/dialog.service';
import { Subject } from 'rxjs';
import { SentenceEditComponent } from './sentence/sentence-edit.component';

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
  @ViewChildren(SentenceEditComponent) sentencesComponents: QueryList<SentenceEditComponent>;
  @ViewChild('addSentenceInput') addSentenceInput: ElementRef;

  constructor(public dialogRef: NbDialogRef<IntentEditComponent>, protected state: StateService) {}

  _sentences: Sentence[] = [];

  ngOnInit(): void {
    this.form.patchValue({ primary: this.item.intentDefinition.primary });
    if (this.item.main) {
      this.form.get('primary').disable();
    }

    if (this.item.intentDefinition?.sentences?.length) {
      this.item.intentDefinition?.sentences.forEach((sentence) => {
        const clone = JSON.parse(JSON.stringify(sentence));
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
    let sentenceCopy = new TempSentence(
      app.namespace,
      app.name,
      language,
      data.sentence.text,
      false,
      ''
    );
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
    primary: new FormControl(false)
  });

  get sentences(): FormArray {
    return this.form.get('sentences') as FormArray;
  }
  get contextsEntities(): FormArray {
    return this.form.get('contextsEntities') as FormArray;
  }

  get primary(): FormControl {
    return this.form.get('primary') as FormControl;
  }

  dissociateIntent() {
    delete this.item.intentDefinition;
    this.cancel();
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
        const newSentence = new TempSentence(
          app.namespace,
          app.name,
          language,
          sentenceString,
          false,
          ''
        );
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
    if (entities.some((e) => e.entityTypeName === entity.entityTypeName && e.role === entity.role))
      return;
    entities.push(entity);
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
