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

import {Component, Input} from "@angular/core";
import {Choice} from "../dialog-data";
@Component({
  selector: 'tock-bot-message-choice',
  template: `
    <span *ngIf="user" mdTooltip="{{parameters()}}">[Choice] {{choice.intentName}}</span>
    <span *ngIf="!user" mdTooltip="{{parameters()}}">
      <span *ngIf="url()">
        <span *ngIf="title()">
        <button (click)="redirect()" md-button color="warn">{{title()}}</button> 
      </span>
      <span *ngIf="!title()">
        <button (click)="redirect()" md-button color="warn">{{choice.intentName}}</button>
      </span>
      </span>
      <span *ngIf="!url()">
      <span *ngIf="title()">
        <button md-button color="primary">{{title()}}</button> 
      </span>
      <span *ngIf="!title()">
        <button md-button color="primary">{{choice.intentName}}</button>
      </span>
      </span>
    </span>
  `
})
export class BotMessageChoiceComponent {

  @Input()
  choice: Choice;

  @Input()
  user: boolean = false;

  title(): String {
    return this.choice.parameters.get("_title");
  }

  url(): String {
    return this.choice.parameters.get("_url");
  }

  redirect() {
    window.open(this.url() as string, "_blank");
  }

  parameters(): string {
    if (this.choice.parameters.size === 0) {
      return "";
    }
    let r = "";
    const separator = " & ";
    if (this.title() && !this.url()) {
      r += ("intent = " + this.choice.intentName + separator)
    }
    this.choice.parameters.forEach((v, k) => {
      if (k !== "_title") r += (k + " = " + v + separator)
    });
    if (r.endsWith(separator)) {
      r = r.substring(0, r.length - separator.length);
    }
    return r.trim();
  }
}
