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

import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { Router } from '@angular/router';
import { TranslocoService } from '@jsverse/transloco';
import { CreateIndexBlocker, KnowledgeBaseIndexState, KnowledgeBaseSyncStatus } from '../../models';

type BannerStatus = 'basic' | 'info' | 'success' | 'warning' | 'danger';

@Component({
  selector: 'tock-knowledge-base-sync-banner',
  templateUrl: './sync-banner.component.html',
  styleUrl: './sync-banner.component.scss',
  standalone: false
})
export class KnowledgeBaseSyncBannerComponent {
  private router = inject(Router);
  private transloco = inject(TranslocoService);

  @Input() syncStatus: KnowledgeBaseSyncStatus;
  @Input() busy: boolean = false;
  /** Index creation and re-creation are admin only; the banner messages stay visible to everyone. */
  @Input() isAdmin: boolean = false;

  @Output() onSynchronize = new EventEmitter<void>();
  @Output() onCreateIndex = new EventEmitter<void>();
  @Output() onFilterPending = new EventEmitter<void>();
  @Output() onFilterOrphan = new EventEmitter<void>();

  IndexState = KnowledgeBaseIndexState;

  /**
   * The banner is where the index status is surfaced: which state it is in, what it holds, where it
   * came from, and the repair or creation actions that apply. It shows as soon as a status is known.
   * The Repair button still appears only on a real drift, so it never suggests that edits are queued —
   * publishing, unpublishing and deleting all take effect immediately.
   */
  get visible(): boolean {
    return !!this.syncStatus;
  }

  get indexState(): KnowledgeBaseIndexState | null {
    return this.syncStatus?.indexState ?? null;
  }

  get blocker(): CreateIndexBlocker | null {
    return this.syncStatus?.createIndexBlocker ?? null;
  }

  get pending(): number {
    return this.syncStatus?.counts.pending ?? 0;
  }

  get orphan(): number {
    return this.syncStatus?.counts.orphan ?? 0;
  }

  /** A drift only exists against a queryable index, hence the READY guard. */
  get outOfSync(): boolean {
    return (
      this.indexState === KnowledgeBaseIndexState.READY &&
      (this.pending > 0 || this.orphan > 0 || !!this.syncStatus?.counts.failed)
    );
  }

  get embeddingIncompatible(): boolean {
    return this.indexState === KnowledgeBaseIndexState.READY && !!this.syncStatus?.embeddingIncompatible;
  }

  /** Whether the create / re-create button can be offered: admin, and creation not blocked server side. */
  get canCreate(): boolean {
    return this.isAdmin && this.blocker === null;
  }

  get createButtonLabelKey(): string {
    return this.indexState === KnowledgeBaseIndexState.NONE
      ? 'knowledge-base.sync-banner.create_index_button'
      : 'knowledge-base.sync-banner.recreate_index_button';
  }

  get titleKey(): string {
    switch (this.indexState) {
      case KnowledgeBaseIndexState.NONE:
        return 'knowledge-base.sync-banner.no_index_title';
      case KnowledgeBaseIndexState.MISSING:
        return 'knowledge-base.sync-banner.missing_index_title';
      default:
        if (this.outOfSync) return 'knowledge-base.sync-banner.out_of_sync_title';
        if (this.embeddingIncompatible) return 'knowledge-base.sync-banner.embedding_incompatible_title';
        return 'knowledge-base.sync-banner.ready_title';
    }
  }

  /**
   * i18n key of the blocker message shown in NONE / MISSING, null when creation is not blocked.
   * Index creation is admin-only, so a blocker a business user cannot lift themselves points to an
   * administrator; NO_PUBLISHED_ENTRY stays the same, since publishing is within the business user's reach.
   */
  get blockerMessageKey(): string | null {
    switch (this.blocker) {
      case CreateIndexBlocker.RAG_NOT_CONFIGURED:
        return 'knowledge-base.job.create_blocked_rag_not_configured';
      case CreateIndexBlocker.EMBEDDING_MODEL_UNDEFINED:
        return this.isAdmin
          ? 'knowledge-base.job.create_blocked_embedding_model_undefined'
          : 'knowledge-base.job.create_blocked_embedding_model_undefined_non_admin';
      case CreateIndexBlocker.NO_PUBLISHED_ENTRY:
        return 'knowledge-base.job.create_blocked_no_published_entry';
      default:
        return null;
    }
  }

  /**
   * NONE with nothing blocking creation. Only an admin can create the index — it may need to be built from
   * an external ingestion tool — so a business user gets an informative message rather than a call to act.
   */
  get noIndexMessageKey(): string {
    return this.isAdmin
      ? 'knowledge-base.sync-banner.no_index_message'
      : 'knowledge-base.sync-banner.no_index_message_non_admin';
  }

  /** MISSING: correcting the configuration or re-creating the index is admin-only. */
  get missingIndexMessageKey(): string {
    return this.isAdmin
      ? 'knowledge-base.sync-banner.missing_index_message'
      : 'knowledge-base.sync-banner.missing_index_message_non_admin';
  }

  /** Human-readable index origin: mapped label for known origins, the raw value otherwise. */
  get originLabel(): string {
    const origin = this.syncStatus?.collection?.origin ?? null;
    switch (origin) {
      case 'tock_kb':
        return this.transloco.translate('knowledge-base.sync-banner.origin_tock_kb');
      case 'qallam':
        return this.transloco.translate('knowledge-base.sync-banner.origin_qallam');
      case 'indexing_tools':
        return this.transloco.translate('knowledge-base.sync-banner.origin_indexing_tools');
      default:
        return origin ?? this.transloco.translate('knowledge-base.sync-banner.origin_unknown');
    }
  }

  get status(): BannerStatus {
    switch (this.indexState) {
      case KnowledgeBaseIndexState.NONE:
        return this.blocker ? 'warning' : 'info';
      case KnowledgeBaseIndexState.MISSING:
        return 'warning';
      default:
        return this.outOfSync || this.embeddingIncompatible ? 'warning' : 'success';
    }
  }

  get icon(): string {
    switch (this.indexState) {
      case KnowledgeBaseIndexState.NONE:
        return this.blocker ? 'exclamation-triangle' : 'database-add';
      case KnowledgeBaseIndexState.MISSING:
        return 'exclamation-triangle';
      default:
        return this.outOfSync || this.embeddingIncompatible ? 'exclamation-triangle' : 'database-check';
    }
  }

  /**
   * The link to the RAG settings is offered only to an admin: the settings page is admin-only, so a
   * simple user would just be bounced. The blocker message itself stays visible to everyone.
   */
  get canOpenRagSettings(): boolean {
    return this.isAdmin && this.blocker === CreateIndexBlocker.RAG_NOT_CONFIGURED;
  }

  /** Linked from the RAG_NOT_CONFIGURED blocker; the route lives in the rag module. */
  openRagSettings(): void {
    this.router.navigate(['/rag/settings']);
  }
}
