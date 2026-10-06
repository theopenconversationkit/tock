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
import { NbDialogRef } from '@nebular/theme';
import { BotHistoryEventType } from '../../models/dashboard.model';
import { HistorySnapshotComponent } from './history-snapshot.component';

describe('History snapshot completeness', () => {
  let component: HistorySnapshotComponent;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [{ provide: NbDialogRef, useValue: {} }] });
    component = TestBed.runInInjectionContext(() => new HistorySnapshotComponent());
  });

  it('renders lexicon edits even when topics are unchanged', () => {
    component.event = {
      id: '1',
      date: '',
      type: BotHistoryEventType.promptContext,
      snapshot: {
        previous: { lexiconGroups: [{ id: 1, terms: ['old'] }] },
        current: { lexiconGroups: [{ id: 1, terms: ['new'] }] }
      }
    };
    component.ngOnInit();
    expect(component.kvDiff.changedCount).toBe(1);
    expect(component.kvDiff.rows[0].after).toBe('new');
  });

  it('keeps prompt metadata visible independently from template changes', () => {
    component.event = {
      id: '1',
      date: '',
      type: BotHistoryEventType.ragSettings,
      snapshot: {
        previous: { questionAnsweringPrompt: { template: 'same', formatter: 'jinja2', inputs: { topic: 'old' } } },
        current: { questionAnsweringPrompt: { template: 'same', formatter: 'jinja2', inputs: { topic: 'new' } } }
      }
    };
    component.ngOnInit();
    expect(component.ragSections[0].kv.rows.some((row) => row.key === 'questionAnsweringPrompt.inputs.topic' && row.changed)).toBeTrue();
  });
});
