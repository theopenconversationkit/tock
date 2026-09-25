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

import { TestDialogComponent } from './test-dialog.component';
import { TestSharedModule } from '../../test-shared.module';
import { BotConfigurationService } from '../../../core/bot-configuration.service';
import { BehaviorSubject, Subject } from 'rxjs';
import { StateServiceMock } from '../../test-shared/state-service.mock';
import { StateService } from '../../../core-nlp/state.service';
import { NbWindowRef } from '@nebular/theme';

describe('TestDialogComponent', () => {
  let component: TestDialogComponent;
  let fixture: ComponentFixture<TestDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TestDialogComponent],
      imports: [TestSharedModule],
      providers: [
        {
          provide: BotConfigurationService,
          useValue: {
            configurations: new BehaviorSubject([]),
            restConfigurations: new BehaviorSubject([]),
            getConfigurationById: () => undefined,
            updateConfigurations: () => {}
          }
        },
        { provide: StateService, useClass: StateServiceMock },
        {
          provide: NbWindowRef,
          useValue: {
            close: () => {},
            config: {},
            stateChange: new Subject()
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TestDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
