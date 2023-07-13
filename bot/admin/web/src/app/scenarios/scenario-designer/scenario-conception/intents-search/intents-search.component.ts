import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { StateService } from '../../../../core-nlp/state.service';
import { Intent, SearchQuery, Sentence, SentencesResult } from '../../../../model/nlp';
import { NlpService } from '../../../../nlp-tabs/nlp.service';
import { ScenarioService } from '../../../services';

type SentencesGroupedByIntent = { intent: Intent; sentences: Sentence[] };

@Component({
  selector: 'tock-scenario-intents-search',
  templateUrl: './intents-search.component.html',
  styleUrls: ['./intents-search.component.scss']
})
export class IntentsSearchComponent implements OnInit, OnDestroy {
  @Input() intentSentence: string;

  @Output() createNewIntentEvent = new EventEmitter();
  @Output() useIntentEvent = new EventEmitter();

  destroy = new Subject();

  constructor(
    private dialogRef: NbDialogRef<IntentsSearchComponent>,
    private stateService: StateService,
    private nlpService: NlpService,
    private scenarioService: ScenarioService
  ) {}

  loading: boolean = true;
  title: string = 'Searching existing intents';
  deduplicatedIntents: SentencesGroupedByIntent[];

  ngOnInit(): void {
    this.searchIntents()
      .pipe(takeUntil(this.destroy))
      .subscribe((sentencesResearch) => {
        this.loading = false;
        this.title = `Existing intents`;
        this.deduplicatedIntents = this.extractIntents(sentencesResearch.rows);
        if (!this.deduplicatedIntents.length) {
          this.createNewIntent();
        }
      });
  }

  useIntent(deduplicatedIntent: SentencesGroupedByIntent): void {
    this.useIntentEvent.emit(deduplicatedIntent.intent);
  }

  private getObjectValueByPath(obj: {}, key: string): any {
    if (key.indexOf('.') > -1) {
      const nameSpaces = key.split('.');
      return nameSpaces.reduce((accumulator, value) => {
        if (accumulator) return accumulator[value];
      }, obj);
    } else {
      return obj[key];
    }
  }

  private groupBy(arr: Sentence[], key: string): {} {
    return arr.reduce((accumulator, sentence) => {
      const val = this.getObjectValueByPath(sentence, key);
      (accumulator[val] = accumulator[val] || []).push(sentence);
      return accumulator;
    }, {});
  }

  private extractIntents(foundSentences: Sentence[]): SentencesGroupedByIntent[] {
    let intents = this.groupBy(foundSentences, 'classification.intentId');

    // we delete unknown intents if any
    delete intents[Intent.unknown];

    let intentsIds = Object.keys(intents);
    return intentsIds.map((intentId) => {
      let intent = this.stateService.findSharedNamespaceIntentById(intentId);
      let sentences = intents[intentId];
      return {
        intent: intent,
        sentences: sentences.map((s) => s.text)
      };
    });
  }

  private searchIntents(): Observable<SentencesResult> {
    const searchQuery: SearchQuery = this.scenarioService.createSearchIntentsQuery({
      searchString: this.intentSentence
    });

    return this.nlpService.searchSentences(searchQuery);
  }

  doesIntentBelongToCurrentNameSpace(intent: Intent): boolean {
    return intent.namespace === this.stateService.currentApplication.namespace;
  }

  cancel(): void {
    this.dialogRef.close();
  }

  createNewIntent(): void {
    this.createNewIntentEvent.emit();
  }

  ngOnDestroy() {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
