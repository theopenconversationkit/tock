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
import { BotMessage, Choice } from '../../../../model/dialog-data';

@Component({
  selector: 'tock-chat-ui-message-choice',
  templateUrl: './chat-ui-message-choice.component.html',
  styleUrls: ['./chat-ui-message-choice.component.scss']
})
export class ChatUiMessageChoiceComponent {
  @Input() choice: Choice;

  @Input() replay: boolean;

  @Input() reply: boolean;

  @Output()
  replyMessage: EventEmitter<BotMessage> = new EventEmitter();

  title(): String {
    return this.choice.parameters.get('_title');
  }

  url(): String {
    return this.choice.parameters.get('_url');
  }

  click() {
    if (this.url()) {
      this.redirect();
      //TODO emit redirect event
    } else {
      this.replyMessage.emit(new Choice(0, this.choice.intentName, this.choice.parameters));
    }
  }

  redirect() {
    window.open(this.url() as string, '_blank');
  }

  parameters(): string {
    if (this.choice.parameters.size === 0) {
      return '';
    }
    let r = '';
    const separator = ' & ';
    if (this.title() && !this.url()) {
      r += 'intent = ' + this.choice.intentName + separator;
    }
    this.choice.parameters.forEach((v, k) => {
      if (k !== '_title') r += k + ' = ' + v + separator;
    });
    if (r.endsWith(separator)) {
      r = r.substring(0, r.length - separator.length);
    }
    return r.trim();
  }
}
