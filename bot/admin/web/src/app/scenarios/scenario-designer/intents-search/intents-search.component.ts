import { Component, EventEmitter, Inject, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { StateService } from 'src/app/core-nlp/state.service';
import { PaginatedQuery } from 'src/app/model/commons';
import { SearchQuery, SentencesResult } from 'src/app/model/nlp';
import { NlpService } from 'src/app/nlp-tabs/nlp.service';

@Component({
  selector: 'scenario-intents-search',
  templateUrl: './intents-search.component.html',
  styleUrls: ['./intents-search.component.scss'],
  providers: [NlpService]
})
export class IntentsSearchComponent implements OnInit, OnDestroy {
  destroy = new Subject();
  @Input() intentSentence: string;
  @Output() createNewIntentEvent = new EventEmitter();
  @Output() useIntentEvent = new EventEmitter();

  constructor(
    public dialogRef: NbDialogRef<IntentsSearchComponent>,
    protected state: StateService,
    private nlp: NlpService
  ) {}

  loading: boolean = true;
  title: string = 'Searching existing intents';
  groupedIntents;

  ngOnInit(): void {
    this.searchIntents()
      .pipe(takeUntil(this.destroy))
      .subscribe((sentencesResearch) => {
        this.loading = false;
        this.title = `Existing intents`;
        this.groupedIntents = this.extractIntents(sentencesResearch.rows);
        if (!this.groupedIntents.length) {
          this.createNewIntent();
        }
      });
  }

  useIntent(intent) {
    this.useIntentEvent.emit(intent.intent);
  }

  getObjectValueByPath(obj, key) {
    if (key.indexOf('.') > -1) {
      let nameSpaces = key.split('.');
      return nameSpaces.reduce((accumulator, value) => {
        if (accumulator) return accumulator[value];
      }, obj);
    } else {
      return obj[key];
    }
  }

  groupBy(arr, key) {
    return arr.reduce((entry, x) => {
      let val = this.getObjectValueByPath(x, key);
      (entry[val] = entry[val] || []).push(x);
      return entry;
    }, {});
  }

  extractIntents(foundSentences) {
    let intents = this.groupBy(foundSentences, 'classification.intentId');
    let intentsIds = Object.keys(intents);
    return intentsIds.map((intentId) => {
      let intent = this.state.findIntentById(intentId);
      let sentences = intents[intentId];
      return {
        intent: intent,
        sentences: sentences.map((s) => s.text)
      };
    });
  }

  searchIntents() {
    const cursor: number = 0;
    const pageSize: number = 10;
    const mark = null;
    const paginatedQuery: PaginatedQuery = this.state.createPaginatedQuery(cursor, pageSize, mark);
    const searchQuery: SearchQuery = new SearchQuery(
      paginatedQuery.namespace,
      paginatedQuery.applicationName,
      paginatedQuery.language,
      paginatedQuery.start,
      paginatedQuery.size,
      paginatedQuery.searchMark,
      this.intentSentence
    );
    return this.nlp.searchSentences(searchQuery);
  }

  cancel(): void {
    this.dialogRef.close();
  }

  createNewIntent(): void {
    this.createNewIntentEvent.emit();
  }

  ngOnDestroy() {
    this.destroy.next();
    this.destroy.complete();
  }
}
