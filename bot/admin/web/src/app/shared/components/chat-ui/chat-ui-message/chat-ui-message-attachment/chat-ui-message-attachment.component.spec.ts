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
import { Attachment } from '../../../../model/dialog-data';

import { ChatUiMessageAttachmentComponent } from './chat-ui-message-attachment.component';

const attachment = new Attachment(0, 'http://bot_api:8080/f/appf728cff0-40ee-456b-88d3-41a873f696e4.txt', 3);

describe('ChatUiMessageAttachmentComponent', () => {
  let component: ChatUiMessageAttachmentComponent;
  let fixture: ComponentFixture<ChatUiMessageAttachmentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ChatUiMessageAttachmentComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ChatUiMessageAttachmentComponent);
    component = fixture.componentInstance;
    component.attachment = attachment;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
