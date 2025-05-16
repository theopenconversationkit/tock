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

import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ParseQuery, Sentence } from '../../../model/nlp';
import { Subject, takeUntil } from 'rxjs';
import { NlpService } from '../../../core-nlp/nlp.service';
import { StateService } from '../../../core-nlp/state.service';
import { NbToastrService } from '@nebular/theme';

@Component({
  selector: 'tock-sentence-new',
  templateUrl: './sentence-new.component.html',
  styleUrls: ['./sentence-new.component.scss']
})
export class SentenceNewComponent implements OnInit, OnDestroy {
  destroy = new Subject();
  sentence: Sentence;
  skipCache: boolean = false;
  queryState: string;

  @ViewChild('newSentenceInput') newSentenceInput: ElementRef;

  constructor(private nlp: NlpService, private state: StateService, private toastrService: NbToastrService) {}

  ngOnInit(): void {
    this.state.configurationChange.pipe(takeUntil(this.destroy)).subscribe((_) => this.onClose());
  }

  onTry(value: string): void {
    const app = this.state.currentApplication;
    const language = this.state.currentLocale;
    const v = value.trim();

    if (v.length == 0) {
      this.toastrService.show(`Please enter a non-empty query`, 'ERROR', { duration: 2000 });
    } else {
      this.sentence = null;
      this.nlp
        .parse(
          new ParseQuery(
            app.namespace,
            app.name,
            language,
            v,
            !this.skipCache,
            !this.queryState || this.queryState.trim().length === 0 ? null : this.queryState.trim()
          )
        )
        .subscribe((sentence) => {
          this.sentence = sentence;
        });
    }
  }

  onClose(): void {
    this.sentence = null;
    this.newSentenceInput.nativeElement.value = '';
  }

  ngOnDestroy() {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
