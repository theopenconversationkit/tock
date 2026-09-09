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
import { Observable } from 'rxjs';

import { EvaluationSampleDefinition } from '../../quality/samples/models';
import { RagSettings } from '../../rag/rag-settings/models/rag-settings';
import {
  BotContact,
  BotHistoryEvent,
  BotIdentity,
  DashboardAnswerOutcome,
  DashboardPeriod,
  DashboardTopic,
  DashboardUsage,
  GenAiConfiguration,
  IngestionNotes,
  KnowledgeIndex
} from '../models/dashboard.model';

/**
 * Data access contract for the bot dashboard.
 *
 * Abstract on purpose: the module binds it to DashboardRestService in production and can
 * swap in DashboardMockService by changing a single provider line. Every widget depends
 * on this class, never on a concrete implementation.
 */
export abstract class DashboardService {
  // --- usage (POST /dialogs/stats) ---
  abstract getUsage(namespace: string, applicationName: string, period: DashboardPeriod, includeTests: boolean): Observable<DashboardUsage>;

  // --- answer outcome & topics (POST /bot/{applicationName}/metrics) ---
  abstract getAnswerOutcome(
    namespace: string,
    applicationName: string,
    period: DashboardPeriod,
    includeTests: boolean
  ): Observable<DashboardAnswerOutcome>;

  abstract getTopics(
    namespace: string,
    applicationName: string,
    period: DashboardPeriod,
    includeTests: boolean
  ): Observable<DashboardTopic[]>;

  // --- evaluations (GET /bots/{applicationName}/evaluation-samples/) ---
  abstract getEvaluationSamples(namespace: string, applicationName: string): Observable<EvaluationSampleDefinition[]>;

  // --- gen ai configuration (GET /gen-ai/bots/{applicationName}/configuration/*) ---
  abstract getGenAiConfiguration(namespace: string, applicationName: string): Observable<GenAiConfiguration>;

  abstract getRagSettings(applicationName: string): Observable<RagSettings>;

  // --- knowledge index (GET /gen-ai/bots/{botId}/vector-store/indexes) ---
  abstract getKnowledgeIndex(namespace: string, applicationName: string): Observable<KnowledgeIndex>;

  // --- ingestion notes (GET/PUT /bots/{botId}/index-sessions/{indexSessionId}/note) ---
  abstract getIngestionNotes(namespace: string, applicationName: string, indexSessionId: string): Observable<IngestionNotes>;

  abstract saveIngestionNotes(namespace: string, applicationName: string, notes: IngestionNotes): Observable<IngestionNotes>;

  // --- contacts (GET/PUT /bots/{botId}/contacts) ---
  abstract getContacts(namespace: string, applicationName: string): Observable<BotContact[]>;
  abstract saveContacts(namespace: string, applicationName: string, contacts: BotContact[]): Observable<BotContact[]>;

  // --- identity (GET/PUT /bots/{botId}/identity) ---
  abstract getBotIdentity(namespace: string, applicationName: string): Observable<BotIdentity>;
  abstract saveBotIdentity(namespace: string, applicationName: string, identity: BotIdentity): Observable<BotIdentity>;

  // --- history (GET /bots/{botId}/history) ---
  abstract getBotHistory(namespace: string, applicationName: string): Observable<BotHistoryEvent[]>;
}
