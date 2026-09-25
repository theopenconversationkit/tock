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
import { StateService } from '../core-nlp/state.service';
import { DialogService } from '../core-nlp/dialog.service';
import { BotConfigurationService } from '../core/bot-configuration.service';
import { DashboardService } from './services/dashboard.service';
import { DashboardStateService } from './services/dashboard-state.service';
import { BehaviorSubject, NEVER, of, Subject } from 'rxjs';
import { DashboardComponent } from './dashboard.component';
import { BotHistoryEventType, BotHistoryPage } from './models/dashboard.model';

describe('Dashboard history pagination', () => {
  it('appends pages and ignores pending responses after a bot change', () => {
    const configurations = new BehaviorSubject([]);
    const initial = new Subject<BotHistoryPage>();
    const more = new Subject<BotHistoryPage>();
    const other = new Subject<BotHistoryPage>();
    const state = { currentApplication: { namespace: 'ns', name: 'first' }, hasRole: () => false };
    const service = jasmine.createSpyObj('DashboardService', [
      'getUsage',
      'getAnswerOutcome',
      'getTopics',
      'getContacts',
      'getBotIdentity',
      'getEvaluationSamples',
      'getBotHistory'
    ]);
    ['getUsage', 'getAnswerOutcome', 'getTopics', 'getContacts', 'getBotIdentity', 'getEvaluationSamples'].forEach((method) => {
      service[method].and.returnValue(NEVER);
    });
    service.getBotHistory.and.callFake((_namespace: string, bot: string, before?: string) =>
      bot === 'other' ? other : before ? more : initial
    );
    const dashboardState = { period: 30, displayTests: false, period$: of(30), displayTests$: of(false), applyBotContext: () => {} };
    TestBed.configureTestingModule({
      providers: [
        { provide: StateService, useValue: state },
        { provide: BotConfigurationService, useValue: { configurations } },
        { provide: DashboardService, useValue: service },
        { provide: DashboardStateService, useValue: dashboardState },
        { provide: DialogService, useValue: {} }
      ]
    });
    const component = TestBed.runInInjectionContext(() => new DashboardComponent());
    component.ngOnInit();
    const event = { id: 'first', date: '', type: BotHistoryEventType.created };
    initial.next({ events: [event], hasMore: true, nextCursor: 'cursor' });
    component.loadMoreHistory();
    expect(service.getBotHistory).toHaveBeenCalledWith('ns', 'first', 'cursor');
    more.next({ events: [{ ...event, id: 'second' }], hasMore: true, nextCursor: 'next' });
    expect(component.history.map((item) => item.id)).toEqual(['first', 'second']);
    component.loadMoreHistory();
    state.currentApplication = { namespace: 'ns', name: 'other' };
    configurations.next([]);
    other.next({ events: [{ ...event, id: 'other' }], hasMore: false, nextCursor: null });
    more.next({ events: [{ ...event, id: 'obsolete' }], hasMore: false, nextCursor: null });
    expect(component.history.map((item) => item.id)).toEqual(['other']);
    expect(component.historyCursor).toBeNull();
    component.ngOnDestroy();
  });
});
