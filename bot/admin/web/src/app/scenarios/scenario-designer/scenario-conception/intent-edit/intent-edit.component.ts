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

  constructor(
    public dialogRef: NbDialogRef<IntentEditComponent>,
    protected state: StateService,
    private nbMenuService: NbMenuService,
    private dialogService: DialogService
  ) {}

  _sentences: Sentence[] = [];

  ngOnInit(): void {
    this.form.patchValue(this.item);

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
    this.saveModifications.emit(this.form.value);
  }

  cancel(): void {
    this.dialogRef.close();
  }

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }
}
