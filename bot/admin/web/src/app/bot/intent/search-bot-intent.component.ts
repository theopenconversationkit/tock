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

import {Component, OnInit} from "@angular/core";
import {BotService} from "../bot-service";
import {MdSnackBar} from "@angular/material";
import {NlpService} from "tock-nlp-admin/src/app/nlp-tabs/nlp.service";
import {StateService} from "tock-nlp-admin/src/app/core/state.service";
import {BotIntent, BotIntentSearchQuery, UpdateBotIntentRequest} from "../model/bot-intent";

@Component({
  selector: 'tock-search-bot-intent',
  templateUrl: './search-bot-intent.component.html',
  styleUrls: ['./search-bot-intent.component.css']
})
export class SearchBotIntentComponent implements OnInit {

  intents: BotIntent[];

  constructor(private nlp: NlpService,
              private state: StateService,
              private bot: BotService,
              private snackBar: MdSnackBar) {
  }

  ngOnInit(): void {
    this.bot.getBotIntents(
      new BotIntentSearchQuery(
        this.state.currentApplication.namespace,
        this.state.currentApplication.name,
        this.state.currentLocale,
        0,
        10
      )).subscribe(intents => {
      this.intents = intents;
      this.intents.forEach(i => i.storyDefinition.initTextAnswer());
    })
  }

  delete(intent: BotIntent) {
    this.bot.deleteBotIntent(intent.storyDefinition._id)
      .subscribe(_ => {
        this.ngOnInit();
        this.snackBar.open(`Answer deleted`, "Delete", {duration: 2000})
      });
  }

  update(intent: BotIntent) {
    this.bot.updateBotIntent(
      new UpdateBotIntentRequest(
        intent.storyDefinition._id,
        this.state.currentLocale,
        intent.storyDefinition.textAnswer
      ))
      .subscribe(_ => {
        this.ngOnInit();
        this.snackBar.open(`Answer updated`, "Update", {duration: 2000})
      });
  }
}
