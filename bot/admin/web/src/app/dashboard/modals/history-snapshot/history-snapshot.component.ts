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
import { Component, inject, Input, OnInit } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';

import {
  BOT_HISTORY_EVENT_ICONS,
  BOT_HISTORY_SNAPSHOT_KINDS,
  BotHistoryEvent,
  BotHistorySnapshotKind,
  INDEX_SESSION_FACET,
  INDEX_SESSION_FACET_ICON,
  INDEX_SESSION_SNAPSHOT_FIELD,
  RAG_PROMPT_SNAPSHOT_FIELDS
} from '../../models/dashboard.model';
import { buildKvDiff, buildTagDiff, buildTextDiff, KvDiffResult, TagDiff, TextDiffLine } from './snapshot-diff.utils';

/** One rendered block of the RAG composite view. */
interface RagSection {
  titleKey: string;
  kind: 'kv' | 'text';
  kv?: KvDiffResult;
  text?: TextDiffLine[];
}

@Component({
  selector: 'tock-history-snapshot',
  templateUrl: './history-snapshot.component.html',
  styleUrls: ['./history-snapshot.component.scss'],
  standalone: false
})
export class HistorySnapshotComponent implements OnInit {
  @Input() event: BotHistoryEvent;

  readonly dialogRef = inject(NbDialogRef<HistorySnapshotComponent>);

  kind: BotHistorySnapshotKind;
  icon: string;
  labelKey: string;

  /** An index session move is surfaced on its own, above the raw settings diff. */
  isCorpusChange: boolean = false;
  previousSessionId: string;
  currentSessionId: string;

  /** True for the first change of its type: current state shown, no diff. */
  firstOfType: boolean = false;

  kvDiff: KvDiffResult;
  ragSections: RagSection[] = [];

  ngOnInit(): void {
    this.kind = BOT_HISTORY_SNAPSHOT_KINDS[this.event.type];
    this.firstOfType = !this.event.snapshot?.previous;

    const previous = this.event.snapshot?.previous ?? null;
    const current = this.event.snapshot?.current ?? {};

    this.previousSessionId = (previous?.[INDEX_SESSION_SNAPSHOT_FIELD] as string) ?? '';
    this.currentSessionId = (current[INDEX_SESSION_SNAPSHOT_FIELD] as string) ?? '';
    this.isCorpusChange = !!previous && this.previousSessionId !== this.currentSessionId;

    this.icon = this.isCorpusChange ? INDEX_SESSION_FACET_ICON : BOT_HISTORY_EVENT_ICONS[this.event.type];
    this.labelKey = this.isCorpusChange
      ? `dashboard.history.event.${INDEX_SESSION_FACET}.label`
      : `dashboard.history.event.${this.event.type}.label`;

    if (this.kind === 'kv') {
      this.kvDiff = buildKvDiff(previous, current);
    } else if (this.kind === 'rag') {
      this.ragSections = this.buildRagSections(previous, current);
    }
    // 'tags' is rendered directly from coveredTags() / excludedTags() in the template.
  }

  coveredTags(): TagDiff[] {
    const snap = this.event.snapshot;
    return buildTagDiff(
      snap?.previous ? ((snap.previous['coveredTopics'] as string[]) ?? []) : null,
      (snap?.current['coveredTopics'] as string[]) ?? []
    );
  }

  excludedTags(): TagDiff[] {
    const snap = this.event.snapshot;
    return buildTagDiff(
      snap?.previous ? ((snap.previous['excludedTopics'] as string[]) ?? []) : null,
      (snap?.current['excludedTopics'] as string[]) ?? []
    );
  }

  private buildRagSections(previous: Record<string, unknown> | null, current: Record<string, unknown>): RagSection[] {
    const sections: RagSection[] = [];
    const promptKeys = RAG_PROMPT_SNAPSHOT_FIELDS.map((field) => field.key);

    // 1. everything that is not a prompt → key/value
    const stripPrompts = (obj: Record<string, unknown> | null): Record<string, unknown> | null => {
      if (!obj) return obj;
      const clone = { ...obj };
      promptKeys.forEach((key) => delete clone[key]);
      return clone;
    };

    const settings = buildKvDiff(stripPrompts(previous), stripPrompts(current) ?? {});
    if (this.firstOfType || settings.changedCount > 0) {
      sections.push({ titleKey: 'dashboard.history.snapshot.settings', kind: 'kv', kv: settings });
    }

    // 2. each prompt → text diff, only when its template changed
    RAG_PROMPT_SNAPSHOT_FIELDS.forEach((field) => {
      const before = this.templateOf(previous, field.key);
      const after = this.templateOf(current, field.key);
      if (this.firstOfType || before !== after) {
        sections.push({ titleKey: field.labelKey, kind: 'text', text: buildTextDiff(before, after) });
      }
    });

    return sections;
  }

  private templateOf(obj: Record<string, unknown> | null, key: string): string {
    const field = obj?.[key] as { template?: string } | undefined;
    return field?.template ?? '';
  }

  close(): void {
    this.dialogRef.close();
  }
}
