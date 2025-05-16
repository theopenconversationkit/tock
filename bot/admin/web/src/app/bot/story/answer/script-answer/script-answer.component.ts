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

import { AnswerContainer, ScriptAnswerConfiguration } from '../../../model/story';

@Component({
  selector: 'tock-script-answer',
  templateUrl: './script-answer.component.html',
  styleUrls: ['./script-answer.component.scss']
})
export class ScriptAnswerComponent implements OnInit {
  @Input() container: AnswerContainer;

  answer: ScriptAnswerConfiguration;

  ngOnInit(): void {
    this.answer = this.container.scriptAnswer();
  }
}
