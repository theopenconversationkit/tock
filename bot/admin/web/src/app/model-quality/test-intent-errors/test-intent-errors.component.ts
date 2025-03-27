import { Component, OnDestroy, OnInit } from '@angular/core';
import { IntentTestError, TestErrorQuery } from '../../model/nlp';
import { StateService } from '../../core-nlp/state.service';
import { QualityService } from '../quality.service';
import { NbToastrService } from '@nebular/theme';
import { Router } from '@angular/router';
import { DialogService } from '../../core-nlp/dialog.service';
import { NlpService } from '../../core-nlp/nlp.service';
import { escapeRegex } from '../../model/commons';
import { saveAs } from 'file-saver-es';
import { UserRole } from '../../model/auth';
import { Subject, takeUntil } from 'rxjs';
import { Pagination } from '../../shared/components';
import { getExportFileName } from '../../shared/utils';

@Component({
  selector: 'tock-test-intent-errors',
  templateUrl: './test-intent-errors.component.html',
  styleUrls: ['./test-intent-errors.component.scss']
})
export class TestIntentErrorsComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  dataSource: IntentTestError[] = [];

  intent: string = '';

  loading: boolean = false;

  pagination: Pagination = {
    start: 0,
    end: undefined,
    size: 10,
    total: undefined
  };

  constructor(
    public state: StateService,
    private qualityService: QualityService,
    private toastrService: NbToastrService,
    private router: Router,
    private dialog: DialogService,
    private nlp: NlpService
  ) {}

  ngOnInit(): void {
    this.state.configurationChange.pipe(takeUntil(this.destroy)).subscribe((_) => this.search());
    this.search();
  }

  refresh(): void {
    this.search(this.pagination.start, this.pagination.size);
  }

  search(start: number = 0, size: number = this.pagination.size): void {
    this.loading = true;

    this.qualityService
      .searchIntentErrors(TestErrorQuery.create(this.state, start, size, this.intent === '' ? undefined : this.intent))
      .subscribe((result) => {
        this.pagination.total = result.total;

        this.pagination.end = Math.min(start + this.pagination.size, this.pagination.total); //result.end;

        this.dataSource = result.data;
        this.pagination.start = start; //result.start;

        this.loading = false;
      });
  }

  validate(error: IntentTestError) {
    this.qualityService.deleteIntentError(error).subscribe((e) => {
      this.toastrService.show(`Sentence validated`, 'Validate Intent', { duration: 2000 });
      this.refresh();
    });
  }

  change(error: IntentTestError) {
    this.qualityService.deleteIntentError(error).subscribe((e) => {
      this.router.navigate(['/language-understanding/search'], { state: { searchSentence: '^' + escapeRegex(error.sentence.text) + '$' } });
    });
  }

  download() {
    setTimeout((_) => {
      this.qualityService
        .searchIntentErrorsBlob(TestErrorQuery.create(this.state, 0, 100000, this.intent === '' ? undefined : this.intent))
        .subscribe((blob) => {
          const exportFileName = getExportFileName(
            this.state.currentApplication.namespace,
            this.state.currentApplication.name,
            'Intent-errors',
            'json'
          );
          saveAs(blob, exportFileName);
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

  ngOnDestroy() {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
