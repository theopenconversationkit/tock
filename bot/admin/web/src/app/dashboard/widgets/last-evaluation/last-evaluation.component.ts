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

import { EvaluationSampleDefinition } from '../../../quality/samples/models';
import { WidgetState } from '../../models/dashboard.model';
import { daysSince } from '../../dashboard.utils';

/** Beyond this age the most recent validated evaluation is flagged as worth refreshing. */
const STALE_EVALUATION_THRESHOLD_DAYS = 60;

const RING_RADIUS = 26;

export interface EvaluationEntry {
  sample: EvaluationSampleDefinition;
  /** Positive share over what was actually evaluated, not over the whole sample. */
  positiveRate: number;
  /** Variation against the evaluation that preceded it, null for the oldest one shown. */
  delta: number | null;
  ageInDays: number;
  /** Only the most recent one can be flagged as outdated. */
  isStale: boolean;
  ringOffset: number;
}

@Component({
  selector: 'tock-last-evaluation',
  templateUrl: './last-evaluation.component.html',
  styleUrls: ['./last-evaluation.component.scss'],
  standalone: false
})
export class LastEvaluationComponent implements OnChanges {
  /** Validated samples only, most recent first. */
  @Input() evaluations: EvaluationSampleDefinition[] = [];
  @Input() state: WidgetState = WidgetState.loading;

  WidgetState = WidgetState;

  entries: EvaluationEntry[] = [];

  readonly ringRadius = RING_RADIUS;
  readonly ringCircumference = 2 * Math.PI * RING_RADIUS;

  ngOnChanges(): void {
    this.entries = (this.evaluations ?? []).map((sample, index, all) => {
      const positiveRate = this.rateOf(sample);
      const ageInDays = sample.statusChangeDate ? daysSince(sample.statusChangeDate) : 0;

      return {
        sample,
        positiveRate,
        delta: this.deltaBetween(positiveRate, all[index + 1]),
        ageInDays,
        isStale: index === 0 && ageInDays > STALE_EVALUATION_THRESHOLD_DAYS,
        ringOffset: this.ringCircumference * (1 - positiveRate)
      };
    });
  }

  private rateOf(sample: EvaluationSampleDefinition): number {
    const result = sample?.evaluationsResult;
    return result?.evaluated ? result.positiveCount / result.evaluated : 0;
  }

  private deltaBetween(rate: number, older: EvaluationSampleDefinition): number | null {
    if (!older) return null;
    const olderRate = this.rateOf(older);
    return olderRate ? (rate - olderRate) / olderRate : null;
  }
}
