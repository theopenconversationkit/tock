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

import { CreateRuleComponent } from './create-rule.component';
import { TestSharedModule } from '../../../../shared/test-shared.module';
import { NbAutocompleteModule, NbDialogRef, NbToggleModule } from '@nebular/theme';
import { of } from 'rxjs';
import { StateServiceMock } from '../../../../shared/test-shared/state-service.mock';
import { StateService } from '../../../../core-nlp/state.service';

describe('CreateRuleComponent', () => {
  let component: CreateRuleComponent;
  let fixture: ComponentFixture<CreateRuleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CreateRuleComponent],
      imports: [TestSharedModule, NbToggleModule, NbAutocompleteModule],
      providers: [
        { provide: StateService, useClass: StateServiceMock },
        { provide: NbDialogRef, useValue: { onClose: of(null), close: () => {} } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CreateRuleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
