import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { ActionReport, DialogReport, Sentence } from '../../model/dialog-data';
import { NbDialogRef, NbToastrService } from '@nebular/theme';
import { Annotation, AnnotationEvent, AnnotationEventType, AnnotationEventTypes, AnnotationState, AnnotationStates } from './annotations';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { FormType, G } from 'ngx-mf';
import { RestService } from '../../../core-nlp/rest/rest.service';
import { StateService } from '../../../core-nlp/state.service';
import { deepCopy } from '../../utils';
import { SortOrder } from '../../model/misc';
import { ResponseIssueReason, ResponseIssueReasons } from '../../model/response-issue';
import { Subject, takeUntil } from 'rxjs';
import { TranslocoService } from '@jsverse/transloco';

type AnnotationForm = FormType<Omit<Annotation, '_id' | 'user' | 'createdAt' | 'lastUpdateDate' | 'expiresAt'> & { comment: string }>;
type AnnotationFormGroupKeysType = AnnotationForm[G];

@Component({
    selector: 'tock-annotation',
    templateUrl: './annotation.component.html',
    styleUrl: './annotation.component.scss',
    standalone: false
})
export class AnnotationComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  loading: boolean = true;

  annotationStates = AnnotationStates;

  annotationReasons = ResponseIssueReasons;

  annotationEventType = AnnotationEventType;
  annotationEventTypes = AnnotationEventTypes;

  @Input() dialogReport: DialogReport;
  @Input() actionReport: ActionReport;
  // The question actually sent, provided by callers (e.g. datasets) whose minimal dialogReport does
  // not contain the preceding user action. Lower priority than ragDebug.user_question.
  @Input() exchangeQuestion?: string | null;

  isSubmitted: boolean = false;

  question: string;
  condensedQuestion: string;
  answer: string;

  sortOrder = SortOrder;

  constructor(
    private dialogRef: NbDialogRef<AnnotationComponent>,
    private rest: RestService,
    private stateService: StateService,
    private toastrService: NbToastrService,
    private transloco: TranslocoService
  ) {}

  ngOnInit(): void {
    this.transloco
      .selectTranslateObject('shared.annotation.states', {}, '')
      .pipe(takeUntil(this.destroy$))
      .subscribe((translatedRanges) => {
        this.annotationStates = [
          { label: translatedRanges.opened, value: AnnotationState.ANOMALY },
          { label: translatedRanges.reviewNeeded, value: AnnotationState.REVIEW_NEEDED },
          { label: translatedRanges.resolved, value: AnnotationState.RESOLVED },
          { label: translatedRanges.wontFix, value: AnnotationState.WONT_FIX }
        ];
      });

    this.transloco
      .selectTranslateObject('common.responseIssueReasons', {}, '')
      .pipe(takeUntil(this.destroy$))
      .subscribe((translatedRanges) => {
        this.annotationReasons = [
          { label: translatedRanges.questionNotOrMisunderstood, value: ResponseIssueReason.QUESTION_MISUNDERSTOOD },
          { label: translatedRanges.inaccurateAnswer, value: ResponseIssueReason.INACCURATE_ANSWER },
          { label: translatedRanges.incompleteAnswer, value: ResponseIssueReason.INCOMPLETE_ANSWER },
          { label: translatedRanges.incompleteSourcesOrDocuments, value: ResponseIssueReason.INCOMPLETE_SOURCES },
          { label: translatedRanges.obsoleteSourcesOrDocuments, value: ResponseIssueReason.OBSOLETE_SOURCES },
          { label: translatedRanges.businessLexiconProblem, value: ResponseIssueReason.BUSINESS_LEXICON_PROBLEM },
          { label: translatedRanges.wrongAnswerFormat, value: ResponseIssueReason.WRONG_ANSWER_FORMAT },
          { label: translatedRanges.hallucination, value: ResponseIssueReason.HALLUCINATION },
          { label: translatedRanges.other, value: ResponseIssueReason.OTHER }
        ];
      });

    this.setExchangeInfos();

    if (this.actionReport.annotation) {
      this.form.patchValue(this.actionReport.annotation);
    }

    this.loading = false;
  }

  setExchangeInfos() {
    this.answer = (this.actionReport.message as unknown as Sentence).text;

    // condensed_question only ever comes from ragDebug.
    if (this.actionReport.ragDebug?.condensed_question) {
      this.condensedQuestion = this.actionReport.ragDebug.condensed_question;
    }

    // Question precedence: ragDebug.user_question (the question as the RAG received it), else the
    // caller-provided exchangeQuestion, else a lookup in the dialog's preceding actions.
    if (this.actionReport.ragDebug?.user_question) {
      this.question = this.actionReport.ragDebug.user_question;
    } else if (this.exchangeQuestion) {
      this.question = this.exchangeQuestion;
    } else {
      const question = this.findPrecedingQuestion();
      if (question) this.question = question;
    }
  }

  // Bounded backward scan for the question: the closest preceding user action's text. Bot actions
  // (debug or not) are skipped; the scan stops at the first non-bot action, taking its text if any.
  private findPrecedingQuestion(): string | undefined {
    const actions = this.dialogReport.actions;
    const answerIndex = actions.findIndex((act) => act === this.actionReport);

    for (let i = answerIndex - 1; i >= 0; i--) {
      const action = actions[i];
      if (action.isBot()) continue;
      return (action.message as unknown as Sentence).text;
    }

    return undefined;
  }

  hideChangeEvents: boolean = false;

  eventsSortingDirection: SortOrder = SortOrder.DESC;

  toggleEventsSortingDirection(): void {
    if (this.eventsSortingDirection === SortOrder.ASC) {
      this.eventsSortingDirection = SortOrder.DESC;
    } else {
      this.eventsSortingDirection = SortOrder.ASC;
    }
  }

  getFilteredEvents(): AnnotationEvent[] {
    let eventsList = this.actionReport.annotation.events;

    if (this.hideChangeEvents) {
      eventsList = this.actionReport.annotation.events.filter((e) => e.type === AnnotationEventType.COMMENT);
    }

    return eventsList.sort((a, b) => {
      const A = new Date(a.creationDate).valueOf();
      const B = new Date(b.creationDate).valueOf();

      if (this.eventsSortingDirection === SortOrder.DESC) {
        return B - A;
      } else {
        return A - B;
      }
    });
  }

  form = new FormGroup<AnnotationFormGroupKeysType>({
    state: new FormControl(AnnotationState.ANOMALY, [Validators.required]),
    reason: new FormControl(undefined),
    description: new FormControl(undefined, [Validators.required]),
    groundTruth: new FormControl(undefined),
    comment: new FormControl(undefined)
  });

  get state(): FormControl {
    return this.form.get('state') as FormControl;
  }
  get description(): FormControl {
    return this.form.get('description') as FormControl;
  }

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  getStateLabel(state: AnnotationState): string {
    return this.annotationStates.find((s) => s.value === state)?.label || state;
  }

  getReasonLabel(reason: ResponseIssueReason): string {
    return this.annotationReasons.find((s) => s.value === reason)?.label || reason;
  }

  getEventTypeLabel(eventType: AnnotationEventType): string {
    return this.annotationEventTypes.find((t) => t.value === eventType)?.label || eventType;
  }

  getBeforeAfterDisplayLabel(eventType: AnnotationEventType, value: any): string {
    if (eventType === AnnotationEventType.STATE) {
      return this.getStateLabel(value);
    }

    if (eventType === AnnotationEventType.REASON) {
      return this.getReasonLabel(value);
    }

    return value;
  }

  getTextAreaNbRows(): number {
    if (!this.actionReport.annotation) return 14;
    return 4;
  }

  submit(): void {
    this.isSubmitted = true;
    if (this.canSave && this.form.dirty) {
      const isFormDirty = Object.keys(this.form.controls).some((key) => {
        if (['comment', 'events'].includes(key)) return false;
        return this.form.get(key).dirty;
      });

      if (isFormDirty) {
        this.post();
      } else {
        this.postComment();
      }
    }
  }

  getAnnotationBaseUrl(): string {
    return `/bots/${this.stateService.currentApplication.name}/dialogs/${this.dialogReport.id}/actions/${this.actionReport.id}/annotation`;
  }

  post(): void {
    const formValue: any = deepCopy(this.form.value);
    delete formValue['comment'];
    delete formValue['events'];

    let url = this.getAnnotationBaseUrl();

    this.rest.post(url, formValue).subscribe({
      next: (annotation: Annotation) => {
        this.actionReport.annotation = annotation;
        this.form.markAsPristine();

        this.loading = false;

        this.postComment();
      },
      error: (error) => {
        this.toastrService.danger(
          this.transloco.translate('common.messages.an-error-occured'),
          this.transloco.translate('common.messages.error'),
          {
            duration: 5000,
            status: 'danger'
          }
        );
        this.loading = false;
      }
    });
  }

  postComment(): void {
    const formValue: any = deepCopy(this.form.value);

    if (formValue.comment?.trim().length) {
      const payload = {
        type: AnnotationEventType.COMMENT,
        comment: formValue.comment
      };

      const url = `${this.getAnnotationBaseUrl()}/events`;

      this.loading = true;

      this.rest.post(url, payload).subscribe({
        next: (event: AnnotationEvent) => {
          this.actionReport.annotation.events.push(event);

          this.form.get('comment').reset();

          this.loading = false;
        },
        error: (error) => {
          this.toastrService.danger(
            this.transloco.translate('common.messages.an-error-occured'),
            this.transloco.translate('common.messages.error'),
            {
              duration: 5000,
              status: 'danger'
            }
          );
          this.loading = false;
        }
      });
    }
  }

  deleteComment(event: AnnotationEvent): void {
    const url = `${this.getAnnotationBaseUrl()}/events/${event.eventId}`;

    this.loading = true;

    this.rest.delete(url).subscribe((res: Boolean) => {
      this.actionReport.annotation.events = this.actionReport.annotation.events.filter((e) => e.eventId !== event.eventId);

      this.loading = false;
    });
  }

  putComment(data: { event: AnnotationEvent; value: string }): void {
    const { event, value } = data;

    const modifiedEvent = deepCopy(event);
    modifiedEvent.comment = value;

    const url = `${this.getAnnotationBaseUrl()}/events/${event.eventId}`;

    this.loading = true;

    this.rest.put(url, modifiedEvent).subscribe({
      next: (event: AnnotationEvent) => {
        this.actionReport.annotation.events = this.actionReport.annotation.events.map((e) => (e.eventId === event.eventId ? event : e));

        this.loading = false;
      },
      error: (error) => {
        this.toastrService.danger(
          this.transloco.translate('common.messages.an-error-occured'),
          this.transloco.translate('common.messages.error'),
          {
            duration: 5000,
            status: 'danger'
          }
        );
        this.loading = false;
      }
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
