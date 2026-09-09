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
import { Injectable } from '@angular/core';
import { Observable, delay, of } from 'rxjs';

import { BotContact, BotHistoryEvent, BotHistoryEventType, BotIdentity, IngestionNotes } from '../models/dashboard.model';
import { DashboardRestService } from './dashboard-rest.service';

/**
 * Mock override for the endpoints the backend has not delivered yet: identity, contacts,
 * ingestion notes and history. Everything else falls through to DashboardRestService and
 * hits the real, already-existing endpoints.
 *
 * As each lot of the back ticket lands, drop the matching override here — the real call
 * inherited from the parent takes over. To enable, swap the provider in dashboard.module:
 *   { provide: DashboardService, useClass: DashboardMockService }
 */
@Injectable()
export class DashboardMockService extends DashboardRestService {
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

  private mockIdentity: BotIdentity = {
    displayName: 'Léa',
    notes:
      'Assistant for home insurance policy holders, exposed on the customer portal and the mobile app.\n' +
      'Answers about contracts, claims and payments. Anything about health or car insurance is out of scope\n' +
      'and handed over to the relevant assistant.',
    updatedAt: new Date(Date.now() - 34 * 86400000).toISOString(),
    updatedBy: 'r.leroy'
  };

  override getIngestionNotes(namespace: string, applicationName: string, indexSessionId: string): Observable<IngestionNotes> {
    return of({
      indexSessionId,
      text: '',
      updatedAt: null,
      updatedBy: null
    }).pipe(delay(200));
  }

  override saveIngestionNotes(namespace: string, applicationName: string, notes: IngestionNotes): Observable<IngestionNotes> {
    return of({ ...notes, updatedAt: new Date().toISOString(), updatedBy: 'you' }).pipe(delay(200));
  }

  override getContacts(namespace: string, applicationName: string): Observable<BotContact[]> {
    return of([...this.mockContacts]).pipe(delay(200));
  }

  override saveContacts(namespace: string, applicationName: string, contacts: BotContact[]): Observable<BotContact[]> {
    this.mockContacts = contacts.map((contact, index) => ({ ...contact, id: contact.id ?? `mock-${index}` }));
    return of([...this.mockContacts]).pipe(delay(200));
  }

  override getBotIdentity(namespace: string, applicationName: string): Observable<BotIdentity> {
    return of({ ...this.mockIdentity }).pipe(delay(200));
  }

  override saveBotIdentity(namespace: string, applicationName: string, identity: BotIdentity): Observable<BotIdentity> {
    this.mockIdentity = { ...identity, updatedAt: new Date().toISOString(), updatedBy: 'you' };
    return of({ ...this.mockIdentity }).pipe(delay(200));
  }

  override getBotHistory(namespace: string, applicationName: string): Observable<BotHistoryEvent[]> {
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
}
