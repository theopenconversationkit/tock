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
import { Component, EventEmitter, Input, Output } from '@angular/core';

import { StateService } from '../../../core-nlp/state.service';
import { UserRole } from '../../../model/auth';
import { IngestionNotes, KnowledgeIndex, WidgetState } from '../../models/dashboard.model';
import { daysSince } from '../../dashboard.utils';

/** Beyond this age the index is flagged as possibly outdated. */
const STALE_INDEX_THRESHOLD_DAYS = 30;

@Component({
  selector: 'tock-knowledge-index',
  templateUrl: './knowledge-index.component.html',
  styleUrls: ['./knowledge-index.component.scss'],
  standalone: false
})
export class KnowledgeIndexComponent {
  @Input() index: KnowledgeIndex;
  @Input() notes: IngestionNotes;
  @Input() state: WidgetState = WidgetState.loading;

  @Output() onEditNotes = new EventEmitter<void>();

  WidgetState = WidgetState;

  constructor(public state$: StateService) {}

  get canEdit(): boolean {
    return this.state$.hasRole(UserRole.admin);
  }

  get ageInDays(): number | null {
    return this.index?.indexDatetime ? daysSince(this.index.indexDatetime) : null;
  }

  get isStale(): boolean {
    const age = this.ageInDays;
    return age !== null && age > STALE_INDEX_THRESHOLD_DAYS;
  }

  get hasNotes(): boolean {
    return !!this.notes?.text?.trim().length;
  }

  /** Notes are fetched after the index resolves, so they lag one request behind. */
  get notesLoaded(): boolean {
    return !!this.notes;
  }

  editNotes(): void {
    this.onEditNotes.emit();
  }
}
