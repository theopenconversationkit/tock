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

import { Component, Input, OnInit, inject } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';

import { KnowledgeBaseIndexState } from '../../models';

/**
 * Confirms an index creation or re-creation and collects the one choice that shapes it: whether the
 * bot should be pointed at the new index as soon as it exists (`switchIndex`) or whether the index is
 * created as a standalone snapshot the bot keeps ignoring for now. RAG activation is never touched by
 * this action, whatever the choice — the dialog says so explicitly.
 *
 * Closes with the chosen boolean on confirm, and with `undefined` on cancel, so the caller can tell a
 * cancellation from a `switchIndex === false` confirmation.
 */
@Component({
  selector: 'tock-knowledge-base-create-index-dialog',
  templateUrl: './create-index-dialog.component.html',
  styleUrl: './create-index-dialog.component.scss',
  standalone: false
})
export class KnowledgeBaseCreateIndexDialogComponent implements OnInit {
  private dialogRef = inject(NbDialogRef);

  /** Current index state, drives the title and the default of the switch checkbox. */
  @Input() indexState: KnowledgeBaseIndexState = KnowledgeBaseIndexState.NONE;
  /** Whether RAG is enabled on the bot, so the dialog can warn that a switched index goes live at once. */
  @Input() ragEnabled: boolean = false;
  /** Documentary rows in the current index that a fresh index would not carry over. */
  @Input() otherRowCount: number | null = null;

  switchIndex: boolean = true;

  ngOnInit(): void {
    // Pointing the bot at the new index is the natural default when it has no usable index yet
    // (NONE / MISSING); re-creating over a working index defaults to a snapshot instead.
    this.switchIndex = this.indexState !== KnowledgeBaseIndexState.READY;
  }

  get recreate(): boolean {
    return this.indexState === KnowledgeBaseIndexState.READY;
  }

  get titleKey(): string {
    return this.recreate ? 'knowledge-base.create-index-dialog.recreate_title' : 'knowledge-base.create-index-dialog.create_title';
  }

  get confirmLabelKey(): string {
    return this.recreate ? 'knowledge-base.create-index-dialog.recreate_button' : 'knowledge-base.create-index-dialog.create_button';
  }

  /** Documentary rows that would be lost, shown only when switching onto an index that has some. */
  get droppedOtherRows(): number {
    return this.switchIndex ? this.otherRowCount ?? 0 : 0;
  }

  confirm(): void {
    this.dialogRef.close(this.switchIndex);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
