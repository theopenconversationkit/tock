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

import { SentenceGenerationSettingsComponent } from './sentence-generation-settings.component';
import { TestSharedModule } from '../../shared/test-shared.module';
import { StateService } from '../../core-nlp/state.service';
import { StateServiceMock } from '../../shared/test-shared/state-service.mock';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { of } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('SentenceGenerationSettingsComponent', () => {
  let component: SentenceGenerationSettingsComponent;
  let fixture: ComponentFixture<SentenceGenerationSettingsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SentenceGenerationSettingsComponent],
      imports: [TestSharedModule],
      providers: [
        { provide: StateService, useClass: StateServiceMock },
        {
          provide: BotConfigurationService,
          useValue: {
            configurations: of([]),
            restConfigurations: of([]),
            hasRestConfigurations: of(false),
            supportedConnectors: of([]),
            bots: of([])
          }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(SentenceGenerationSettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
