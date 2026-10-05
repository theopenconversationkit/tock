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

import { DialogComponent } from './dialog.component';
import { TestSharedModule } from '../../shared/test-shared.module';
import { AnalyticsService } from '../analytics.service';
import { of } from 'rxjs';
import { StateServiceMock } from '../../shared/test-shared/state-service.mock';
import { StateService } from '../../core-nlp/state.service';
import { ApplicationService } from '../../core-nlp/applications.service';
import { AuthService } from '../../core-nlp/auth/auth.service';
import { SettingsService } from '../../core-nlp/settings.service';
import { BotSharedService } from '../../shared/bot-shared.service';

describe('DialogComponent', () => {
  let component: DialogComponent;
  let fixture: ComponentFixture<DialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DialogComponent],
      imports: [TestSharedModule],
      providers: [
        {
          provide: AnalyticsService,
          useValue: {
            getUsersAnalytics: () => of([]),
            getDialogsAnalytics: () => of([]),
            findDialog: () => of({}),
            search: () => of({ rows: [], total: 0 })
          }
        },
        { provide: StateService, useClass: StateServiceMock },
        { provide: ApplicationService, useValue: {} },
        { provide: AuthService, useValue: {} },
        { provide: SettingsService, useValue: {} },
        { provide: BotSharedService, useValue: { getDialogWithNlpStats: () => of(null) } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
