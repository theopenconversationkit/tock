import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DoCheck,
  ElementRef,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';
import { SentenceExtended } from '../sentence-training.component';
import { SelectionModel } from '@angular/cdk/collections';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { StateService } from '../../../../core-nlp/state.service';
import { Observable, Subject, lastValueFrom, of, takeUntil } from 'rxjs';
import { Intent, IntentsCategory, nameFromQualifiedName, SentenceStatus } from '../../../../model/nlp';
import { SentenceReviewRequestComponent } from '../sentence-review-request/sentence-review-request.component';
import { Action, SentenceTrainingMode } from '../models';
import { IntentDialogComponent } from '../../../../language-understanding/intent-dialog/intent-dialog.component';
import { UserRole } from '../../../../model/auth';
import { NlpService } from '../../../../core-nlp/nlp.service';
import { Router } from '@angular/router';
import { truncate } from '../../../../model/commons';
import { getSentenceId } from '../commons/utils';
import { copyToClipboard } from '../../../utils';
import { IntentStoryDetailsComponent } from '../../intent-story-details/intent-story-details.component';
import { ChoiceDialogComponent } from '../../choice-dialog/choice-dialog.component';
import { TestDialogService } from '../../test-dialog/test-dialog.service';
import { KeyValue } from '@angular/common';

@Component({
  selector: 'tock-sentence-training-entry',
  templateUrl: './sentence-training-entry.component.html',
  styleUrls: ['./sentence-training-entry.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SentenceTrainingEntryComponent implements OnInit, DoCheck, OnDestroy {
  private readonly _destroy$: Subject<boolean> = new Subject();

  @Input() standAlone: boolean;
  @Input() sentence: SentenceExtended;
  @Input() sentences: SentenceExtended[] = [];
  @Input() selection: SelectionModel<SentenceExtended>;
  @Input() sentenceTrainingMode: SentenceTrainingMode = SentenceTrainingMode.SEARCH;

  @Output() onDetails = new EventEmitter<SentenceExtended>();
  @Output() onClearSentence = new EventEmitter<SentenceExtended>();

  @ViewChild('sentenceIntentInput') sentenceIntentInput: ElementRef;

  SentenceTrainingMode = SentenceTrainingMode;

  UserRole: typeof UserRole = UserRole;

  intentGroups: IntentsCategory[];
  filteredIntentGroups: Observable<IntentsCategory[]>;

  Action: typeof Action = Action;

  getSentenceId = getSentenceId;

  constructor(
    public state: StateService,
    private nlp: NlpService,
    private router: Router,
    private nbDialogService: NbDialogService,
    private toastrService: NbToastrService,
    private cd: ChangeDetectorRef,
    private testDialogService: TestDialogService
  ) {}

  ngOnInit(): void {
    this.state.currentIntentsCategories.pipe(takeUntil(this._destroy$)).subscribe((groups) => {
      this.intentGroups = Object.freeze(groups) as IntentsCategory[];
      this.resetIntentsListFilter();
    });
  }

  ngDoCheck() {
    if (!this.sentence._showDialog) this.cd.markForCheck();
  }

  askForReview(sentence: SentenceExtended) {
    const dialogRef = this.nbDialogService.open(SentenceReviewRequestComponent, {
      context: {
        beforeClassification: sentence._intentBeforeClassification || sentence.classification.intentId,
        reviewComment: sentence.reviewComment
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result && result.status === 'confirm') {
        sentence.forReview = true;
        sentence.reviewComment = result.description;
        this.handleAction(Action.VALIDATE, sentence);
      }
    });
  }

  async handleAction(action: Action, sentence: SentenceExtended): Promise<void> {
    const actionTitle = this.getActionTitle(action);

    this.setSentenceAccordingToAction(action, sentence);

    await lastValueFrom(this.nlp.updateSentence(sentence));

    // delete old sentence when language change
    if (sentence.language !== this.state.currentLocale) {
      const s = sentence.clone();
      s.language = this.state.currentLocale;
      s.status = SentenceStatus.deleted;
      this.nlp.updateSentence(s).subscribe((_) => {
        this.toastrService.success(`Language change to ${this.state.localeName(sentence.language)}`, 'Language change');
      });
    }

    if (this.selection?.isSelected(sentence)) {
      this.selection.deselect(sentence);
    }

    this.onClearSentence.emit(sentence);

    this.toastrService.success(truncate(sentence.text), actionTitle, {
      duration: 2000,
      status: 'basic'
    });

    this.cd.markForCheck();
  }

  private getActionTitle(action: Action): string {
    switch (action) {
      case Action.DELETE:
        return 'Delete';
      case Action.UNKNOWN:
        return 'Unknown';
      case Action.RAGEXCLUDED:
        return 'Rag excluded';
      case Action.VALIDATE:
        return 'Validate';
    }
  }

  private setSentenceAccordingToAction(action: Action, sentence: SentenceExtended): void {
    switch (action) {
      case Action.DELETE:
        sentence.status = SentenceStatus.deleted;
        break;
      case Action.UNKNOWN:
        sentence.classification.intentId = Intent.unknown;
        sentence.classification.entities = [];
        sentence.status = SentenceStatus.validated;
        break;
      case Action.RAGEXCLUDED:
        sentence.classification.intentId = Intent.ragExcluded;
        sentence.classification.entities = [];
        sentence.status = SentenceStatus.validated;
        break;
      case Action.VALIDATE:
        const intentId = sentence.classification.intentId;

        if (!intentId) {
          this.toastrService.show(`Please select an intent first`);
          break;
        }
        if (intentId === Intent.unknown) {
          sentence.classification.intentId = Intent.unknown;
          sentence.classification.entities = [];
        }
        sentence.status = SentenceStatus.validated;
        break;
    }
  }

  addIntentToSentence(intentId: string): void {
    const isSelected = this.selection?.isSelected(this.sentence);
    this.selection?.deselect(this.sentence);

    let originalIndex = this.sentences.findIndex((s) => s === this.sentence);
    let intentBeforeClassification = this.sentence._intentBeforeClassification || this.sentence.classification.intentId;
    const newSentence = this.sentence.withIntent(this.state, intentId) as SentenceExtended;
    newSentence._intentBeforeClassification = intentBeforeClassification;

    this.sentence = newSentence;

    this.sentenceIntentInput.nativeElement.value = '';

    this.sentences.splice(originalIndex, 1, newSentence);
    if (isSelected) this.selection.select(newSentence);

    this.resetIntentsListFilter();
  }

  isIntentStorySearchable() {
    return this.sentence.classification.intentId !== Intent.unknown && this.sentence.classification.intentId !== Intent.ragExcluded;
  }

  originalOrder = (a: KeyValue<string, number>, b: KeyValue<string, number>): number => {
    return 0;
  };

  displayIntentStoryDetails() {
    const modal = this.nbDialogService.open(IntentStoryDetailsComponent, {
      context: {
        intentId: this.sentence.classification.intentId
      }
    });
  }

  resetIntentsListFilter(): void {
    this.filteredIntentGroups = of(this.intentGroups);
  }

  filterIntentsList(event: any): void {
    if (['ArrowDown', 'ArrowUp', 'Escape'].includes(event.key)) return;

    let str = event.target.value.toLowerCase();
    let result: IntentsCategory[] = [];
    this.intentGroups.forEach((group) => {
      group.intents.forEach((intent) => {
        if (intent.label?.toLowerCase().includes(str) || intent.name?.toLowerCase().includes(str)) {
          let cat = result.find((cat) => cat.category == group.category);
          if (!cat) {
            cat = { category: group.category, intents: [] };
            result.push(cat);
          }
          cat.intents.push(intent);
        }
      });
    });
    this.filteredIntentGroups = of(result);
  }

  onFocus(): void {
    this.resetIntentsListFilter();
  }
  onBlur(event: any): void {
    event.target.value = '';
  }

  swapStatsDetails(sentence: SentenceExtended): void {
    sentence._showStatsDetails = !sentence._showStatsDetails;
  }

  getSentenceAttribut(sentence: SentenceExtended, category: 'intentLabel' | 'probability'): string | number {
    switch (category) {
      case 'intentLabel':
        return sentence.getIntentLabel(this.state);
      case 'probability':
        return sentence.classification.intentProbability;
    }
  }

  isSentenceSelected(sentence: SentenceExtended): boolean {
    return this.selection?.isSelected(sentence);
  }

  toggle(sentence: SentenceExtended): void {
    this.selection?.toggle(sentence);
  }

  showDetails(sentence: SentenceExtended): void {
    this.onDetails.emit(sentence);
  }

  async copySentence(sentence) {
    copyToClipboard(sentence.getText());
    this.toastrService.success(`Sentence copied to clipboard`, 'Clipboard');
  }

  testDialogSentence(sentence) {
    this.testDialogService.testSentenceDialog({
      sentenceText: sentence.text,
      sentenceLocale: sentence.language
    });
  }

  redirectToFaqManagement(sentence: SentenceExtended): void {
    this.router.navigate(['faq/management'], { state: { question: sentence.text } });
  }

  createNewIntent(sentence: SentenceExtended) {
    const dialogRef = this.nbDialogService.open(IntentDialogComponent, { context: { create: true } });

    dialogRef.onClose.subscribe((result) => {
      if (result && result.name) {
        this.createIntent(sentence, result.name, result.label, result.description, result.category);
      }
    });
  }

  private createIntent(sentence: SentenceExtended, name: string, label: string, description: string, category: string): void {
    if (
      StateService.intentExistsInApp(this.state.currentApplication, name) ||
      name === nameFromQualifiedName(Intent.unknown) ||
      name === nameFromQualifiedName(Intent.ragExcluded)
    ) {
      this.toastrService.warning(`Intent ${name} already exists`);
    } else {
      if (this.state.intentExistsInOtherApplication(name)) {
        const action = 'confirm';
        const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
          context: {
            title: 'This intent is already used in an other application',
            subtitle: 'If you confirm the name, the intent will be shared between the two applications.',
            actions: [
              { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
              { actionName: action, buttonStatus: 'danger' }
            ]
          }
        });
        dialogRef.onClose.subscribe((result) => {
          if (result === action) {
            this.saveIntent(sentence, name, label, description, category);
          }
        });
      } else {
        this.saveIntent(sentence, name, label, description, category);
      }
    }
  }

  private saveIntent(sentence: SentenceExtended, name: string, label: string, description: string, category: string) {
    this.nlp
      .saveIntent(
        new Intent(name, this.state.user.organization, [], [this.state.currentApplication._id], [], [], label, description, category)
      )
      .subscribe({
        next: (intent) => {
          this.state.addIntent(intent);

          sentence.classification.intentId = intent._id;
          const oldSentenceIndex = this.sentences.findIndex((s) => s === sentence);
          const oldSentence = sentence;

          const newSentence = oldSentence.clone();
          newSentence.classification.intentId = intent._id;
          newSentence.classification.entities = oldSentence.classification.entities.filter(
            (e) => intent && intent.containsEntity(e.type, e.role)
          );

          this.sentence = newSentence;
          this.sentences.splice(oldSentenceIndex, 1, newSentence);
          this.cd.markForCheck();
        },
        error: () => {
          this.toastrService.warning(`Error on intent creation`);
        }
      });
  }

  ngOnDestroy(): void {
    this._destroy$.next(true);
    this._destroy$.complete();
  }
}
