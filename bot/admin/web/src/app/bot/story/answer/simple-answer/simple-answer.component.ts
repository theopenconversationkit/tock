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

import { Component, Input, OnInit } from '@angular/core';

import { AnswerContainer, Media, SimpleAnswer, SimpleAnswerConfiguration } from '../../../model/story';
import { BotService } from '../../../bot-service';
import { StateService } from '../../../../core-nlp/state.service';
import { CreateI18nLabelRequest } from '../../../model/i18n';
import { MediaDialogComponent } from './../../media/media-dialog.component';
import { AnswerController } from './../../controller';
import { DialogService } from '../../../../core-nlp/dialog.service';

@Component({
  selector: 'tock-simple-answer',
  templateUrl: './simple-answer.component.html',
  styleUrls: ['./simple-answer.component.scss']
})
export class SimpleAnswerComponent implements OnInit {
  @Input() container: AnswerContainer;
  @Input() answerLabel: string = 'answer';
  @Input() submit: AnswerController = new AnswerController();

  answer: SimpleAnswerConfiguration;
  fullDisplay: boolean;
  newAnswer: string;
  newMedia: Media;

  constructor(public state: StateService, private bot: BotService, private dialog: DialogService) {}

  ngOnInit(): void {
    const _this = this;
    this.answer = this.container.simpleAnswer();
    this.answer.allowNoAnswer = this.container.allowNoAnwser();
    this.submit.answerSubmitListener = (callback) => _this.addAnswerIfNonEmpty(callback);
  }

  toggleDisplay() {
    this.fullDisplay = !this.fullDisplay;
  }

  updateLabel(answer: SimpleAnswer) {
    // back returns an I18nLabel whose “namespace” attribute may have been turned to lowercase; we need to fix this.
    // TODO : Fix back behavior
    answer.label.namespace = this.state.currentApplication.namespace;

    this.bot.saveI18nLabel(answer.label).subscribe((_) =>
      this.dialog.notify(`Story label has been updated successfully.`, 'Label Updated', {
        duration: 3000,
        status: 'success'
      })
    );
  }

  private addAnswerIfNonEmpty(callback) {
    if (this && this.newAnswer && this.newAnswer.trim().length !== 0) {
      this.addAnswer(callback);
    } else {
      callback();
    }
  }

  addAnswer(callback?: Function) {
    if (!this.newAnswer || this.newAnswer.trim().length === 0) {
      this.newAnswer = '';
      if (!this.container.allowNoAnwser() && this.answer.answers.length === 0) {
        this.dialog.notify('Please specify an answer');
      } else {
        this.submit.submitAnswer();
      }
    } else {
      this.bot
        .createI18nLabel(new CreateI18nLabelRequest(this.container.category, this.newAnswer.trim(), this.state.currentLocale))
        .subscribe((i18n) => {
          this.answer.answers.push(new SimpleAnswer(i18n, -1, this.newMedia));
          this.newAnswer = '';
          this.newMedia = null;
          if (callback) {
            callback();
          }
        });
    }
  }

  downward(answer: SimpleAnswer) {
    const answers = this.answer.answers;
    const i = answers.indexOf(answer);
    answers[i] = answers[i + 1];
    answers[i + 1] = answer;
    this.answer.answers = answers.slice();
  }

  canDownward(answer: SimpleAnswer): boolean {
    const answers = this.answer.answers;
    return answers.length > 1 && answers[answers.length - 1] !== answer;
  }

  upward(answer: SimpleAnswer) {
    const answers = this.answer.answers;
    const i = answers.indexOf(answer);
    answers[i] = answers[i - 1];
    answers[i - 1] = answer;
    this.answer.answers = answers.slice();
  }

  canUpward(answer: SimpleAnswer): boolean {
    const answers = this.answer.answers;
    return answers.length > 1 && answers[0] !== answer;
  }

  deleteAnswer(answer: SimpleAnswer, notify: boolean = false) {
    this.answer.answers.splice(this.answer.answers.indexOf(answer), 1);
    this.bot.deleteI18nLabel(answer.label).subscribe((c) => {
      if (notify) this.dialog.notify(`Label deleted`, 'DELETE');
    });
  }

  displayMediaMessage(answer?: SimpleAnswer) {
    const media = answer ? answer.mediaMessage : this.newMedia;
    let dialogRef = this.dialog.openDialog(MediaDialogComponent, {
      context: {
        // @ts-ignore
        media: media,
        category: this.container.category
      }
    });

    dialogRef.onClose.subscribe((result) => {
      if (result) {
        const removeMedia = result.removeMedia;
        const media = result.media;
        if (removeMedia || media) {
          if (removeMedia) {
            if (answer) answer.mediaMessage = null;
            else this.newMedia = null;
          } else {
            if (answer) answer.mediaMessage = media;
            else this.newMedia = media;
          }
        }
      }
    });
  }
}
