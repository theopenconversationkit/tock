import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { debounceTime, Subject, takeUntil } from 'rxjs';

interface IntentsFilterForm {
  search: FormControl<string>;
}

export interface IntentsFilter {
  search: string;
}

@Component({
  selector: 'tock-intents-filters',
  templateUrl: './intents-filters.component.html',
  styleUrls: ['./intents-filters.component.scss']
})
export class IntentsFiltersComponent implements OnInit, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  @Output() onFilter = new EventEmitter<IntentsFilter>();

  form = new FormGroup<IntentsFilterForm>({
    search: new FormControl()
  });

  get search(): FormControl {
    return this.form.get('search') as FormControl;
  }

  ngOnInit(): void {
    this.form.valueChanges.pipe(debounceTime(300), takeUntil(this.destroy$)).subscribe(() => {
      this.onFilter.emit(this.form.value as IntentsFilter);
    });
  }

  clearSearch(): void {
    this.search.reset();
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
