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

import { TokenViewComponent } from './token-view.component';
import { TestSharedModule } from '../../../../test-shared.module';
import { StateService } from '../../../../../core-nlp/state.service';
import { StateServiceMock } from '../../../../test-shared/state-service.mock';
import { Token } from './token.model';
import { SentenceTrainingService } from '../../sentence-training.service';
import { of } from 'rxjs';

describe('TokenViewComponent', () => {
  let component: TokenViewComponent;
  let fixture: ComponentFixture<TokenViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TokenViewComponent],
      imports: [TestSharedModule],
      providers: [
        { provide: StateService, useClass: StateServiceMock },
        { provide: SentenceTrainingService, useValue: { communication: of({}), documentClick: () => {} } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TokenViewComponent);
    component = fixture.componentInstance;

    component.token = new Token(0, 'hello', null as any);

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
