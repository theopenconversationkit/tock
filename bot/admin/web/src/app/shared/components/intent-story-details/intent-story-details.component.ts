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
import { BotService } from '../../../bot/bot-service';
import { StateService } from '../../../core-nlp/state.service';
import { NbDialogRef } from '@nebular/theme';
import { StoryDefinitionConfiguration } from '../../../bot/model/story';
import { Intent, SearchQuery } from '../../../model/nlp';
import { PaginatedQuery } from '../../../model/commons';
import { NlpService } from '../../../core-nlp/nlp.service';
import { TestDialogService } from '../test-dialog/test-dialog.service';

@Component({
  selector: 'tock-intent-story-details',
  templateUrl: './intent-story-details.component.html',
  styleUrls: ['./intent-story-details.component.scss']
})
export class IntentStoryDetailsComponent implements OnInit {
  @Input() intentId?: string;
  @Input() intentName?: string;

  loading: boolean = true;
  intent: Intent;
  story: StoryDefinitionConfiguration;
  sentences: string[];
  sentencesReveal: boolean = false;

  constructor(
    private dialogRef: NbDialogRef<IntentStoryDetailsComponent>,
    private stateService: StateService,
    private botService: BotService,
    private nlpService: NlpService,
    private testDialogService: TestDialogService
  ) {}

  ngOnInit(): void {
    if (this.intentId) {
      this.intent = this.stateService.findIntentById(this.intentId);
    }

    if (this.intentName) {
      this.intent = this.stateService.findIntentByName(this.intentName);
    }

    if (this.intent) {
      this.botService.findStoryByBotIdAndIntent(this.stateService.currentApplication.name, this.intent.name).subscribe((s) => {
        this.story = s;

        this.searchIntentSentences();
        this.loading = false;
      });
    } else {
      this.loading = false;
    }
  }

  searchIntentSentences(): void {
    const cursor: number = 0;
    const pageSize: number = 1000;
    const mark = null;
    const paginatedQuery: PaginatedQuery = this.stateService.createPaginatedQuery(cursor, pageSize, mark);

    const searchQuery = new SearchQuery(
      paginatedQuery.namespace,
      paginatedQuery.applicationName,
      null,
      paginatedQuery.start,
      paginatedQuery.size,
      paginatedQuery.searchMark,
      null,
      this.intent._id
    );

    this.nlpService.searchSentences(searchQuery).subscribe((res: any) => {
      this.sentences = res.sentences.sort((a, b) => {
        return b.classification?.usageCount - a.classification?.usageCount;
      });
    });
  }

  testDialogSentence(message, locale) {
    this.testDialogService.testSentenceDialog({
      sentenceText: message,
      sentenceLocale: locale
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
