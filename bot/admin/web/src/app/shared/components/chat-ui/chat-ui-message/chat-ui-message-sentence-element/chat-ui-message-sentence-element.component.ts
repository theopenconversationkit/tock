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

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { BotMessage, SentenceElement } from '../../../../model/dialog-data';
import linkifyHtml from 'linkify-html';
@Component({
  selector: 'tock-chat-ui-message-sentence-element',
  templateUrl: './chat-ui-message-sentence-element.component.html',
  styleUrls: ['./chat-ui-message-sentence-element.component.scss']
})
export class ChatUiMessageSentenceElementComponent {
  @Input() element: SentenceElement;

  @Input() replay: boolean;

  @Input() reply: boolean = false;

  @Output() sendMessage: EventEmitter<BotMessage> = new EventEmitter();

  linkifyHtml(str) {
    return linkifyHtml(str, { target: '_blank' });
  }

  replyMessage(message: BotMessage) {
    this.sendMessage.emit(message);
  }
}
