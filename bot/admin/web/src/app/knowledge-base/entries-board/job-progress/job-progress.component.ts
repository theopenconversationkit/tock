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

import { Component, Input } from '@angular/core';

import { KnowledgeBaseJob, KnowledgeBaseJobState } from '../../models';

/** Progress of a batch job, on the same principle as the dataset run follow up. */
@Component({
  selector: 'tock-knowledge-base-job-progress',
  templateUrl: './job-progress.component.html',
  styleUrl: './job-progress.component.scss',
  standalone: false
})
export class KnowledgeBaseJobProgressComponent {
  @Input() job: KnowledgeBaseJob;

  JobState = KnowledgeBaseJobState;

  get running(): boolean {
    return this.job?.state === KnowledgeBaseJobState.QUEUED || this.job?.state === KnowledgeBaseJobState.RUNNING;
  }

  get percent(): number {
    if (!this.job?.progress.total) return 0;
    return Math.round((this.job.progress.done / this.job.progress.total) * 100);
  }

  get status(): string {
    if (this.job?.state === KnowledgeBaseJobState.FAILED) return 'danger';
    if (this.job?.failures.length) return 'warning';
    return 'primary';
  }

  /** i18n key describing what this job is doing, so the user knows what is running. */
  get labelKey(): string {
    return `knowledge-base.job.type_${this.job.type.toLowerCase()}`;
  }
}
