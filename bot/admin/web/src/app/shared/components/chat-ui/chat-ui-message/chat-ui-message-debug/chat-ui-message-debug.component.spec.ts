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
import { NbDialogService } from '@nebular/theme';
import { Debug } from '../../../../model/dialog-data';

import { ChatUiMessageDebugComponent } from './chat-ui-message-debug.component';

const mockDebugMessage = {
  text: 'title1',
  data: {
    level: 'INFO',
    message: 'This is a debug message',
    errors: ['error 578', 'error 24']
  },
  eventType: 'debug',
  delay: 0
};

describe('ChatUiMessageDebugComponent', () => {
  let component: ChatUiMessageDebugComponent;
  let fixture: ComponentFixture<ChatUiMessageDebugComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ChatUiMessageDebugComponent],
      providers: [
        {
          provide: NbDialogService,
          useValue: {
            open: () => {}
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ChatUiMessageDebugComponent);
    component = fixture.componentInstance;
    component.message = mockDebugMessage as Debug;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
