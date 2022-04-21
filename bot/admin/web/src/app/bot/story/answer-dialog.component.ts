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

import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { StateService } from '../../core-nlp/state.service';
import { AnswerConfiguration, AnswerConfigurationType, AnswerContainer } from '../model/story';
import { BotService } from '../bot-service';
import { AnswerController } from './controller';
import { NbToastrService } from '@nebular/theme';

@Component({
  selector: 'tock-answer-dialog',
  templateUrl: './answer-dialog.component.html',
  styleUrls: ['./answer-dialog.component.css']
})
export class AnswerDialogComponent implements OnInit {
  create: boolean;
  answer: AnswerContainer;
  answerLabel = 'Answer';

  submit: AnswerController = new AnswerController();

  private originalCurrentType: AnswerConfigurationType;
  private originalAnswers: AnswerConfiguration[];

  constructor(
    public dialogRef: MatDialogRef<AnswerDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private state: StateService,
    private bot: BotService,
    private toastrService: NbToastrService
  ) {
    this.create = this.data.create;
    this.answer = this.data.answer;
    this.answerLabel = this.data.answerLabel ? this.data.answerLabel : 'Answer';
    this.originalCurrentType = this.answer.currentType;
    this.originalAnswers = this.answer.answers.slice(0).map((a) => a.clone());
  }

  ngOnInit() {
    const _this = this;
    this.submit.submitListener = (_) => _this.save();
  }

  save() {
    this.submit.checkAnswer((_) => {
      const invalidMessage = this.answer.currentAnswer().invalidMessage();
      if (invalidMessage) {
        this.toastrService.show(`Error: ${invalidMessage}`, 'ERROR', {
          duration: 5000,
          status: 'danger'
        });
      } else if (!this.create) {
        this.answer.save(this.bot).subscribe((r) => {
          this.dialogRef.close({
            answer: this.answer
          });
          this.toastrService.show(`${this.answerLabel} Modified`, 'UPDATE', { duration: 1000 });
        });
      } else {
        this.dialogRef.close({
          answer: this.answer
        });
      }
    });
  }

  cancel() {
    this.answer.currentType = this.originalCurrentType;
    this.answer.answers = this.originalAnswers;
    this.answer.answers.forEach((a) => a.checkAfterReset(this.bot));
    this.dialogRef.close({});
  }
}
