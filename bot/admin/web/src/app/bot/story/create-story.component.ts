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

import {Component, ElementRef, OnInit, ViewChild} from "@angular/core";
import {MatSnackBar} from "@angular/material";
import {NlpService} from "../../nlp-tabs/nlp.service";
import {StateService} from "../../core-nlp/state.service";
import {NormalizeUtil} from "../../model/commons";
import {ParseQuery, Sentence} from "../../model/nlp";
import {BotService} from "../bot-service";
import {AnswerConfigurationType, CreateStoryRequest, IntentName, StoryDefinitionConfiguration} from "../model/story";
import {ActivatedRoute} from "@angular/router";
import {BotConfigurationService} from "../../core/bot-configuration.service";

@Component({
  selector: 'tock-create-story',
  templateUrl: './create-story.component.html',
  styleUrls: ['./create-story.component.css']
})
export class CreateStoryComponent implements OnInit {

  sentence: Sentence;
  text: string;
  intent: string;

  botConfigurationId: string;

  story: StoryDefinitionConfiguration;

  @ViewChild('newSentence') newSentence: ElementRef;

  constructor(private nlp: NlpService,
              public state: StateService,
              private botConfiguration: BotConfigurationService,
              private bot: BotService,
              private snackBar: MatSnackBar,
              private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.text = params["text"];
      if (this.text) {
        this.onSentence(this.text);
      }
      setTimeout(() => this.newSentence.nativeElement.focus(), 500);
    });
  }

  onSentence(value: string) {
    const app = this.state.currentApplication;
    const language = this.state.currentLocale;
    const v = value.trim();
    if (v.length == 0) {
      this.snackBar.open(`Please enter a non-empty sentence`, "ERROR", {duration: 2000});
    } else {
      this.nlp.parse(new ParseQuery(app.namespace, app.name, language, v, true)).subscribe(sentence => {
        this.sentence = sentence;
        this.initIntentName(v);
        this.createStory();
      });
    }
  }

  private createStory() {
    this.botConfiguration.configurations.subscribe(confs => {
      const botId = confs.find(c => c._id === this.botConfigurationId).botId;
      let intent = this.intent;
      this.story = new StoryDefinitionConfiguration(
        intent,
        botId,
        new IntentName(intent),
        AnswerConfigurationType.simple,
        this.state.user.organization,
        [],
        "build",
        intent
      )
    });
  }

  resetState() {
    this.sentence = null;
    this.story = null;
  }

  initIntentName(value: string) {
    const appName = this.state.currentApplication.name;
    const underscoreIndex = appName.indexOf("_");
    const prefix = underscoreIndex !== -1 ? appName.substring(0, Math.min(underscoreIndex, 5)) : appName.substring(0, Math.min(appName.length, 5));
    const v = NormalizeUtil.normalize(value.trim().toLowerCase()).replace(new RegExp(" ", 'g'), "_");
    let candidate = prefix + "_" + v.substring(0, Math.min(value.length, 10));
    let count = 1;
    const candidateBase = candidate;
    while (this.state.intentExists(candidate)) {
      candidate = candidateBase + (count++);
    }
    this.intent = candidate;
  }

  onIntentChange(e: string) {
    if (e) {
      this.createStory();
    } else {
      this.resetState();
    }
  }

  onReply() {
    if (this.state.intentExists(this.intent)) {
      this.snackBar.open(`Intent ${this.intent} already exists`, "Error", {duration: 5000});
      return;
    }
    let invalidMessage = this.story.currentAnswer().invalidMessage();
    if (invalidMessage) {
      this.snackBar.open(`Error: ${invalidMessage}`, "ERROR", {duration: 5000});
    } else {
      this.bot.newStory(
        new CreateStoryRequest(
          this.story,
          this.state.currentLocale,
          this.text ? [this.text.trim()] : []
        )
      ).subscribe(intent => {
        this.state.addIntent(intent);
        this.snackBar.open(`New story created for language ${this.state.currentLocale}`, "New Story", {duration: 3000});
        this.onClose();

        this.newSentence.nativeElement.focus();
      });
    }
  }

  onClose() {
    this.resetState();
    this.intent = null;
    this.text = null;
  }

}
