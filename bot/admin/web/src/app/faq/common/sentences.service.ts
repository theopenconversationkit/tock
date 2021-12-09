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
