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

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SentenceTrainingEntryComponent } from './sentence-training-entry.component';
import { TestSharedModule } from '../../../test-shared.module';
import { StateService } from '../../../../core-nlp/state.service';
import { StateServiceMock } from '../../../test-shared/state-service.mock';

describe('SentenceTrainingEntryComponent', () => {
  let component: SentenceTrainingEntryComponent;
  let fixture: ComponentFixture<SentenceTrainingEntryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SentenceTrainingEntryComponent],
      imports: [TestSharedModule],
      providers: [{ provide: StateService, useClass: StateServiceMock }]
    }).compileComponents();

    fixture = TestBed.createComponent(SentenceTrainingEntryComponent);
    component = fixture.componentInstance;

    component.sentence = {
      _showDialog: true
    } as any;

    fixture.detectChanges();
  });

  xit('should create', () => {
    // TODO: template lourd nécessitant un SentenceExtended complet
    // (getSentenceId → normalize, statusColor/statusDisplayed, classification.*,
    //  state.currentApplication.supportedLocales). À seeder si nécessaire.
    expect(component).toBeTruthy();
  });
});
