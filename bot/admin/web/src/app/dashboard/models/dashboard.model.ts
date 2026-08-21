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
import { RagAnswerStatus } from '../../shared/utils/dialog.utils';
import { VectorDbProvider } from '../../configuration/vector-db-settings/models/providers-configuration';

/**
 * Reporting window, in days. Shared by every time-based widget of the dashboard.
 */
export type DashboardPeriod = 7 | 30 | 90;

export const DASHBOARD_PERIODS: DashboardPeriod[] = [7, 30, 90];

/**
 * Every widget loads independently and must be able to fail on its own.
 */
export enum WidgetState {
  loading = 'loading',
  ready = 'ready',
  /** Configured, but nothing to report over the selected period. */
  empty = 'empty',
  /** Not applicable to this bot (RAG disabled, no index, no metrics emitted). */
  unavailable = 'unavailable',
  error = 'error'
}

/** POST /dialogs/stats - prod branch only, test dialogs are deliberately ignored. */
export interface DashboardUsage {
  total: number;
  previousTotal: number | null;
  byDate: DashboardDailyCount[];
  /** Same length as byDate, aligned index by index, for the visual comparison. */
  previousByDate: DashboardDailyCount[];
  feedbackUp: number;
  feedbackDown: number;
  previousPositiveRate: number | null;
}

export interface DashboardDailyCount {
  date: string;
  count: number;
}

/** POST /bot/{applicationName}/metrics - indicator `rag_status`. */
export interface DashboardAnswerOutcome {
  counts: Partial<Record<RagAnswerStatus, number>>;
  previousCounts: Partial<Record<RagAnswerStatus, number>> | null;
}

/** POST /bot/{applicationName}/metrics - indicator `rag_topics`. */
export interface DashboardTopic {
  name: string;
  count: number;
}

/** vector-store-inspection API. */
export interface KnowledgeIndex {
  indexSessionId: string;
  indexName: string;
  /** Read from the `index_datetime` chunk metadata. Null when the pipeline did not set it. */
  indexDatetime: string | null;
  documentCount: number | null;
  chunkCount: number | null;
  provider: VectorDbProvider;
  embeddingLabel: string | null;
  /** False when the session configured in the RAG settings has no matching collection. */
  existsInStore: boolean;
}

/** Free-form notes attached to one ingestion session. */
export interface IngestionNotes {
  indexSessionId: string;
  text: string;
  updatedAt: string | null;
  updatedBy: string | null;
}

/**
 * Contacts are held on the application, not on a connector configuration.
 * `role` is free text on purpose: a closed list would not survive contact with reality.
 */
export interface BotContact {
  id: string;
  role: string;
  name: string;
  email?: string;
  link?: string;
  note?: string;
  comment?: string;
}

export const CONTACT_ROLE_SUGGESTIONS: string[] = [
  'Business owner',
  'Technical owner',
  'Corpus & ingestion',
  'Escalation',
  'Security & compliance'
];

export type GenAiCheckStatus = 'ok' | 'warning' | 'error';

export interface GenAiConfigurationCheck {
  label: string;
  value: string;
  status: GenAiCheckStatus;
  note?: string;
}

export interface GenAiConfiguration {
  checks: GenAiConfigurationCheck[];
  lastCheckedAt: string | null;
}

/**
 * Identity of the bot as the business sees it. The technical bot id and the name the
 * end user is given in the prompt are almost never the same, and only the former is
 * stored today.
 */
export interface BotIdentity {
  /** Name the assistant introduces itself with. */
  displayName: string;
  /** Free-form notes about the bot: purpose, audience, decisions worth remembering. */
  notes: string;
  updatedAt: string | null;
  updatedBy: string | null;
}

export enum BotHistoryEventType {
  created = 'created',
  ingestion = 'ingestion',
  ragSettings = 'rag-settings',
  vectorStoreSettings = 'vector-store-settings',
  promptChange = 'prompt-change',
  connector = 'connector',
  evaluation = 'evaluation'
}

export interface BotHistoryEvent {
  id: string;
  date: string;
  type: BotHistoryEventType;
  /** Short headline, already resolved for display. */
  label: string;
  /** Optional second line: what changed, which index, which score. */
  detail?: string;
  author?: string;
}

export const BOT_HISTORY_EVENT_ICONS: Record<BotHistoryEventType, string> = {
  [BotHistoryEventType.created]: 'stars',
  [BotHistoryEventType.ingestion]: 'database-add',
  [BotHistoryEventType.ragSettings]: 'sliders',
  [BotHistoryEventType.vectorStoreSettings]: 'hdd-network',
  [BotHistoryEventType.promptChange]: 'chat-square-quote',
  [BotHistoryEventType.connector]: 'plug',
  [BotHistoryEventType.evaluation]: 'eyedropper'
};
