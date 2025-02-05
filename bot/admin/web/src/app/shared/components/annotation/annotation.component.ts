import { Component, Input, OnInit } from '@angular/core';
import { ActionReport, Debug, DialogReport, Sentence, SentenceWithFootnotes } from '../../model/dialog-data';
import { NbDialogRef, NbToastrService } from '@nebular/theme';
import {
  Annotation,
  AnnotationEvent,
  AnnotationEventType,
  AnnotationEventTypes,
  AnnotationReason,
  AnnotationReasons,
  AnnotationState,
  AnnotationStates
} from './annotations';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { FormType, G } from 'ngx-mf';
import { RestService } from '../../../core-nlp/rest/rest.service';
import { StateService } from '../../../core-nlp/state.service';
import { deepCopy } from '../../utils';

type AnnotationForm = FormType<Omit<Annotation, '_id' | 'user' | 'createdAt' | 'lastUpdateDate' | 'expiresAt'> & { comment: string }>;
type AnnotationFormGroupKeysType = AnnotationForm[G];

@Component({
  selector: 'tock-annotation',
  templateUrl: './annotation.component.html',
  styleUrl: './annotation.component.scss'
})
export class AnnotationComponent implements OnInit {
  loading: boolean = true;

  annotationStates = AnnotationStates;

  annotationReasons = AnnotationReasons;

  annotationEventType = AnnotationEventType;
  annotationEventTypes = AnnotationEventTypes;

  @Input() dialogReport: DialogReport;
  @Input() actionReport: ActionReport;

  isSubmitted: boolean = false;

  question: string;
  condensedQuestion: string;
  answer: string;

  constructor(
    private dialogRef: NbDialogRef<AnnotationComponent>,
    private rest: RestService,
    private stateService: StateService,
    private toastrService: NbToastrService
  ) {}

  ngOnInit(): void {
    this.setExchangeInfos();

    if (this.actionReport.annotation) {
      this.form.patchValue(this.actionReport.annotation);
    }

    this.loading = false;
  }

  setExchangeInfos() {
    this.answer = (this.actionReport.message as unknown as Sentence).text;

    const actionsStack = this.dialogReport.actions;
    let actionIndex = actionsStack.findIndex((act) => act === this.actionReport);

    if (actionIndex > 0) {
      actionIndex--;
      let questionAction = actionsStack[actionIndex];

      let question;
      let condensedQuestion;

      while (!question && questionAction) {
        if (questionAction.message.isDebug()) {
          const actionDebug = questionAction.message as unknown as Debug;
          if (actionDebug.data.condensed_question) {
            condensedQuestion = actionDebug.data.condensed_question;
          }
          actionIndex--;
          questionAction = actionsStack[actionIndex];
        } else if (!questionAction.isBot()) {
          const questionSentence = questionAction.message as unknown as Sentence;
          question = questionSentence.text;
        }
      }

      if (condensedQuestion) {
        this.condensedQuestion = condensedQuestion;
      }
      if (question) {
        this.question = question;
      }
    }
  }

  hideChangeEvents: boolean = false;

  eventsSortingDirection: 'asc' | 'desc' = 'desc';

  toggleEventsSortingDirection(): void {
    if (this.eventsSortingDirection === 'asc') {
      this.eventsSortingDirection = 'desc';
    } else {
      this.eventsSortingDirection = 'asc';
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

      if (this.eventsSortingDirection === 'desc') {
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

  getReasonLabel(reason: AnnotationReason): string {
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
    if (!this.actionReport.annotation?._id) return 14;
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
        this.postOrPut();
      } else {
        this.postComment();
      }
    }
  }

  getAnnotationBaseUrl(): string {
    return `/bots/${this.stateService.currentApplication.name}/dialogs/${this.dialogReport.id}/actions/${this.actionReport.id}/annotation`;
  }

  postOrPut(): void {
    const formValue: any = deepCopy(this.form.value);
    delete formValue['comment'];
    delete formValue['events'];

    let url = this.getAnnotationBaseUrl();
    let method = this.rest.post(url, formValue);

    if (this.actionReport.annotation?._id) {
      url += `/${this.actionReport.annotation._id}`;
      method = this.rest.put(url, formValue);
    }

    method.subscribe({
      next: (annotation: Annotation) => {
        this.actionReport.annotation = annotation;
        this.form.markAsPristine();

        this.loading = false;

        this.postComment();
      },
      error: (error) => {
        this.toastrService.danger('An error occured', 'Error', {
          duration: 5000,
          status: 'danger'
        });
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

      const url = `${this.getAnnotationBaseUrl()}/${this.actionReport.annotation._id}/events`;

      this.loading = true;

      this.rest.post(url, payload).subscribe({
        next: (event: AnnotationEvent) => {
          this.actionReport.annotation.events.push(event);

          this.form.get('comment').reset();

          this.loading = false;
        },
        error: (error) => {
          this.toastrService.danger('An error occured', 'Error', {
            duration: 5000,
            status: 'danger'
          });
          this.loading = false;
        }
      });
    }
  }

  deleteComment(event: AnnotationEvent): void {
    const url = `${this.getAnnotationBaseUrl()}/${this.actionReport.annotation._id}/events/${event.eventId}`;

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

    const url = `${this.getAnnotationBaseUrl()}/${this.actionReport.annotation._id}/events/${event.eventId}`;

    this.loading = true;

    this.rest.put(url, modifiedEvent).subscribe({
      next: (event: AnnotationEvent) => {
        this.actionReport.annotation.events = this.actionReport.annotation.events.map((e) => (e.eventId === event.eventId ? event : e));

        this.loading = false;
      },
      error: (error) => {
        this.toastrService.danger('An error occured', 'Error', {
          duration: 5000,
          status: 'danger'
        });
        this.loading = false;
      }
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
