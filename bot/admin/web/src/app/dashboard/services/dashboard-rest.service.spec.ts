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
import { TestBed } from '@angular/core/testing';
import { firstValueFrom, of, throwError } from 'rxjs';
import { RestService } from '../../core-nlp/rest/rest.service';
import { DashboardRestService } from './dashboard-rest.service';

describe('DashboardRestService contracts', () => {
  let service: DashboardRestService;
  let rest: jasmine.SpyObj<RestService>;
  let responses: Record<string, unknown>;

  beforeEach(() => {
    rest = jasmine.createSpyObj('RestService', ['get', 'post']);
    responses = {
      '/gen-ai/bots/bot/configuration/rag': { enabled: true, indexSessionId: 'session', emSetting: {} },
      '/gen-ai/bots/bot/vector-store/capabilities': { provider: 'PGVector', supportsIndexListing: true },
      '/gen-ai/bots/bot/vector-store/indexes': {
        indexes: [{ indexSessionId: 'session', indexName: 'index', isCurrent: true, documentCount: 12 }]
      }
    };
    rest.get.and.callFake((path: string, parse: (value: any) => any) => of(parse(responses[path])));
    TestBed.configureTestingModule({ providers: [DashboardRestService, { provide: RestService, useValue: rest }] });
    service = TestBed.inject(DashboardRestService);
  });

  it('loads both usage periods through the dedicated endpoint and merges test counters locally', async () => {
    const branch = (app: string, total: number, up: number, down: number) => ({
      allUserActions: [{ _id: app, total }],
      allUserActionsByDate: [],
      allFeedbackUp: [{ _id: app, total: up }],
      allFeedbackDown: [{ _id: app, total: down }]
    });
    rest.post.and.returnValue(of({ prod: branch('prod', 10, 3, 1), test: branch('test-bot', 5, 1, 2) }));
    const prod = await firstValueFrom(service.getUsage('ns', 'bot', 30, false));
    expect(prod.total).toBe(10);
    expect(prod.previousTotal).toBe(10);
    expect(prod.feedbackUp).toBe(3);
    expect(prod.feedbackDown).toBe(1);
    expect(prod.previousPositiveRate).toBe(0.75);
    expect(rest.post.calls.count()).toBe(2);
    const calls = rest.post.calls.allArgs();
    expect(calls.every(([url]) => url === '/bots/bot/usage')).toBeTrue();
    expect((calls[1][1] as any).to.getTime()).toBeLessThan((calls[0][1] as any).from.getTime());
    const combined = await firstValueFrom(service.getUsage('ns', 'bot', 30, true));
    expect(combined.total).toBe(15);
    expect(combined.feedbackUp).toBe(4);
    expect(combined.feedbackDown).toBe(3);
  });

  it('unwraps the index envelope served by master', async () => {
    const index = await firstValueFrom(service.getKnowledgeIndex('ns', 'bot'));
    expect(index.existsInStore).toBeTrue();
    expect(index.documentCount).toBe(12);
    expect(index.provider).toBe('PGVector');
  });

  it('does not list indexes when inspection is unsupported', async () => {
    responses['/gen-ai/bots/bot/vector-store/capabilities'] = { provider: 'OpenSearch', supportsIndexListing: false };
    const index = await firstValueFrom(service.getKnowledgeIndex('ns', 'bot'));
    expect(index.inspectionSupported).toBeFalse();
    expect(index.provider).toBe('OpenSearch');
    expect(rest.get.calls.allArgs().some(([url]) => url.endsWith('/indexes'))).toBeFalse();
  });

  it('keeps an inspection failure distinct from a missing session', async () => {
    rest.get.and.callFake((path: string, parse: (value: any) => any) =>
      path.endsWith('/indexes') ? throwError(() => new Error('unreachable')) : of(parse(responses[path]))
    );
    await expectAsync(firstValueFrom(service.getKnowledgeIndex('ns', 'bot'))).toBeRejectedWithError('unreachable');
  });

  it('does not inspect a disabled RAG configuration', async () => {
    responses['/gen-ai/bots/bot/configuration/rag'] = { enabled: false };
    expect(await firstValueFrom(service.getKnowledgeIndex('ns', 'bot'))).toBeNull();
    expect(rest.get.calls.count()).toBe(1);
  });

  it('preserves pagination metadata and sends the cursor', async () => {
    const page = { events: [], hasMore: true, nextCursor: 'next' };
    responses['/bots/bot/history?before=cursor'] = page;
    expect(await firstValueFrom(service.getBotHistory('ns', 'bot', 'cursor'))).toEqual(page);
  });
});
