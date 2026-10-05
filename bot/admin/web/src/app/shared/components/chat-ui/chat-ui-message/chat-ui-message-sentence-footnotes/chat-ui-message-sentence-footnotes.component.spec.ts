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

import { ChatUiMessageSentenceFootnotesComponent } from './chat-ui-message-sentence-footnotes.component';
import { TestSharedModule } from '../../../../test-shared.module';
import { ResilientDatePipe } from '../../../../pipes/resilient-date.pipe';

describe('ChatUiMessageSentenceFootnotesComponent', () => {
  let component: ChatUiMessageSentenceFootnotesComponent;
  let fixture: ComponentFixture<ChatUiMessageSentenceFootnotesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ChatUiMessageSentenceFootnotesComponent],
      imports: [TestSharedModule],
      providers: [{ provide: ResilientDatePipe, useValue: { transform: (v) => v } }]
    }).compileComponents();

    fixture = TestBed.createComponent(ChatUiMessageSentenceFootnotesComponent);
    component = fixture.componentInstance;

    component.sentence = { userInterface: undefined } as any;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
