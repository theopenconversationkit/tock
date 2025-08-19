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

import { Component, Input, OnInit } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { MetricResult } from '../../models';
import { RagAnswerStatusLabels, snakeCaseToDisplayLabel } from '../../../shared/utils';

enum SortingCriteria {
  name,
  count
}

@Component({
  selector: 'tock-metrics-indicator-details',
  templateUrl: './metrics-indicator-details.component.html',
  styleUrls: ['./metrics-indicator-details.component.scss']
})
export class MetricsIndicatorDetailsComponent implements OnInit {
  @Input() indicatorName: string;
  @Input() metrics: MetricResult[];

  SortingCriteria = SortingCriteria;
  indicatorLabel: string;

  constructor(public dialogRef: NbDialogRef<MetricsIndicatorDetailsComponent>) {}

  ngOnInit(): void {
    this.indicatorLabel = snakeCaseToDisplayLabel(this.indicatorName);
    this.sortMetrics();
  }

  sortCriteria: SortingCriteria = SortingCriteria.count;
  sortDirection: boolean = true;

  sortBy(criteria: SortingCriteria): void {
    if (this.sortCriteria === criteria) {
      this.sortDirection = !this.sortDirection;
    } else {
      this.sortCriteria = criteria;
    }
    this.sortMetrics();
  }

  sortMetrics(): void {
    if (this.sortCriteria === SortingCriteria.count) {
      this.metrics.sort((a, b) => {
        if (this.sortDirection) return b.count - a.count;
        else return a.count - b.count;
      });
    }

    if (this.sortCriteria === SortingCriteria.name) {
      this.metrics.sort((a, b) => {
        if (this.sortDirection) return a.row.indicatorValueName.localeCompare(b.row.indicatorValueName);
        else return b.row.indicatorValueName.localeCompare(a.row.indicatorValueName);
      });
    }
  }

  getIndicatorValueLabel(indicatorValueName: string): string {
    return RagAnswerStatusLabels[indicatorValueName.toLowerCase()] || snakeCaseToDisplayLabel(indicatorValueName);
  }

  getSum(): number {
    return this.metrics.reduce((sum, metric) => sum + metric.count, 0);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
