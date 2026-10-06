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
import { Component, Input, OnChanges } from '@angular/core';

import {
  RagAnswerStatus,
  RagAnswerStatusDisplayOrder,
  RagAnswerStatusIcons,
  RagAnswerStatusLabels
} from '../../../shared/utils/dialog.utils';
import { DashboardAnswerOutcome, WidgetState } from '../../models/dashboard.model';

interface OutcomeRow {
  status: RagAnswerStatus;
  label: string;
  icon: string;
  count: number;
  share: number;
}

@Component({
  selector: 'tock-answer-outcome',
  templateUrl: './answer-outcome.component.html',
  styleUrls: ['./answer-outcome.component.scss'],
  standalone: false
})
export class AnswerOutcomeComponent implements OnChanges {
  @Input() outcome: DashboardAnswerOutcome;
  @Input() state: WidgetState = WidgetState.loading;

  WidgetState = WidgetState;
  RagAnswerStatus = RagAnswerStatus;

  expanded: boolean = false;

  rows: OutcomeRow[] = [];
  total: number = 0;
  found: number = 0;
  notFound: number = 0;
  groundedRate: number = 0;
  delta: number | null = null;

  ngOnChanges(): void {
    if (!this.outcome?.counts) return;

    const counts = this.outcome.counts;
    this.total = RagAnswerStatusDisplayOrder.reduce((sum, status) => sum + (counts[status] ?? 0), 0);
    this.found = counts[RagAnswerStatus.FOUND_IN_CONTEXT] ?? 0;
    this.notFound = counts[RagAnswerStatus.NOT_FOUND_IN_CONTEXT] ?? 0;

    // Deliberately excludes small talk, out of scope and escalation: those are not
    // retrieval failures, and including them would dilute the signal.
    const knowledgeAnswers = this.found + this.notFound;
    this.groundedRate = knowledgeAnswers ? this.found / knowledgeAnswers : 0;

    this.rows = RagAnswerStatusDisplayOrder.map((status) => ({
      status,
      label: RagAnswerStatusLabels[status],
      icon: RagAnswerStatusIcons[status],
      count: counts[status] ?? 0,
      share: this.total ? (counts[status] ?? 0) / this.total : 0
    }));

    this.delta = this.computeDelta();
  }

  private computeDelta(): number | null {
    const previous = this.outcome?.previousCounts;
    if (!previous) return null;

    const previousFound = previous[RagAnswerStatus.FOUND_IN_CONTEXT] ?? 0;
    const previousNotFound = previous[RagAnswerStatus.NOT_FOUND_IN_CONTEXT] ?? 0;
    const previousTotal = previousFound + previousNotFound;
    if (!previousTotal) return null;

    const previousRate = previousFound / previousTotal;
    return previousRate ? (this.groundedRate - previousRate) / previousRate : null;
  }

  toggleDetail(): void {
    this.expanded = !this.expanded;
  }
}
