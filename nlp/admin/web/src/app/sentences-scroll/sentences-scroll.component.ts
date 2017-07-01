/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Component, Input, OnDestroy, OnInit} from "@angular/core";
import {SearchQuery, Sentence, SentenceStatus} from "../model/nlp";
import {NlpService} from "../nlp-tabs/nlp.service";
import {StateService} from "../core/state.service";

@Component({
  selector: 'tock-sentences-scroll',
  templateUrl: './sentences-scroll.component.html',
  styleUrls: ['./sentences-scroll.component.css']
})
export class SentencesScrollComponent implements OnInit, OnDestroy {

  @Input() filter: SentenceFilter;
  @Input() title: string;
  @Input() displayArchiveButton: boolean = true;
  @Input() displayProbabilities: boolean = false;

  sentences: Array<Sentence> = [];
  cursor: number = 0;
  pageSize: number = 10;
  total: number = -1;
  loading: boolean = false;

  private currentApplicationUnsuscriber: any;
  private currentLocaleUnsuscriber: any;

  constructor(private state: StateService, private nlp: NlpService) {
  }

  ngOnInit() {
    this.load();
    this.currentApplicationUnsuscriber = this.state.currentApplicationEmitter.subscribe(_ => this.refresh());
    this.currentLocaleUnsuscriber = this.state.currentLocaleEmitter.subscribe(_ => this.refresh());
  }

  ngOnDestroy() {
    this.currentApplicationUnsuscriber.unsubscribe();
    this.currentLocaleUnsuscriber.unsubscribe();
  }

  refresh() {
    this.loading = false;
    this.cursor = 0;
    this.total = -1;
    this.sentences = [];
    this.load();
  }

  load() {
    if (!this.loading && (this.total === -1 || this.total > this.cursor)) {
      this.loading = true;
      const app = this.state.currentApplication;
      const language = this.state.currentLocale;
      this.nlp.searchSentences(new SearchQuery(
        app.namespace,
        app.name,
        language,
        this.cursor,
        this.cursor + this.pageSize,
        this.filter.search,
        this.filter.intentId,
        this.filter.status))
        .subscribe(s => {
          Array.prototype.push.apply(this.sentences, s.sentences);
          this.cursor = s.end;
          this.total = s.total;
          this.loading = false;
        });
    }
  }

  onScroll() {
    this.load();
  }

  onClose(sentence: Sentence) {
    let s = this.sentences.filter(s => s.text === sentence.text)[0];
    this.sentences.splice(this.sentences.indexOf(s), 1);
    this.total -= 1;
    this.cursor -= 1;
  }

}

export class SentenceFilter {
  constructor(public search?: string,
              public intentId?: string,
              public status?: SentenceStatus[]) {
  }

  clone(): SentenceFilter {
    return new SentenceFilter(this.search, this.intentId, this.status);
  }
}
