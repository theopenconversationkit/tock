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
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { NbToastrService } from '@nebular/theme';
import { of } from 'rxjs';

import { RestService } from '../../core-nlp/rest/rest.service';
import { StateService } from '../../core-nlp/state.service';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { FaqManagementComponent } from './faq-management.component';
import { Location } from '@angular/common';

class BotConfigurationServiceMock {
  configurations = of([
    {
      applicationId: 'new_assistant',
      botId: 'new_assistant',
      namespace: 'app',
      nlpModel: 'new_assistant',
      connectorType: { id: 'web', userInterfaceType: 'textChat' },
      ownerConnectorType: null,
      name: 'new_assistant',
      baseUrl: 'http://bot_api:8080',
      parameters: {},
      path: '/io/app/new_assistant/web',
      _id: '1',
      targetConfigurationId: null
    }
  ]);
}

class StateServiceMock {
  createPaginatedQuery() {
    return {
      namespace: 'app',
      application: 'app',
      language: 'fr',
      start: 0,
      size: 10
    };
  }

  hasRole() {
    return true;
  }
}

describe('FaqManagementComponent', () => {
  let component: FaqManagementComponent;
  let fixture: ComponentFixture<FaqManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FaqManagementComponent],
      imports: [],
      providers: [
        { provide: BotConfigurationService, useClass: BotConfigurationServiceMock },
        {
          provide: RestService,
          useValue: {
            post: () =>
              of({
                end: 0,
                rows: [],
                start: 0,
                total: 0
              })
          }
        },
        { provide: StateService, useClass: StateServiceMock },
        { provide: NbToastrService, useValue: {} },
        {
          provide: Location,
          useValue: {
            getState: () => null
          }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FaqManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
