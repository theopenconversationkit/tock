/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Component, EventEmitter, Input, Output} from "@angular/core";
import {BotMessage} from "../model/dialog-data";
@Component({
  selector: 'tock-bot-message',
  templateUrl: './bot-message.component.html',
  styleUrls: ['./bot-message.component.css']
})
export class BotMessageComponent {

  @Input()
  message: BotMessage;

  @Output()
  reply: EventEmitter<BotMessage> = new EventEmitter();

  @Input()
  user: boolean = false;

  sendReply(message: BotMessage) {
    this.reply.emit(message);
  }
}
