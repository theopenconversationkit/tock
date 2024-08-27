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

import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { saveAs } from 'file-saver-es';
import { Observable, Subject, debounceTime, takeUntil } from 'rxjs';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { DisplayIntentFullLogComponent } from './display-intents-full-log/display-intents-full-log.component';
import { StateService } from '../../core-nlp/state.service';
import { NlpService } from '../../core-nlp/nlp.service';
import { CoreConfig } from '../../core-nlp/core.config';
import { Log, LogsQuery, PaginatedResult, Sentence } from '../../model/nlp';
import { PaginatedQuery, SearchMark } from '../../model/commons';
import { copyToClipboard } from '../../shared/utils';
import { Pagination } from '../../shared/components';
import { DOCUMENT } from '@angular/common';
import { Router } from '@angular/router';

interface IntentsLogsFilterForm {
  searchString: FormControl<string>;
  onlyCurrentLocale: FormControl<boolean>;
  displayTests: FormControl<boolean>;
}

@Component({
  selector: 'tock-intents-logs',
  templateUrl: './intents-logs.component.html',
  styleUrls: ['./intents-logs.component.scss']
})
export class IntentsLogsComponent implements OnInit, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  loading: boolean = false;

  pagination: Pagination = {
    start: 0,
    end: undefined,
    size: 10,
    total: undefined
  };

  logs: Log[];

  dialogDetailsSentence: Sentence;

  constructor(
    public state: StateService,
    private nlp: NlpService,
    private dialogService: NbDialogService,
    public config: CoreConfig,
    private toastrService: NbToastrService,
    private router: Router,
    @Inject(DOCUMENT) private document: Document
  ) {}

  ngOnInit(): void {
    this.state.configurationChange.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.closeDetails();
      this.loadData();
    });

    this.form.valueChanges.pipe(debounceTime(500)).subscribe(() => {
      this.closeDetails();
      this.loadData();
    });

    this.loadData();
  }

  paginationChange(): void {
    this.loadData(this.pagination.start, this.pagination.size, false, true, false, true);
  }

  onScroll(): void {
    if (this.loading || this.pagination.end >= this.pagination.total) return;

    this.loadData(this.pagination.end, this.pagination.size, true, false);
  }

  loadData(
    start: number = 0,
    size: number = this.pagination.size,
    add: boolean = false,
    showLoadingSpinner: boolean = true,
    partialReload: boolean = false,
    scrollToTop: Boolean = false
  ): void {
    if (showLoadingSpinner) this.loading = true;

    this.search(this.state.createPaginatedQuery(start, size))
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: PaginatedResult<Log>) => {
          this.pagination.total = data.total;
          if (!partialReload || this.pagination.end > this.pagination.total) {
            this.pagination.end = data.end;
          }

          if (add) {
            this.logs = [...this.logs, ...data.rows];
          } else {
            this.logs = data.rows;
            this.pagination.start = data.start;
          }

          if (scrollToTop) {
            this.scrollToTop();
          }

          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  search(query: PaginatedQuery): Observable<PaginatedResult<Log>> {
    return this.nlp.searchLogs(
      new LogsQuery(
        query.namespace,
        query.applicationName,
        this.onlyCurrentLocale.value ? query.language : null,
        query.start,
        query.size,
        query.searchMark,
        this.searchString.value,
        this.displayTests.value
      )
    );
  }

  resetSearch(): void {
    this.searchString.reset();
  }

  scrollToTop(): void {
    const currentScroll = this.document.documentElement.scrollTop || this.document.body.scrollTop;
    if (currentScroll > 0) {
      window.requestAnimationFrame(this.scrollToTop.bind(this));
      window.scrollTo(0, currentScroll - currentScroll / 4);
    }
  }

  form = new FormGroup<IntentsLogsFilterForm>({
    searchString: new FormControl(),
    onlyCurrentLocale: new FormControl(false),
    displayTests: new FormControl(false)
  });

  get searchString(): FormControl {
    return this.form.get('searchString') as FormControl;
  }

  get onlyCurrentLocale(): FormControl {
    return this.form.get('onlyCurrentLocale') as FormControl;
  }

  get displayTests(): FormControl {
    return this.form.get('displayTests') as FormControl;
  }

  isRootSentence(sentence: Sentence): boolean {
    return sentence instanceof Sentence;
  }

  redirectToFaqManagement(sentence: Sentence): void {
    this.router.navigate(['faq/management'], { state: { question: sentence.text } });
  }

  displayFullLog(log: Log): void {
    this.dialogService.open(DisplayIntentFullLogComponent, {
      context: {
        request: JSON.parse(log.requestDetails()),
        response: JSON.parse(log.responseDetails())
      }
    });
  }

  copySentence(sentence): void {
    copyToClipboard(sentence.getText());
    this.toastrService.success(`Sentence copied to clipboard`, 'Clipboard');
  }

  showDetails(sentence: Sentence): void {
    if (this.dialogDetailsSentence && this.dialogDetailsSentence == sentence) {
      this.dialogDetailsSentence = undefined;
    } else {
      this.dialogDetailsSentence = sentence;
    }
  }

  closeDetails(): void {
    this.dialogDetailsSentence = undefined;
  }

  downloadDump(): void {
    setTimeout((_) => {
      this.nlp.exportLogs(this.state.currentApplication, this.state.currentLocale).subscribe((blob) => {
        saveAs(blob, this.state.currentApplication.name + '_' + this.state.currentLocale + '_logs.csv');
        this.toastrService.show(`Export provided`, 'Dump', { duration: 2000 });
      });
    }, 1);
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
