/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

import {Injectable} from '@angular/core';
import {Sentence, SentenceStatus, UpdateSentencesQuery} from "../../model/nlp";
import {DialogService} from "../../core-nlp/dialog.service";
import {NlpService} from "../../nlp-tabs/nlp.service";
import {StateService} from "../../core-nlp/state.service";
import {EMPTY, empty, merge, Observable, of} from 'rxjs';
import {concatMap, flatMap, take, takeUntil} from 'rxjs/operators';

@Injectable()
export class SentencesService {

  constructor(
    private readonly state: StateService,
    private readonly nlp: NlpService,
    private readonly dialog: DialogService
  ) {
  }

  public save(sentence: Sentence, cancel$: Observable<any> = empty()): Observable<any> {
    return this.nlp.updateSentence(sentence)
      .pipe(
        takeUntil(cancel$),
        flatMap(_ => {
          // delete old language
          if (sentence.language !== this.state.currentLocale) {
            const s = sentence.clone();
            s.language = this.state.currentLocale;
            s.status = SentenceStatus.deleted;
            this.dialog.notify(`Language change to ${this.state.localeName(sentence.language)}`
              , 'Language change');

            return this.nlp.updateSentence(s);
          } else {
            return empty();
          }
        })
      );
  }

  public saveBulk(sentences: Sentence[], cancel$: Observable<any> = empty()): Observable<any> {
    if (sentences.length === 0) {
      return empty();
    }

    if (sentences.some(s => s.language !== this.state.currentLocale)) {
      throw new Error("Unsupported operation");
    }

    // because we could not use current backend API for doing this
    return sentences.reduce((acc, value) => {
        return merge(acc, this.nlp.updateSentence(value))
          .pipe(take(1), takeUntil(cancel$)); // on-the-fly Http Request cancellation
    }, empty());
  }
}
