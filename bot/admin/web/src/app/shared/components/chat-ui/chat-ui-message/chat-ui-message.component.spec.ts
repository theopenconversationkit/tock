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
import { DomSanitizer } from '@angular/platform-browser';
import { Sentence } from '../../../model/dialog-data';

import { ChatUiMessageComponent } from './chat-ui-message.component';

const message = new Sentence(0, [], 'test');

describe('ChatUiMessageComponent', () => {
  let component: ChatUiMessageComponent;
  let fixture: ComponentFixture<ChatUiMessageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ChatUiMessageComponent],
      providers: [
        {
          provide: DomSanitizer,
          useValue: {
            bypassSecurityTrustStyle: (str) => str
          }
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ChatUiMessageComponent);
    component = fixture.componentInstance;
    component.message = message;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
