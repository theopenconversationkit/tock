/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import {Component, Input, OnInit} from '@angular/core';
import {
  AnswerConfigurationType,
  AnswerContainer,
  ScriptAnswerConfiguration,
  ScriptAnswerVersionedConfiguration,
  SimpleAnswerConfiguration
} from '../model/story';
import {BotService} from '../bot-service';
import {MatDialog} from '@angular/material/dialog';
import {MatRadioChange} from '@angular/material/radio';
import {StateService} from '../../core-nlp/state.service';
import {AnswerDialogComponent} from './answer-dialog.component';
import {AnswerController} from './controller';
import {BotSharedService} from '../../shared/bot-shared.service';
import {DialogService} from '../../core-nlp/dialog.service';

@Component({
  selector: 'tock-answer',
  templateUrl: './answer.component.html',
  styleUrls: ['./answer.component.css']
})
export class AnswerComponent implements OnInit {

  @Input()
  answer: AnswerContainer;

  @Input()
  fullDisplay = false;

  @Input()
  editable = true;

  @Input()
  create = false;

  @Input()
  answerLabel = 'Answer';

  @Input()
  submit: AnswerController = new AnswerController();

  @Input()
  wide = false;

  constructor(private state: StateService,
              private bot: BotService,
              private dialog: DialogService,
              private matDialog: MatDialog,
              public shared: BotSharedService) {
  }

  ngOnInit(): void {
    if (!this.answer.currentAnswer()) {
      this.changeAnswerType(this.answer.currentType);
    }
  }

  editAnswer() {
    this.dialog.open(
      this.matDialog,
      AnswerDialogComponent,
      {
        data:
          {
            answer: this.answer,
            create: this.create,
            answerLabel: this.answerLabel
          }
      }
    );
  }

  changeType(event: MatRadioChange) {
    this.changeAnswerType(event.value);
  }

  private changeAnswerType(value: AnswerConfigurationType) {
    this.answer.changeCurrentType(value);
    if (value === AnswerConfigurationType.simple) {
      if (!this.answer.simpleAnswer()) {
        const newAnswer = new SimpleAnswerConfiguration([]);
        newAnswer.allowNoAnswer = this.answer.allowNoAnwser();
        this.answer.addNewAnswerType(newAnswer);
      }
    } else if (value === AnswerConfigurationType.script) {
      if (!this.answer.scriptAnswer()) {
        const s = 'import ai.tock.bot.definition.story\n' +
          '\n' +
          'val s = story("' + this.answer.containerId() + '") { \n' +
          '           end("Hello World! :)")\n' +
          '}';
        const script = new ScriptAnswerVersionedConfiguration(s);
        this.answer.addNewAnswerType(new ScriptAnswerConfiguration([script], script));
      }
    }
  }

}

