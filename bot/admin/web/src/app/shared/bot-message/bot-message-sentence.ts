/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

import {Component, EventEmitter, Input, Output} from "@angular/core";
import {BotMessage, Sentence} from "../model/dialog-data";

@Component({
  selector: 'tock-bot-message-sentence',
  template: `
  <div *ngIf="sentence.text" class="text">
    <img *ngIf="sentence.userInterface"
         src="/assets/images/{{sentence.userInterface}}.svg"
         class="userInterface"
         nbTooltip="{{sentence.userInterface}}"/>
     {{sentence.text}}
  </div>
  <div *ngIf="!sentence.text">
    <div *ngIf="sentence.messages.length === 1">
      <tock-sentence-element [element]="sentence.messages[0]" [user]="user" (sendMessage)="reply($event)"></tock-sentence-element>
    </div>
    <div *ngIf="sentence.messages.length > 1">
      <ul>
        <li *ngFor="let e of sentence.messages">
          ({{e.connectorType.id}})
          <tock-sentence-element [element]="e" [user]="user" (sendMessage)="reply($event)"></tock-sentence-element>
        </li>
      </ul>
    </div>
  </div>`,
  styleUrls: ['./bot-message-sentence.css']
})
export class BotMessageSentenceComponent {

  @Input()
  sentence: Sentence;

  @Input()
  user: boolean = false;

  @Output()
  sendMessage: EventEmitter<BotMessage> = new EventEmitter();

  reply(message: BotMessage) {
    this.sendMessage.emit(message);
  }
}
