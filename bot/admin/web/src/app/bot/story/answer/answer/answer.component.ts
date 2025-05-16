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

import { Component, Input, OnInit } from '@angular/core';
import { NbDialogService } from '@nebular/theme';

import {
  AnswerConfigurationType,
  AnswerContainer,
  ScriptAnswerConfiguration,
  ScriptAnswerVersionedConfiguration,
  SimpleAnswerConfiguration
} from '../../../model/story';
import { AnswerDialogComponent } from '../answer-dialog/answer-dialog.component';
import { AnswerController } from './../../controller';
import { BotSharedService } from '../../../../shared/bot-shared.service';

@Component({
  selector: 'tock-answer',
  templateUrl: './answer.component.html',
  styleUrls: ['./answer.component.scss']
})
export class AnswerComponent implements OnInit {
  @Input() answer: AnswerContainer;
  @Input() fullDisplay = false;
  @Input() editable = true;
  @Input() editableIconSize: 'tiny' | 'small' | 'medium' | 'large' | 'giant' = 'medium';
  @Input() create = false;
  @Input() answerLabel = 'answer';
  @Input() submit: AnswerController = new AnswerController();
  @Input() wide = false;

  answerType: string;

  constructor(public shared: BotSharedService, private nbDialogService: NbDialogService) {}

  ngOnInit(): void {
    this.answerType = this.answer.currentType.toString();
    if (!this.answer.currentAnswer()) {
      this.changeAnswerType(this.answer.currentType);
    }
  }

  editAnswer() {
    this.nbDialogService.open(AnswerDialogComponent, {
      context: {
        answer: this.answer,
        create: this.create,
        answerLabel: this.answerLabel
      }
    });
  }

  changeType(event) {
    this.changeAnswerType(parseInt(event));
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
        const s =
          'import ai.tock.bot.definition.story\n' +
          '\n' +
          'val s = story("' +
          this.answer.containerId() +
          '") { \n' +
          '           end("Hello World! :)")\n' +
          '}';
        const script = new ScriptAnswerVersionedConfiguration(s);
        this.answer.addNewAnswerType(new ScriptAnswerConfiguration([script], script));
      }
    }
  }
}
