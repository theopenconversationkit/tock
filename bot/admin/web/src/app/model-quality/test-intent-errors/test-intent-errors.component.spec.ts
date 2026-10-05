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

import { TestIntentErrorsComponent } from './test-intent-errors.component';
import { TestSharedModule } from '../../shared/test-shared.module';
import { StateService } from '../../core-nlp/state.service';
import { StateServiceMock } from '../../shared/test-shared/state-service.mock';
import { QualityService } from '../quality.service';
import { of } from 'rxjs';

describe('TestIntentErrorsComponent', () => {
  let component: TestIntentErrorsComponent;
  let fixture: ComponentFixture<TestIntentErrorsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TestIntentErrorsComponent],
      imports: [TestSharedModule],
      providers: [
        { provide: StateService, useClass: StateServiceMock },
        {
          provide: QualityService,
          useValue: {
            getIntentsErrors: () => of({ total: 0, data: [] }),
            getEntitiesErrors: () => of({ total: 0, data: [] }),
            buildStats: () => of({ total: 0, data: [] }),
            searchIntentErrors: () => of({ total: 0, data: [] }),
            searchEntityErrors: () => of({ total: 0, data: [] })
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TestIntentErrorsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
