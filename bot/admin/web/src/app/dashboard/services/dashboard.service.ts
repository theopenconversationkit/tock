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
import { inject, Injectable } from '@angular/core';
import { Observable, catchError, delay, forkJoin, map, of } from 'rxjs';

import { RestService } from '../../core-nlp/rest/rest.service';
import { RagAnswerStatus } from '../../shared/utils/dialog.utils';
import { snakeCaseToDisplayLabel } from '../../shared/utils';
import { CountByDateResult, CountResult, DialogStatsGroupResult, DialogStatsQueryResult } from '../../shared/model/dialog-data';
import { MetricGroupResult, MetricResult } from '../../metrics/models/metrics.model';
import { EvaluationSampleDefinition } from '../../quality/samples/models';
import { getEvaluationBaseUrl } from '../../quality/samples/utils';
import { RagSettings } from '../../rag/rag-settings/models/rag-settings';
import { VectorDbSettings } from '../../configuration/vector-db-settings/models/vector-db-settings';
import { CompressorSettings } from '../../configuration/compressor-settings/models/compressor-settings';
import { ObservabilitySettings } from '../../configuration/observability-settings/models/observability-settings';
import { VectorDbProvider } from '../../configuration/vector-db-settings/models/providers-configuration';
import {
  BotContact,
  BotHistoryEvent,
  BotHistoryEventType,
  BotIdentity,
  DashboardAnswerOutcome,
  DashboardPeriod,
  DashboardTopic,
  DashboardUsage,
  GenAiConfiguration,
  GenAiConfigurationCheck,
  IngestionNotes,
  KnowledgeIndex
} from '../models/dashboard.model';

/** Indicator emitted by RAGAnswerHandler as "RAG Status". */
const RAG_STATUS_INDICATOR = 'rag_status';

/** Indicator emitted by RAGAnswerHandler as "RAG Topics". */
const RAG_TOPICS_INDICATOR = 'rag_topics';

const TOPICS_DISPLAYED = 6;

const METRICS_GROUP_BY = ['APPLICATION_ID', 'TYPE', 'INDICATOR_NAME', 'INDICATOR_VALUE_NAME'];

@Injectable()
export class DashboardService {
  private readonly rest = inject(RestService);

  // ---------------------------------------------------------------------------
  // Usage — POST /dialogs/stats
  // ---------------------------------------------------------------------------

  /**
   * The backend always computes both the `test` and `prod` branches, splitting on the
   * `test-` applicationId prefix (BotAdminService.groupByAppConfigType). The query
   * itself carries no test flag: `includeTests` only decides which branches are summed.
   */
  getUsage(namespace: string, applicationName: string, period: DashboardPeriod, includeTests: boolean): Observable<DashboardUsage> {
    const current = this.rangeFor(period);
    const previous = this.previousRangeFor(period);

    return forkJoin([
      this.queryDialogStats(namespace, applicationName, current),
      this.queryDialogStats(namespace, applicationName, previous)
    ]).pipe(
      map(([currentStats, previousStats]) => {
        // All answer types, RAG or not: this widget must stay meaningful on a bot
        // that answers from stories only.
        const total = this.mergedTotal(currentStats, includeTests, (stats) => stats.allUserActions);
        const previousTotal = this.mergedTotal(previousStats, includeTests, (stats) => stats.allUserActions);

        return {
          total,
          previousTotal: previousTotal || null,
          byDate: this.accumulateByDate(this.mergedByDate(currentStats, includeTests), current),
          previousByDate: this.accumulateByDate(this.mergedByDate(previousStats, includeTests), previous),
          feedbackUp: this.mergedTotal(currentStats, includeTests, (stats) => stats.allFeedbackUp),
          feedbackDown: this.mergedTotal(currentStats, includeTests, (stats) => stats.allFeedbackDown),
          previousPositiveRate: this.positiveRate(previousStats, includeTests)
        };
      })
    );
  }

  /** Sums one counter over the `prod` branch, plus `test` when tests are displayed. */
  private mergedTotal(
    result: DialogStatsGroupResult,
    includeTests: boolean,
    pick: (stats: DialogStatsQueryResult) => CountResult[]
  ): number {
    const branches = includeTests ? [result?.prod, result?.test] : [result?.prod];
    return branches.reduce((sum, branch) => sum + this.sumCounts(branch ? pick(branch) : []), 0);
  }

  private mergedByDate(result: DialogStatsGroupResult, includeTests: boolean): CountByDateResult[] {
    return includeTests
      ? [...(result?.prod?.allUserActionsByDate ?? []), ...(result?.test?.allUserActionsByDate ?? [])]
      : result?.prod?.allUserActionsByDate ?? [];
  }

  private queryDialogStats(
    namespace: string,
    applicationName: string,
    range: { from: Date; to: Date }
  ): Observable<DialogStatsGroupResult> {
    return this.rest.post<unknown, DialogStatsGroupResult>('/dialogs/stats', {
      namespace,
      applicationName,
      from: range.from,
      to: range.to
    });
  }

  private positiveRate(result: DialogStatsGroupResult, includeTests: boolean): number | null {
    const up = this.mergedTotal(result, includeTests, (stats) => stats.allFeedbackUp);
    const down = this.mergedTotal(result, includeTests, (stats) => stats.allFeedbackDown);
    return up + down ? up / (up + down) : null;
  }

  private sumCounts(counts: CountResult[]): number {
    return (counts ?? []).reduce((sum, item) => sum + item.total, 0);
  }

  private accumulateByDate(raw: CountByDateResult[], range: { from: Date; to: Date }) {
    const totals: Record<string, number> = {};
    (raw ?? []).forEach((item) => {
      totals[item.date] = (totals[item.date] ?? 0) + item.total;
    });

    // The backend only returns days that carry data: fill the gaps so the curve
    // does not compress quiet periods.
    return this.datesBetween(range.from, range.to).map((date) => ({ date, count: totals[date] ?? 0 }));
  }

  // ---------------------------------------------------------------------------
  // Answer outcome and topics — POST /bot/{applicationName}/metrics
  // ---------------------------------------------------------------------------

  getAnswerOutcome(
    namespace: string,
    applicationName: string,
    period: DashboardPeriod,
    includeTests: boolean
  ): Observable<DashboardAnswerOutcome> {
    return forkJoin([
      this.queryIndicator(applicationName, RAG_STATUS_INDICATOR, this.rangeFor(period)),
      this.queryIndicator(applicationName, RAG_STATUS_INDICATOR, this.previousRangeFor(period))
    ]).pipe(
      map(([current, previous]) => ({
        counts: this.countsByValue(current, RAG_STATUS_INDICATOR, includeTests) as Partial<Record<RagAnswerStatus, number>>,
        previousCounts: this.countsByValue(previous, RAG_STATUS_INDICATOR, includeTests) as Partial<Record<RagAnswerStatus, number>>
      }))
    );
  }

  getTopics(namespace: string, applicationName: string, period: DashboardPeriod, includeTests: boolean): Observable<DashboardTopic[]> {
    return this.queryIndicator(applicationName, RAG_TOPICS_INDICATOR, this.rangeFor(period)).pipe(
      map((result) => {
        const counts = this.countsByValue(result, RAG_TOPICS_INDICATOR, includeTests);
        return (
          Object.entries(counts)
            .sort(([, a], [, b]) => b - a)
            .slice(0, TOPICS_DISPLAYED)
            // Topic values are emitted in snake_case by the LLM. Formatted here rather
            // than in the template, the same way the metrics board renders indicator values.
            .map(([name, count]) => ({ name: snakeCaseToDisplayLabel(name), count }))
        );
      })
    );
  }

  private queryIndicator(applicationName: string, indicatorName: string, range: { from: Date; to: Date }): Observable<MetricGroupResult> {
    return this.rest.post<unknown, MetricGroupResult>(`/bot/${applicationName}/metrics`, {
      filter: {
        indicatorNames: [indicatorName],
        creationDateSince: range.from,
        creationDateUntil: range.to
      },
      groupBy: METRICS_GROUP_BY
    });
  }

  /** Sums the rows of one indicator, keyed by indicator value. */
  private countsByValue(result: MetricGroupResult, indicatorName: string, includeTests: boolean): Record<string, number> {
    const branch = includeTests ? [...(result?.prod ?? []), ...(result?.test ?? [])] : result?.prod ?? [];
    const rows: MetricResult[] = branch.filter((metric) => metric.row?.indicatorName === indicatorName);

    return rows.reduce<Record<string, number>>((counts, metric) => {
      const value = metric.row?.indicatorValueName;
      if (value) {
        counts[value] = (counts[value] ?? 0) + metric.count;
      }
      return counts;
    }, {});
  }

  // ---------------------------------------------------------------------------
  // Evaluations — GET /bots/{applicationName}/evaluation-samples/
  // ---------------------------------------------------------------------------

  /** Returns every sample; the caller keeps only the validated ones. */
  getEvaluationSamples(namespace: string, applicationName: string): Observable<EvaluationSampleDefinition[]> {
    return this.rest.get<EvaluationSampleDefinition[]>(
      getEvaluationBaseUrl(applicationName),
      (samples: EvaluationSampleDefinition[]) => samples ?? []
    );
  }

  // ---------------------------------------------------------------------------
  // Gen AI configuration — GET /gen-ai/bots/{applicationName}/configuration/*
  // ---------------------------------------------------------------------------

  /**
   * Reads the four stored configurations. This reports what is *configured*, not
   * whether each provider actually answers: the config-check endpoints perform real
   * network calls and are too costly to run on every dashboard load.
   */
  getGenAiConfiguration(namespace: string, applicationName: string): Observable<GenAiConfiguration> {
    return forkJoin([
      this.getRagSettings(applicationName),
      this.getSettings<VectorDbSettings>(applicationName, 'vector-store'),
      this.getSettings<CompressorSettings>(applicationName, 'document-compressor'),
      this.getSettings<ObservabilitySettings>(applicationName, 'observability')
    ]).pipe(
      map(([rag, vectorDb, compressor, observability]) => ({
        lastCheckedAt: new Date().toISOString(),
        checks: this.buildChecks(rag, vectorDb, compressor, observability)
      }))
    );
  }

  getRagSettings(applicationName: string): Observable<RagSettings> {
    return this.getSettings<RagSettings>(applicationName, 'rag');
  }

  private getSettings<T>(applicationName: string, resource: string): Observable<T> {
    const url = `/gen-ai/bots/${applicationName}/configuration/${resource}`;
    return this.rest.get<T>(url, (settings: T) => settings).pipe(catchError(() => of(null as T)));
  }

  private buildChecks(
    rag: RagSettings,
    vectorDb: VectorDbSettings,
    compressor: CompressorSettings,
    observability: ObservabilitySettings
  ): GenAiConfigurationCheck[] {
    const checks: GenAiConfigurationCheck[] = [];

    checks.push(this.settingCheck('LLM', rag?.questionAnsweringLlmSetting?.provider, rag?.questionAnsweringLlmSetting?.model));
    checks.push(this.settingCheck('Embedding', rag?.emSetting?.provider, rag?.emSetting?.model));

    // Most bots run on the server-wide vector store: the absence of a bot-level
    // configuration is the normal case and must not be reported as a problem.
    checks.push({
      label: 'Vector store',
      value: vectorDb?.enabled
        ? [vectorDb.setting?.provider, rag?.documentSearchType].filter(Boolean).join(' · ')
        : ['Server default', rag?.documentSearchType].filter(Boolean).join(' · '),
      status: 'ok'
    });

    checks.push({
      label: 'Compressor',
      value: compressor?.enabled ? `${compressor.setting?.provider} · max ${compressor.setting?.maxDocuments} documents` : 'Disabled',
      status: compressor?.enabled ? 'ok' : 'warning',
      // Known defect: create_rag_chain() never reads request.compressor_setting, so the
      // flag has no runtime effect. Remove this note once the orchestrator is fixed.
      note: compressor?.enabled ? 'Enabled in the settings but not applied at runtime' : undefined
    });

    checks.push({
      label: 'Observability',
      value: observability?.enabled ? String(observability.setting?.provider) : 'Disabled',
      status: observability?.enabled ? 'ok' : 'warning'
    });

    checks.push({
      label: 'Documents retrieved',
      value: rag?.maxDocumentsRetrieved != null ? String(rag.maxDocumentsRetrieved) : 'Not set',
      status: rag?.maxDocumentsRetrieved != null ? 'ok' : 'warning'
    });

    return checks;
  }

  private settingCheck(label: string, provider: unknown, model: unknown): GenAiConfigurationCheck {
    const configured = !!provider;
    return {
      label,
      value: configured ? [provider, model].filter(Boolean).join(' · ') : 'Not configured',
      status: configured ? 'ok' : 'error'
    };
  }

  // ---------------------------------------------------------------------------
  // Knowledge index — partly real, partly mocked
  // ---------------------------------------------------------------------------

  /**
   * The session identifier and index name come from the stored RAG configuration.
   *
   * MOCKED: ingestion date, document and chunk counts, and whether the collection
   * actually exists. None of these live in Mongo — the date is a chunk metadata
   * (`index_datetime`) written at ingestion time, so reading it requires the
   * vector-store-inspection API.
   */
  getKnowledgeIndex(namespace: string, applicationName: string): Observable<KnowledgeIndex> {
    return forkJoin([this.getRagSettings(applicationName), this.getSettings<VectorDbSettings>(applicationName, 'vector-store')]).pipe(
      map(([rag, vectorDb]) => {
        if (!rag?.enabled || !rag?.indexSessionId) {
          return null;
        }

        return {
          indexSessionId: rag.indexSessionId,
          indexName: rag.indexName,
          provider: (vectorDb?.setting?.provider ?? VectorDbProvider.PGVector) as VectorDbProvider,
          embeddingLabel: [rag.emSetting?.provider, rag.emSetting?.model].filter(Boolean).join(' · ') || null,

          // --- mocked until the vector-store-inspection API lands ---
          indexDatetime: new Date(Date.now() - 8 * 86400000).toISOString(),
          documentCount: 2417,
          chunkCount: 31680,
          existsInStore: true
        };
      })
    );
  }

  /** MOCKED. Notes are attached to one ingestion session; no backend yet. */
  getIngestionNotes(namespace: string, applicationName: string, indexSessionId: string): Observable<IngestionNotes> {
    return of({
      indexSessionId,
      text: '',
      updatedAt: null,
      updatedBy: null
    }).pipe(delay(200));
  }

  /** MOCKED. */
  saveIngestionNotes(namespace: string, applicationName: string, notes: IngestionNotes): Observable<IngestionNotes> {
    return of({ ...notes, updatedAt: new Date().toISOString(), updatedBy: 'you' }).pipe(delay(200));
  }

  // ---------------------------------------------------------------------------
  // Contacts — MOCKED, schema addition still to be specified
  // ---------------------------------------------------------------------------

  private mockContacts: BotContact[] = [
    {
      id: '1',
      role: 'Business owner',
      name: 'Digital Insurance squad',
      email: 'squad-assurance@example.com',
      note: 'Content questions and wording changes.'
    },
    {
      id: '2',
      role: 'Technical owner',
      name: 'Conversational platform',
      email: 'plateforme-conv@example.com',
      link: 'https://wiki.example.com/conv',
      comment: 'On-call rota published each Monday on the wiki.'
    }
  ];

  getContacts(namespace: string, applicationName: string): Observable<BotContact[]> {
    return of([...this.mockContacts]).pipe(delay(200));
  }

  saveContacts(namespace: string, applicationName: string, contacts: BotContact[]): Observable<BotContact[]> {
    this.mockContacts = contacts.map((contact, index) => ({ ...contact, id: contact.id ?? `mock-${index}` }));
    return of([...this.mockContacts]).pipe(delay(200));
  }

  // ---------------------------------------------------------------------------
  // Bot identity — MOCKED, schema addition still to be specified
  // ---------------------------------------------------------------------------

  private mockIdentity: BotIdentity = {
    displayName: 'Léa',
    notes:
      'Assistant for home insurance policy holders, exposed on the customer portal and the mobile app.\n' +
      'Answers about contracts, claims and payments. Anything about health or car insurance is out of scope\n' +
      'and handed over to the relevant assistant.',
    updatedAt: new Date(Date.now() - 34 * 86400000).toISOString(),
    updatedBy: 'r.leroy'
  };

  getBotIdentity(namespace: string, applicationName: string): Observable<BotIdentity> {
    return of({ ...this.mockIdentity }).pipe(delay(200));
  }

  saveBotIdentity(namespace: string, applicationName: string, identity: BotIdentity): Observable<BotIdentity> {
    this.mockIdentity = { ...identity, updatedAt: new Date().toISOString(), updatedBy: 'you' };
    return of({ ...this.mockIdentity }).pipe(delay(200));
  }

  // ---------------------------------------------------------------------------
  // History — MOCKED
  //
  // No single source exists today: creation date would come from the application,
  // ingestions from the vector store, settings changes from an audit trail that does
  // not exist yet, and evaluations from the samples endpoint. Simulated here to settle
  // the display first.
  // ---------------------------------------------------------------------------

  getBotHistory(namespace: string, applicationName: string): Observable<BotHistoryEvent[]> {
    return of(this.buildMockHistory()).pipe(delay(300));
  }

  private buildMockHistory(): BotHistoryEvent[] {
    const events: BotHistoryEvent[] = [];
    const now = Date.now();
    const day = 86400000;

    const createdAt = now - 985 * day;
    const authors = ['r.leroy', 'm.bekkari', 'c.tanguy', 's.morvan'];
    const pick = (seed: number) => authors[seed % authors.length];

    // Baseline RAG configuration, reused to build realistic before/after snapshots.
    const ragBase = {
      questionCondensingLlmSetting: { provider: 'AzureOpenAIService', model: 'gpt-4o', temperature: 0.2 },
      questionAnsweringLlmSetting: { provider: 'AzureOpenAIService', model: 'gpt-4o', temperature: 0.2 },
      emSetting: { provider: 'AzureOpenAIService', model: 'text-embedding-3-large' },
      indexSessionId: '8f14e45f-ceea-4a5b-9c2f-3d1b70e12a44',
      maxDocumentsRetrieved: 4,
      documentsRequired: false,
      documentSearchType: 'HYBRID_SEARCH',
      maxMessagesFromHistory: 5,
      questionCondensingPrompt: { template: "Reformule la question en tenant compte de l'historique." },
      questionAnsweringPrompt: {
        template: "Tu es l'assistant de l'assurance habitation.\nRéponds à partir des documents fournis."
      }
    };

    events.push({
      id: 'created',
      date: new Date(createdAt).toISOString(),
      type: BotHistoryEventType.created,
      author: pick(0)
    });

    events.push({
      id: 'connector-0',
      date: new Date(createdAt + 6 * day).toISOString(),
      type: BotHistoryEventType.connector,
      params: { connector: 'web', label: 'Portail client' },
      author: pick(1)
    });

    // First RAG configuration: no predecessor, the modal shows the state alone.
    events.push({
      id: 'rag-0',
      date: new Date(createdAt + 30 * day).toISOString(),
      type: BotHistoryEventType.ragSettings,
      author: pick(2),
      snapshot: { previous: null, current: ragBase }
    });

    // Corpus updates: plain RAG settings events whose indexSessionId moved. The front
    // derives the corpus facet from the snapshot rather than from a dedicated type.
    const corpusUpdates = [
      { offset: 3, session: 'c4f2a1d8-77b3-4e21-9a08-51ce62d4a913', documents: 2417, chunks: 31680 },
      { offset: 34, session: 'b1d90e77-2a4c-4f60-8e15-90ab73c1f228', documents: 2311, chunks: 30112 },
      { offset: 61, session: '5e7c3b90-14da-4c8f-b7e2-2f4109ad6b55', documents: 2088, chunks: 27340 }
    ];

    corpusUpdates.forEach((update, index) => {
      const previousSession = corpusUpdates[index + 1]?.session ?? ragBase.indexSessionId;
      events.push({
        id: `corpus-${index}`,
        date: new Date(now - update.offset * day).toISOString(),
        type: BotHistoryEventType.ragSettings,
        params: { documentCount: update.documents, chunkCount: update.chunks },
        author: 'qallam',
        snapshot: {
          previous: { ...ragBase, indexSessionId: previousSession },
          current: { ...ragBase, indexSessionId: update.session }
        }
      });
    });

    // Retrieval thresholds only.
    events.push({
      id: 'rag-1',
      date: new Date(now - 12 * day).toISOString(),
      type: BotHistoryEventType.ragSettings,
      author: pick(0),
      snapshot: {
        previous: { ...ragBase, maxDocumentsRetrieved: 4, documentsRequired: false },
        current: { ...ragBase, maxDocumentsRetrieved: 6, documentsRequired: true }
      }
    });

    // Search type — a RAG settings field, not a vector store change.
    events.push({
      id: 'rag-2',
      date: new Date(now - 20 * day).toISOString(),
      type: BotHistoryEventType.ragSettings,
      author: pick(3),
      snapshot: {
        previous: { ...ragBase, documentSearchType: 'SIMILARITY_SEARCH' },
        current: { ...ragBase, documentSearchType: 'HYBRID_SEARCH' }
      }
    });

    // Model and answering prompt changed in the same save.
    events.push({
      id: 'rag-3',
      date: new Date(now - 47 * day).toISOString(),
      type: BotHistoryEventType.ragSettings,
      author: pick(1),
      snapshot: {
        previous: {
          ...ragBase,
          questionAnsweringLlmSetting: { provider: 'AzureOpenAIService', model: 'gpt-4-turbo', temperature: 0.0 },
          questionAnsweringPrompt: {
            template: "Tu es l'assistant de l'assurance habitation.\nRéponds à partir des documents fournis.\nSi tu ne sais pas, dis-le."
          }
        },
        current: {
          ...ragBase,
          questionAnsweringPrompt: {
            template:
              "Tu es Léa, l'assistante de l'assurance habitation.\nRéponds à partir des documents fournis, avec un ton courtois.\nSi la réponse n'est pas dans les documents, invite l'utilisateur à contacter un conseiller.\nNe réponds jamais sur l'assurance auto ou santé."
          }
        }
      }
    });

    // A genuine bot-level vector database override — rare but real.
    events.push({
      id: 'vs-0',
      date: new Date(now - 75 * day).toISOString(),
      type: BotHistoryEventType.vectorStore,
      author: pick(2),
      snapshot: {
        previous: { enabled: false, setting: { provider: 'PGVector', host: 'vector-shared', port: 5432, database: 'tock' } },
        current: { enabled: true, setting: { provider: 'PGVector', host: 'vector-assurance', port: 5432, database: 'habitation' } }
      }
    });

    events.push({
      id: 'pc-0',
      date: new Date(now - 27 * day).toISOString(),
      type: BotHistoryEventType.promptContext,
      author: pick(1),
      snapshot: {
        previous: { coveredTopics: ['Résiliation', 'Sinistre', 'Cotisation'], excludedTopics: ['Assurance auto'] },
        current: {
          coveredTopics: ['Résiliation', 'Sinistre', 'Cotisation', 'Bénéficiaire'],
          excludedTopics: ['Assurance auto', 'Assurance santé']
        }
      }
    });

    events.push({
      id: 'obs-0',
      date: new Date(now - 120 * day).toISOString(),
      type: BotHistoryEventType.observability,
      author: pick(0),
      snapshot: {
        previous: { enabled: false, setting: { provider: 'Langfuse' } },
        current: { enabled: true, setting: { provider: 'Langfuse', publicUrl: 'https://langfuse.example.com' } }
      }
    });

    // Evaluations, no snapshot.
    [150, 330, 520, 700, 880, 960].forEach((offset, i) => {
      events.push({
        id: `evaluation-${i}`,
        date: new Date(createdAt + offset * day).toISOString(),
        type: BotHistoryEventType.evaluation,
        params: { positiveRate: 74 + ((i * 13) % 22), dialogCount: 120 + i * 20 },
        author: pick(i + 2)
      });
    });

    // Most recent first: the widget scrolls back in time.
    return events.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
  }

  // ---------------------------------------------------------------------------
  // Ranges
  // ---------------------------------------------------------------------------

  private rangeFor(period: DashboardPeriod): { from: Date; to: Date } {
    const to = new Date();
    const from = new Date();
    from.setDate(from.getDate() - (period - 1));
    return { from: this.startOfDay(from), to };
  }

  private previousRangeFor(period: DashboardPeriod): { from: Date; to: Date } {
    const to = new Date();
    to.setDate(to.getDate() - period);
    const from = new Date(to);
    from.setDate(from.getDate() - (period - 1));
    return { from: this.startOfDay(from), to };
  }

  private startOfDay(date: Date): Date {
    const start = new Date(date);
    start.setHours(0, 0, 0, 0);
    return start;
  }

  private datesBetween(from: Date, to: Date): string[] {
    const dates: string[] = [];
    const cursor = new Date(from);

    while (cursor <= to) {
      dates.push(this.formatDate(cursor));
      cursor.setDate(cursor.getDate() + 1);
    }

    return dates;
  }

  /** Local calendar day, matching the `date` field returned by the backend. */
  private formatDate(date: Date): string {
    const month = `${date.getMonth() + 1}`.padStart(2, '0');
    const day = `${date.getDate()}`.padStart(2, '0');
    return `${date.getFullYear()}-${month}-${day}`;
  }
}
