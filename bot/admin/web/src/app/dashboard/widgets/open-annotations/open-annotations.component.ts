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
import { Component, Input, OnChanges, OnDestroy, OnInit, inject } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { Subject, combineLatest, takeUntil } from 'rxjs';

import { OpenAnnotations, WidgetState } from '../../models/dashboard.model';
import { AnnotationState } from '../../../shared/components/annotation/annotations';
import { ResponseIssueReason } from '../../../shared/model/response-issue';

/** Row ready to render: enum values already resolved to translated labels and a badge status. */
interface OpenAnnotationView {
  dialogId: string;
  actionId: string;
  question?: string | null;
  actionDate: string;
  stateLabel: string;
  stateStatus: 'danger' | 'warning';
  reasonLabel: string;
}

@Component({
  selector: 'tock-open-annotations',
  templateUrl: './open-annotations.component.html',
  styleUrls: ['./open-annotations.component.scss'],
  standalone: false
})
export class OpenAnnotationsComponent implements OnInit, OnChanges, OnDestroy {
  private readonly transloco = inject(TranslocoService);
  private readonly destroy$ = new Subject<void>();

  @Input() data: OpenAnnotations = { total: 0, rows: [] };
  @Input() state: WidgetState = WidgetState.loading;
  @Input() namespace: string;
  @Input() applicationId: string;

  WidgetState = WidgetState;
  rows: OpenAnnotationView[] = [];

  // Same i18n keys and value→key mapping as AnnotationComponent / generate-sample-report.
  private stateLabels: Partial<Record<AnnotationState, string>> = {};
  private reasonLabels: Partial<Record<ResponseIssueReason, string>> = {};

  ngOnInit(): void {
    combineLatest([
      this.transloco.selectTranslateObject('shared.annotation.states', {}, ''),
      this.transloco.selectTranslateObject('common.responseIssueReasons', {}, '')
    ])
      .pipe(takeUntil(this.destroy$))
      .subscribe(([states, reasons]) => {
        this.stateLabels = {
          [AnnotationState.ANOMALY]: states.opened,
          [AnnotationState.REVIEW_NEEDED]: states.reviewNeeded,
          [AnnotationState.RESOLVED]: states.resolved,
          [AnnotationState.WONT_FIX]: states.wontFix
        };
        this.reasonLabels = {
          [ResponseIssueReason.QUESTION_MISUNDERSTOOD]: reasons.questionNotOrMisunderstood,
          [ResponseIssueReason.INACCURATE_ANSWER]: reasons.inaccurateAnswer,
          [ResponseIssueReason.INCOMPLETE_ANSWER]: reasons.incompleteAnswer,
          [ResponseIssueReason.INCOMPLETE_SOURCES]: reasons.incompleteSourcesOrDocuments,
          [ResponseIssueReason.OBSOLETE_SOURCES]: reasons.obsoleteSourcesOrDocuments,
          [ResponseIssueReason.BUSINESS_LEXICON_PROBLEM]: reasons.businessLexiconProblem,
          [ResponseIssueReason.WRONG_ANSWER_FORMAT]: reasons.wrongAnswerFormat,
          [ResponseIssueReason.HALLUCINATION]: reasons.hallucination,
          [ResponseIssueReason.OTHER]: reasons.other
        };
        this.buildRows();
      });
  }

  ngOnChanges(): void {
    this.buildRows();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private buildRows(): void {
    this.rows = (this.data?.rows ?? []).map((row) => ({
      dialogId: row.dialogId,
      actionId: row.actionId,
      question: row.question,
      actionDate: row.actionDate,
      stateLabel: this.stateLabels[row.annotation.state] ?? row.annotation.state,
      stateStatus: row.annotation.state === AnnotationState.ANOMALY ? 'danger' : 'warning',
      reasonLabel: this.reasonLabels[row.annotation.reason] ?? row.annotation.reason
    }));
  }
}
