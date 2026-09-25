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

import { TestEntityErrorsComponent } from './test-entity-errors.component';
import { TestSharedModule } from '../../shared/test-shared.module';
import { StateService } from '../../core-nlp/state.service';
import { StateServiceMock } from '../../shared/test-shared/state-service.mock';
import { QualityService } from '../quality.service';
import { of } from 'rxjs';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('TestEntityErrorsComponent', () => {
  let component: TestEntityErrorsComponent;
  let fixture: ComponentFixture<TestEntityErrorsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TestEntityErrorsComponent],
      imports: [TestSharedModule],
      providers: [
        { provide: StateService, useClass: StateServiceMock },
        { provide: QualityService, useValue: { searchEntityErrors: () => of({ total: 0, data: [] }) } }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(TestEntityErrorsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
