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

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';

import { KnowledgeBaseEntriesImportComponent } from './entries-import.component';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { KnowledgeBaseService } from '../services/knowledge-base.service';
import { TestSharedModule } from '../../shared/test-shared.module';

function conf(namespace: string, botId: string): BotApplicationConfiguration {
  return { namespace, botId } as unknown as BotApplicationConfiguration;
}

describe('KnowledgeBaseEntriesImportComponent', () => {
  let component: KnowledgeBaseEntriesImportComponent;
  // BehaviorSubject, not of(): the component relies on the replay of the opened bot on subscription.
  let configurations$: BehaviorSubject<BotApplicationConfiguration[]>;
  let router: Router;

  beforeEach(async () => {
    configurations$ = new BehaviorSubject<BotApplicationConfiguration[]>([]);

    await TestBed.configureTestingModule({
      declarations: [KnowledgeBaseEntriesImportComponent],
      imports: [TestSharedModule],
      providers: [
        { provide: BotConfigurationService, useValue: { configurations: configurations$ } },
        { provide: KnowledgeBaseService, useValue: {} }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    const fixture = TestBed.createComponent(KnowledgeBaseEntriesImportComponent);
    component = fixture.componentInstance;

    router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl').and.returnValue(Promise.resolve(true));
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('bot change redirect', () => {
    it('records the opened bot without redirecting', () => {
      // ngOnInit replays [] (empty, ignored), then the opened bot arrives.
      component.ngOnInit();
      configurations$.next([conf('ns', 'botA')]);

      expect(router.navigateByUrl).not.toHaveBeenCalled();
    });

    it('does not redirect when the same bot is re-emitted', () => {
      component.ngOnInit();
      configurations$.next([conf('ns', 'botA')]);
      configurations$.next([conf('ns', 'botA')]);

      expect(router.navigateByUrl).not.toHaveBeenCalled();
    });

    it('redirects to the knowledge base list when the bot changes', () => {
      component.ngOnInit();
      configurations$.next([conf('ns', 'botA')]);
      configurations$.next([conf('ns', 'botB')]);

      expect(router.navigateByUrl).toHaveBeenCalledOnceWith('/knowledge-base');
    });

    it('ignores an empty emission rather than treating it as a change', () => {
      component.ngOnInit();
      configurations$.next([conf('ns', 'botA')]);
      configurations$.next([]);

      expect(router.navigateByUrl).not.toHaveBeenCalled();

      // The recorded bot is unchanged, so a genuinely different bot still redirects.
      configurations$.next([conf('ns', 'botB')]);
      expect(router.navigateByUrl).toHaveBeenCalledOnceWith('/knowledge-base');
    });
  });
});
