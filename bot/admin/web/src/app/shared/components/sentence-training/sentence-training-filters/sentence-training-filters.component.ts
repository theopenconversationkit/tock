import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { Subject } from 'rxjs';
import { debounceTime, takeUntil } from 'rxjs/operators';

import { SentenceTrainingFilter, SentenceTrainingMode } from './../models';

interface SentenceTrainingFilterForm {
  search: FormControl<string>;
  showUnknown: FormControl<boolean>;
}

@Component({
  selector: 'tock-sentence-training-filters',
  templateUrl: './sentence-training-filters.component.html',
  styleUrls: ['./sentence-training-filters.component.scss']
})
export class SentenceTrainingFiltersComponent implements OnInit, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  @Input() sentenceTrainingMode: SentenceTrainingMode;
  SentenceTrainingMode = SentenceTrainingMode;

  @Output() onFilter = new EventEmitter<SentenceTrainingFilter>();

  form = new FormGroup<SentenceTrainingFilterForm>({
    search: new FormControl(),
    showUnknown: new FormControl(false)
  });

  get search(): FormControl {
    return this.form.get('search') as FormControl;
  }

  get showUnknown(): FormControl {
    return this.form.get('showUnknown') as FormControl;
  }

  ngOnInit(): void {
    this.form.valueChanges.pipe(takeUntil(this.destroy$), debounceTime(500)).subscribe(() => {
      this.onFilter.emit(this.form.value as SentenceTrainingFilter);
    });
  }

  clearSearch(): void {
    this.search.reset();
  }

  updateFilter(filter: SentenceTrainingFilter): void {
    this.form.patchValue(filter);
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
