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

import { Component, ViewChild } from '@angular/core';
import { SentenceTrainingMode } from '../../../shared/components/sentence-training/models';
import { SentenceTrainingComponent } from '../../../shared/components';

@Component({
  selector: 'tock-sentences-search',
  templateUrl: './sentences-search.component.html',
  styleUrls: ['./sentences-search.component.scss']
})
export class SentencesSearchComponent {
  mode = SentenceTrainingMode.SEARCH;
  @ViewChild(SentenceTrainingComponent) sentencesTraining;

  refresh() {
    this.sentencesTraining.refresh();
  }

  downloadSentencesDump() {
    this.sentencesTraining.downloadSentencesDump();
  }

  constructor() {}
}
