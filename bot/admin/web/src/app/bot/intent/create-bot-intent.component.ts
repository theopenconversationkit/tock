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
import {MdSnackBar} from "@angular/material";
import {NlpService} from "tock-nlp-admin/src/app/nlp-tabs/nlp.service";
import {StateService} from "tock-nlp-admin/src/app/core/state.service";
import {NormalizeUtil} from "tock-nlp-admin/src/app/model/commons";
import {ParseQuery, Sentence} from "tock-nlp-admin/src/app/model/nlp";
import {BotService} from "../bot-service";
import {AnswerConfigurationType, CreateBotIntentRequest} from "../model/bot-intent";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'tock-create-bot-intent',
  templateUrl: './create-bot-intent.component.html',
  styleUrls: ['./create-bot-intent.component.css']
})
export class CreateBotIntentComponent implements OnInit {

  sentence: Sentence;
  text: string;
  reply: string;
  script: string;
  intent: string;
  intentId: string;
  supportedConfigTypes = [AnswerConfigurationType.simple, AnswerConfigurationType.script];
  configType: AnswerConfigurationType = AnswerConfigurationType.simple;

  sentenceMode: boolean;
  intentMode: boolean;

  botConfigurationId: string;

  @ViewChild('newSentence') newSentence: ElementRef;

  //@ViewChild('newReply') newReply: ElementRef;

  constructor(private nlp: NlpService,
              private state: StateService,
              private bot: BotService,
              private snackBar: MdSnackBar,
              private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.text = params["text"];
      if (this.text) {
        this.onSentence(this.text);
      }
      window.setTimeout(() => this.newSentence.nativeElement.focus(), 500);
    });
  }

  getTypeLabel(type: AnswerConfigurationType): string {
    switch (type) {
      case AnswerConfigurationType.simple :
        return "Simple";
      case AnswerConfigurationType.script :
        return "Script";
      default :
        return "Unknown";
    }
  }

  onSentence(value: string) {
    const app = this.state.currentApplication;
    const language = this.state.currentLocale;
    const v = value.trim();
    if (v.length == 0) {
      this.snackBar.open(`Please enter a non-empty sentence`, "ERROR", {duration: 2000});
    } else {
      this.intentMode = false;
      this.sentenceMode = true;
      this.nlp.parse(new ParseQuery(app.namespace, app.name, language, v, true)).subscribe(sentence => {
        this.sentence = sentence;
        this.initIntentName(v);
        //setTimeout(_ => this.newReply.nativeElement.focus(), 100);
      });
    }
  }

  resetState() {
    this.sentenceMode = false;
    this.intentMode = false;
    this.sentence = null;
    this.intentId = null;
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
    this.script = "import fr.vsct.tock.bot.definition.story\n" +
      "\n" +
      "val greetings = story(\"" + this.intent + "\") { \n" +
      "           end(\"Hello World! :)\")\n" +
      "}"
  }

  isSimpleAnswer(): boolean {
    return this.configType === AnswerConfigurationType.simple;
  }

  isScriptAnswer(): boolean {
    return this.configType === AnswerConfigurationType.script;
  }

  onIntentChange(e:string) {
    if(e) {
      this.intentMode = true;
      this.sentenceMode = false;
      this.intentId = e;
    } else {
      this.resetState();
    }
  }

  onReply() {
    if (this.sentenceMode && this.state.intentExists(this.intent)) {
      this.snackBar.open(`Intent ${this.intent} already exists`, "Error", {duration: 5000});
      return;
    }
    const answer = this.isScriptAnswer() ? this.script : this.reply;
    if (!answer || answer.trim().length === 0) {
      this.snackBar.open(`Please set a non empty reply`, "Error", {duration: 5000});
      return;
    }
    this.bot.newBotIntent(
      new CreateBotIntentRequest(
        this.botConfigurationId,
        this.intent,
        this.state.currentLocale,
        this.text ? [this.text.trim()] : [],
        this.configType,
        answer,
        this.intentId
      )
    ).subscribe(intent => {
      this.state.currentApplication.intents.push(intent);
      this.snackBar.open(`New answer saved for language ${this.state.currentLocale}`, "Answer Saved", {duration: 3000});
      this.onClose();

      this.newSentence.nativeElement.focus();
    });
  }

  onClose() {
    this.resetState();
    this.reply = null;
    this.intent = null;
    this.text = null;
  }

}
