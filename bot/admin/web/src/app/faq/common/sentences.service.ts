import {Injectable} from '@angular/core';
import {Sentence, SentenceStatus} from "../../model/nlp";
import {DialogService} from "../../core-nlp/dialog.service";
import {NlpService} from "../../nlp-tabs/nlp.service";
import {StateService} from "../../core-nlp/state.service";
import {empty, Observable} from 'rxjs';
import {flatMap, takeUntil} from 'rxjs/operators';

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
}
