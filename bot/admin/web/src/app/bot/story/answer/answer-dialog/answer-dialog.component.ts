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
import { NbDialogRef, NbToastrService } from '@nebular/theme';

import { AnswerConfiguration, AnswerConfigurationType, AnswerContainer } from '../../../model/story';
import { BotService } from '../../../bot-service';
import { AnswerController } from '../../controller';

@Component({
  selector: 'tock-answer-dialog',
  templateUrl: './answer-dialog.component.html',
  styleUrls: ['./answer-dialog.component.scss']
})
export class AnswerDialogComponent implements OnInit {
  @Input() create: boolean;
  @Input() answer: AnswerContainer;
  @Input() answerLabel = 'answer';

  submit: AnswerController = new AnswerController();

  private originalCurrentType: AnswerConfigurationType;
  private originalAnswers: AnswerConfiguration[];

  constructor(
    private botService: BotService,
    private toastrService: NbToastrService,
    private nbDialogRef: NbDialogRef<AnswerDialogComponent>
  ) {}

  ngOnInit() {
    const _this = this;
    this.originalCurrentType = this.answer.currentType;
    this.originalAnswers = this.answer.answers.slice(0).map((a) => a.clone());
    this.submit.submitListener = (_) => _this.save();
  }

  save() {
    this.submit.checkAnswer((_) => {
      const invalidMessage = this.answer.currentAnswer().invalidMessage();
      if (invalidMessage) {
        this.toastrService.danger(`Error: ${invalidMessage}`, 'Error', { duration: 5000 });
      } else if (!this.create) {
        this.answer.save(this.botService).subscribe((r) => {
          this.nbDialogRef.close({ answer: this.answer });
          this.toastrService.success(`${this.answerLabel} update successfully`, 'Update', { duration: 1000 });
        });
      } else {
        this.nbDialogRef.close({ answer: this.answer });
      }
    });
  }

  cancel() {
    this.answer.currentType = this.originalCurrentType;
    this.answer.answers = this.originalAnswers;
    this.answer.answers.forEach((a) => a.checkAfterReset(this.botService));
    this.nbDialogRef.close({});
  }
}
