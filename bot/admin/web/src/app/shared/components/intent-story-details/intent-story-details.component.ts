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
      console.log(this.sentences);
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
