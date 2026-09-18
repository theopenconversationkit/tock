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
import { KnowledgeBaseIndexMode, KnowledgeBaseSyncStatus } from '../../models';

type BannerStatus = 'basic' | 'info' | 'success' | 'warning' | 'danger';

@Component({
  selector: 'tock-knowledge-base-sync-banner',
  templateUrl: './sync-banner.component.html',
  styleUrl: './sync-banner.component.scss',
  standalone: false
})
export class KnowledgeBaseSyncBannerComponent {
  @Input() syncStatus: KnowledgeBaseSyncStatus;
  @Input() busy: boolean = false;

  @Output() onSynchronize = new EventEmitter<void>();
  @Output() onCreateIndex = new EventEmitter<void>();
  @Output() onFilterPending = new EventEmitter<void>();
  @Output() onFilterOrphan = new EventEmitter<void>();

  IndexMode = KnowledgeBaseIndexMode;

  /**
   * The banner is a repair affordance, not a validation step: publishing, unpublishing and
   * deleting all take effect immediately. It therefore only shows when something needs
   * attention — no index yet, or a real drift between the knowledge base and the index.
   * Showing a permanent "Synchronize" button would suggest edits are queued, which they are not.
   */
  get visible(): boolean {
    return !!this.syncStatus && (!this.hasIndex || this.outOfSync || this.embeddingWarning);
  }

  get hasIndex(): boolean {
    return !!this.syncStatus && this.syncStatus.indexMode !== KnowledgeBaseIndexMode.NONE;
  }

  get pending(): number {
    return this.syncStatus?.counts.pending ?? 0;
  }

  get orphan(): number {
    return this.syncStatus?.counts.orphan ?? 0;
  }

  get outOfSync(): boolean {
    return this.pending > 0 || this.orphan > 0;
  }

  /** True when entries would be written into an index whose embedding model cannot be verified. */
  get embeddingWarning(): boolean {
    return this.hasIndex && !this.syncStatus.embeddingModelKnown;
  }

  get titleKey(): string {
    if (!this.hasIndex) return 'knowledge-base.sync-banner.no_index_title';
    if (this.outOfSync) return 'knowledge-base.sync-banner.out_of_sync_title';
    return 'knowledge-base.sync-banner.embedding_warning_title';
  }

  get status(): BannerStatus {
    if (!this.hasIndex) return 'info';
    return 'warning';
  }

  get icon(): string {
    if (!this.hasIndex) return 'database-add';
    return 'exclamation-triangle';
  }
}
