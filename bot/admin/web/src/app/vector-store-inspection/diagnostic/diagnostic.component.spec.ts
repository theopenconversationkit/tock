/*
 * Copyright (C) 2017/2026 SNCF Connect & Tech
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

import { Location } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { TranslocoService } from '@jsverse/transloco';
import { NbToastrService } from '@nebular/theme';
import { BehaviorSubject, of } from 'rxjs';

import { BotConfigurationService } from '../../core/bot-configuration.service';
import { DocumentSearchType } from '../../rag/rag-settings/models/engines-configurations';
import { RetrievalSettings } from '../models/vector-store-inspection.models';
import { VectorStoreInspectionService } from '../services/vector-store-inspection.service';
import { VectorStoreInspectionStateService } from '../services/vector-store-inspection-state.service';
import { DiagnosticComponent } from './diagnostic.component';

const BOT_SETTINGS: RetrievalSettings = {
  ragEnabled: true,
  searchType: DocumentSearchType.SIMILARITY_SEARCH,
  maxDocumentsRetrieved: 3,
  compressionEnabled: true,
  minScore: 0.5,
  maxDocuments: 1,
  fillToMaxDocuments: false,
  provider: 'VllmRerank',
  model: 'model'
};

describe('DiagnosticComponent — reset to bot settings', () => {
  let component: DiagnosticComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DiagnosticComponent],
      providers: [
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: new Map() } } },
        { provide: Location, useValue: { getState: () => null } },
        { provide: BotConfigurationService, useValue: { configurations: new BehaviorSubject([]) } },
        { provide: VectorStoreInspectionService, useValue: { getRetrievalSettings: () => of(BOT_SETTINGS) } },
        { provide: NbToastrService, useValue: { warning: () => undefined } },
        { provide: TranslocoService, useValue: { translate: (key: string) => key } },
        { provide: VectorStoreInspectionStateService, useValue: { setSearchType: () => undefined } }
      ]
    }).overrideTemplate(DiagnosticComponent, '');

    // Logic only: the template and ngOnInit subscriptions are not needed here.
    component = TestBed.createComponent(DiagnosticComponent).componentInstance;
    component.botSettings = BOT_SETTINGS;
  });

  it('is ignored while a search is in flight, so the form keeps describing that search', () => {
    component.maxDocuments = 3;
    component.k = 5;
    component.loading = true;

    component.resetToBotSettings();

    expect(component.maxDocuments).toBe(3);
    expect(component.k).toBe(5);
  });

  it('applies the bot settings once no search is running', () => {
    component.maxDocuments = 3;
    component.k = 5;
    component.loading = false;

    component.resetToBotSettings();

    expect(component.maxDocuments).toBe(1);
    expect(component.k).toBe(3);
  });
});
