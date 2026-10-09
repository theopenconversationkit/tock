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
import { firstValueFrom, of } from 'rxjs';
import { RestService } from '../../core-nlp/rest/rest.service';
import { StateService } from '../../core-nlp/state.service';
import { KnowledgeBaseRestService } from './knowledge-base-rest.service';

describe('Knowledge base REST contracts', () => {
  let service: KnowledgeBaseRestService;
  let rest: jasmine.SpyObj<RestService>;
  const state = { currentApplication: { name: 'bot one' } };
  beforeEach(() => {
    rest = jasmine.createSpyObj('RestService', ['get', 'post', 'put', 'getArray']);
    rest.post.and.returnValue(of({ id: 'job' }));
    rest.get.and.callFake((path: string, parse: (value: any) => any) => of(parse(null)));
    TestBed.configureTestingModule({
      providers: [KnowledgeBaseRestService, { provide: RestService, useValue: rest }, { provide: StateService, useValue: state }]
    });
    service = TestBed.inject(KnowledgeBaseRestService);
  });
  it('keeps the deletion job response and escapes identifiers', async () => {
    expect((await firstValueFrom(service.deleteEntry('entry/id'))).id).toBe('job');
    expect(rest.post.calls.mostRecent().args).toEqual(['/bots/bot%20one/knowledge-base/entries/entry%2Fid/delete', {}]);
  });
  it('maps an empty active-job response to null', async () => {
    expect(await firstValueFrom(service.getActiveJob())).toBeNull();
  });
  it('encodes filters rather than merging them into query syntax', async () => {
    await firstValueFrom(service.searchEntries({ start: 0, size: 25, search: 'a&status=PUBLISHED' }));
    const url = rest.get.calls.mostRecent().args[0];
    expect(url).toContain('search=a%26status%3DPUBLISHED');
  });
});
