/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { ConnectorType } from '../../../../core/model/configuration';
import { Subject, debounceTime, take, takeUntil } from 'rxjs';
import { ExtractFormControlTyping } from '../../../../shared/utils/typescript.utils';
import { BotSharedService } from '../../../../shared/bot-shared.service';
import { StateService } from '../../../../core-nlp/state.service';
import { BotConfigurationService } from '../../../../core/bot-configuration.service';
import { RestService } from '../../../../core-nlp/rest/rest.service';
import { AnnotationState, AnnotationStates } from '../../../../shared/components/annotation/annotations';
import { SortOrder, SortOrders } from '../../../../shared/model/misc';
import { FeedbackVote } from '../../dialogs';

export const FeedbackVotes = [
  { labelKey: 'common.feedback.positive', value: FeedbackVote.UP },
  { labelKey: 'common.feedback.negative', value: FeedbackVote.DOWN }
] as const;

import { ResponseIssueReason, ResponseIssueReasons } from '../../../../shared/model/response-issue';
import { RagAnswerStatus, RagAnswerStatusDisplayOrder, RagAnswerStatusIcons, RagAnswerStatusLabels } from '../../../../shared/utils';
import { TranslocoService } from '@jsverse/transloco';

interface DialogListFiltersForm {
  exactMatch: FormControl<boolean>;
  displayTests: FormControl<boolean>;
  dialogId?: FormControl<string>;
  text?: FormControl<string>;
  intentName?: FormControl<string>;
  connectorType?: FormControl<ConnectorType>;
  ratings?: FormControl<number[]>;
  configuration?: FormControl<string>;
  intentsToHide?: FormControl<string[]>;
  isGenAiRagDialog?: FormControl<boolean>;
  dialogSort?: FormControl<SortOrder>;
  dialogActivityFrom?: FormControl<Date>;
  dialogActivityTo?: FormControl<Date>;
  withAnnotations?: FormControl<boolean>;
  annotationStates?: FormControl<AnnotationState[]>;
  annotationReasons?: FormControl<ResponseIssueReason[]>;
  annotationSort?: FormControl<SortOrder>;
  annotationCreationDateFrom?: FormControl<Date>;
  annotationCreationDateTo?: FormControl<Date>;
  feedback?: FormControl<FeedbackVote>;
  ragAnswerStatus?: FormControl<string>;
}

export type DialogListFilters = ExtractFormControlTyping<DialogListFiltersForm>;

@Component({
    selector: 'tock-dialogs-list-filters',
    templateUrl: './dialogs-list-filters.component.html',
    styleUrl: './dialogs-list-filters.component.scss',
    standalone: false
})
export class DialogsListFiltersComponent implements OnInit, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();
  private lastEmittedValue: Partial<DialogListFilters> | null = null;

  advanced: boolean = false;
  connectorTypes: ConnectorType[] = [];
  configurationNameList: { label: string; applicationId: string }[];

  annotationStates = AnnotationStates;
  annotationReasons = ResponseIssueReasons;
  sortOrders = SortOrders;
  feedbackVotes = FeedbackVotes;
  ragAnswerStatusLabels = RagAnswerStatusLabels;
  ragAnswerStatusIcons = RagAnswerStatusIcons;
  ragAnswerStatusDisplayOrder = RagAnswerStatusDisplayOrder;

  @Input() initialFilters: Partial<DialogListFilters>;
  @Output() onFilter = new EventEmitter<Partial<DialogListFilters>>();

  constructor(
    public botSharedService: BotSharedService,
    public state: StateService,
    private botConfiguration: BotConfigurationService,
    private transloco: TranslocoService
  ) {}

  ngOnInit() {
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
      .selectTranslateObject('common.ragAnswerStatus', {}, '')
      .pipe(takeUntil(this.destroy$))
      .subscribe((translatedRanges) => {
        this.ragAnswerStatusLabels = {
          [RagAnswerStatus.FOUND_IN_CONTEXT]: translatedRanges.foundInContext,
          [RagAnswerStatus.NOT_FOUND_IN_CONTEXT]: translatedRanges.notFoundInContext,
          [RagAnswerStatus.SMALL_TALK]: translatedRanges.smallTalk,
          [RagAnswerStatus.HUMAN_ESCALATION]: translatedRanges.humanEscalation,
          [RagAnswerStatus.OUT_OF_SCOPE]: translatedRanges.outOfScope,
          [RagAnswerStatus.INJECTION_ATTEMPT]: translatedRanges.injectionAttempt,
          [RagAnswerStatus.TECHNICAL_ERROR]: translatedRanges.technicalError
        };
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

    this.botSharedService
      .getConnectorTypes()
      .pipe(take(1))
      .subscribe((conf) => {
        this.connectorTypes = conf.map((it) => it.connectorType);
      });

    this.botConfiguration.configurations.pipe(takeUntil(this.destroy$)).subscribe((configs) => {
      this.configurationNameList = configs
        .filter((item) => item.targetConfigurationId == null)
        .map((item) => {
          const label = `${item.name} > ${item.connectorType.label()} (${item.applicationId})`;
          return { label: label, applicationId: item.applicationId };
        });
    });

    this.lastEmittedValue = { ...this.form.value };

    if (this.initialFilters) {
      this.form.patchValue(this.initialFilters);
      this.lastEmittedValue = { ...this.form.value };
    }

    this.form.valueChanges.pipe(debounceTime(500), takeUntil(this.destroy$)).subscribe(() => {
      this.submitFiltersChange();
      this.persisteDisplayTests();
    });
  }

  form = new FormGroup<DialogListFiltersForm>({
    exactMatch: new FormControl(),
    displayTests: new FormControl(),
    dialogId: new FormControl(),
    text: new FormControl(),
    intentName: new FormControl(),
    connectorType: new FormControl(),
    ratings: new FormControl(),
    configuration: new FormControl(),
    intentsToHide: new FormControl([]),
    isGenAiRagDialog: new FormControl(),
    dialogSort: new FormControl(),
    dialogActivityFrom: new FormControl(),
    dialogActivityTo: new FormControl(),
    withAnnotations: new FormControl(),
    annotationStates: new FormControl([]),
    annotationReasons: new FormControl([]),
    annotationSort: new FormControl(),
    annotationCreationDateFrom: new FormControl(),
    annotationCreationDateTo: new FormControl(),
    feedback: new FormControl(),
    ragAnswerStatus: new FormControl()
  });

  getFormControl(formControlName: string): FormControl {
    return this.form.get(formControlName) as FormControl;
  }

  submitFiltersChange(): void {
    const formValue = this.form.value;
    this.onFilter.emit(formValue);
  }

  persisteDisplayTests(): void {
    const displayTests = this.getFormControl('displayTests')?.value;
    this.botSharedService.session_storage = {
      ...this.botSharedService.session_storage,
      ...{ dialogs: { ...this.botSharedService.session_storage?.dialogs, displayTests } }
    };
  }

  resetControl(ctrl: FormControl, input?: HTMLInputElement): void {
    ctrl.reset();
    if (input) {
      input.value = '';
    }
  }

  patchControl(ctrl: FormControl, value: boolean): void {
    ctrl.patchValue(value);
  }

  swapAdvanced(): void {
    this.advanced = !this.advanced;
  }

  getConnectorTypeIconById(connectorId: string): string {
    if (connectorId === null) connectorId = 'web';
    return RestService.connectorIconUrl(connectorId);
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
