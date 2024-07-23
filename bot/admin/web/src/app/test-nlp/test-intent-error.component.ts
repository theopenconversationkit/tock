/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import { saveAs } from 'file-saver-es';
import { Component, OnInit } from '@angular/core';
import { IntentTestError, TestErrorQuery } from '../model/nlp';
import { StateService } from '../core-nlp/state.service';
import { Router } from '@angular/router';
import { QualityService } from '../quality-nlp/quality.service';
import { escapeRegex } from '../model/commons';
import { DialogService } from '../core-nlp/dialog.service';
import { NbToastrService } from '@nebular/theme';
import { UserRole } from '../model/auth';
import { NlpService } from '../nlp-tabs/nlp.service';

@Component({
  selector: 'tock-test-intent-error',
  templateUrl: './test-intent-error.component.html',
  styleUrls: ['./test-intent-error.component.css']
})
export class TestIntentErrorComponent implements OnInit {
  dataSource: IntentTestError[] = [];
  intent: string = '';
  totalSize: number;
  pageSize: number = 10;
  pageIndex: number = 1;
  loading: boolean = false;

  constructor(
    public state: StateService,
    private qualityService: QualityService,
    private toastrService: NbToastrService,
    private router: Router,
    private dialog: DialogService,
    private nlp: NlpService
  ) {}

  ngOnInit(): void {
    this.search();
  }

  getIndex() {
    if (this.pageIndex > 0) return this.pageIndex - 1;
    else return this.pageIndex;
  }
  search() {
    this.loading = true;
    const startIndex = this.getIndex() * this.pageSize;
    this.qualityService
      .searchIntentErrors(TestErrorQuery.create(this.state, startIndex, this.pageSize, this.intent === '' ? undefined : this.intent))
      .subscribe((r) => {
        this.loading = false;
        this.totalSize = r.total;
        this.dataSource = r.data;
      });
  }

  validate(error: IntentTestError) {
    this.qualityService.deleteIntentError(error).subscribe((e) => {
      this.toastrService.show(`Sentence validated`, 'Validate Intent', { duration: 2000 });
      this.search();
    });
  }

  change(error: IntentTestError) {
    this.qualityService.deleteIntentError(error).subscribe((e) => {
      this.router.navigate(['/nlp/search'], {
        queryParams: {
          text: '^' + escapeRegex(error.sentence.text) + '$',
          status: 'model'
        }
      });
    });
  }

  download() {
    setTimeout((_) => {
      this.qualityService
        .searchIntentErrorsBlob(TestErrorQuery.create(this.state, 0, 100000, this.intent === '' ? undefined : this.intent))
        .subscribe((blob) => {
          saveAs(blob, this.state.currentApplication.name + '_intent_errors.json');
          this.dialog.notify(`Dump provided`, 'Dump');
        });
    }, 1);
  }

  canReveal(error: IntentTestError): boolean {
    return error.sentence.key && this.state.hasRole(UserRole.admin);
  }

  reveal(error: IntentTestError) {
    const sentence = error.sentence;
    this.nlp.revealSentence(sentence).subscribe((s) => {
      sentence.text = s.text;
      sentence.key = null;
      error.sentence = sentence.clone();
    });
  }
}
