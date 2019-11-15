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

import {Component, ElementRef, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {MatSnackBar} from "@angular/material";
import {NlpService} from "../../nlp-tabs/nlp.service";
import {StateService} from "../../core-nlp/state.service";
import {NormalizeUtil} from "../../model/commons";
import {ParseQuery, Sentence} from "../../model/nlp";
import {BotService} from "../bot-service";
import {
  AnswerConfigurationType,
  CreateStoryRequest,
  IntentName,
  StoryDefinitionConfiguration,
  StorySearchQuery
} from "../model/story";
import {ActivatedRoute} from "@angular/router";
import {BotConfigurationService} from "../../core/bot-configuration.service";
import {AnswerController} from "./controller";
import {Subscription} from "rxjs";

@Component({
  selector: 'tock-create-story',
  templateUrl: './create-story.component.html',
  styleUrls: ['./create-story.component.css']
})
export class CreateStoryComponent implements OnInit, OnDestroy {

  sentence: Sentence;
  displayStory: boolean = false;

  botConfigurationId: string;

  story: StoryDefinitionConfiguration;
  submit = new AnswerController();
  textRetrieved: boolean = false;

  @ViewChild('newSentence', {static: false}) newSentence: ElementRef;

  private stories: StoryDefinitionConfiguration[] = [];

  private subscription: Subscription;

  constructor(private nlp: NlpService,
              public state: StateService,
              private botConfiguration: BotConfigurationService,
              private bot: BotService,
              private snackBar: MatSnackBar,
              private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.load();
    this.subscription = this.state.configurationChange.subscribe(_ => this.load());
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  private load() {
    this.sentence = undefined;
    this.displayStory = false;
    this.createStory();
    const _this = this;
    this.submit.submitListener = _ => _this.onReply();
    this.bot.getStories(
      new StorySearchQuery(
        this.state.currentApplication.namespace,
        this.state.currentApplication.name,
        this.state.currentLocale,
        0,
        10000
      )).subscribe(s => {
      this.stories = s;
    });
  }

  onSentence(value?: string) {
    const app = this.state.currentApplication;
    const language = this.state.currentLocale;
    const v = value ? value.trim() : this.story.userSentence.trim();
    if (v.length == 0) {
      this.snackBar.open(`Please enter a non-empty sentence`, "ERROR", {duration: 2000});
    } else {
      this.nlp.parse(new ParseQuery(app.namespace, app.name, language, v, true)).subscribe(sentence => {
        this.sentence = sentence;
        const intent = this.initIntentName(v, sentence.classification.intentId);
        this.story.userSentence = v;
        this.story.storyId = intent;
        this.story.intent = new IntentName(intent);
        this.story.name = v;
        this.displayStory = true;
      });
    }
  }

  private createStory() {
    const _this = this;
    if (this.botConfigurationId) {
      const confs = this.botConfiguration.configurations.getValue();
      const conf = confs.find(c => c._id === this.botConfigurationId);
      if (conf) {
        this.story = new StoryDefinitionConfiguration(
          "",
          conf.botId,
          new IntentName(""),
          AnswerConfigurationType.simple,
          this.state.user.organization,
          [],
          "build",
          "",
          "",
          []
        );
        if (!this.textRetrieved) {
          this.route.queryParams.subscribe(params => {
            this.textRetrieved = true;
            const text = params["text"];
            if (text) {
              this.story.userSentence = text;
              this.onSentence(text);
            } else {
              setTimeout(_ => _this.newSentence.nativeElement.focus(), 500);
            }
          });
        }
      }
    } else {
      setTimeout(_ => _this.createStory(), 200);
    }
  }

  resetState() {
    this.sentence = null;
    this.displayStory = false;
    this.createStory();
    setTimeout(_ => this.newSentence.nativeElement.focus(), 200);
  }

  private initIntentName(sentence: string, intentId?: string): string {
    if (intentId) {
      const intent = this.state.findIntentById(intentId);
      if (intent) {
        const story = this.stories.find(s => s.intent.name === intent.name && !s.isBuiltIn());
        //if there is no existing story with this intent, select the intent
        if (!story) {
          return intent.name;
        }
      }
    }
    //else suggest a new intent
    const v = NormalizeUtil.normalize(sentence.trim().toLowerCase()).replace(new RegExp(" ", 'g'), "_");
    let candidate = v.substring(0, Math.min(sentence.length, 10));
    let count = 1;
    const candidateBase = candidate;
    while (this.state.intentExists(candidate)) {
      candidate = candidateBase + (count++);
    }
    return candidate;
  }

  onReply() {
    this.submit.checkAnswer(_ => {
        let invalidMessage = this.story.currentAnswer().invalidMessage();
        if (invalidMessage) {
          this.snackBar.open(`Error: ${invalidMessage}`, "ERROR", {duration: 5000});
        } else {
          this.story.steps = this.story.steps.filter(s => !s.new);
          this.bot.newStory(
            new CreateStoryRequest(
              this.story,
              this.state.currentLocale,
              [this.story.userSentence.trim()]
            )
          ).subscribe(intent => {
            this.state.resetConfiguration();
            this.snackBar.open(`New story ${this.story.name} created for language ${this.state.currentLocale}`, "New Story", {duration: 3000});

            this.newSentence.nativeElement.focus();
            setTimeout(_ => this.resetState(), 200);
          });
        }
      }
    );
  }

}
