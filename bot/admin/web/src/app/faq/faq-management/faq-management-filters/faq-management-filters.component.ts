import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormArray, FormControl, FormGroup } from '@angular/forms';
import { Subscription } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

import { FaqFilter } from '../../models';

interface FaqFilterForm {
  search: FormControl<string>;
  tags: FormControl<string[]>;
  enabled: FormControl<boolean | null>;
}

@Component({
  selector: 'tock-faq-management-filters',
  templateUrl: './faq-management-filters.component.html',
  styleUrls: ['./faq-management-filters.component.scss']
})
export class FaqManagementFiltersComponent implements OnInit, OnDestroy {
  @Input() tagsCache: string[];

  @Output() onFilter = new EventEmitter<FaqFilter>();

  subscription = new Subscription();

  form = new FormGroup<FaqFilterForm>({
    search: new FormControl(),
    tags: new FormControl([]),
    enabled: new FormControl(null)
  });

  get search(): FormControl {
    return this.form.get('search') as FormControl;
  }

  get tags(): FormControl {
    return this.form.get('tags') as FormControl;
  }

  get enabled(): FormControl {
    return this.form.get('enabled') as FormControl;
  }

  get isFiltered(): boolean {
    return this.search.value || this.tags.value?.length || this.enabled.value !== null;
  }

  ngOnInit(): void {
    this.subscription = this.form.valueChanges.pipe(debounceTime(500)).subscribe(() => {
      this.onFilter.emit(this.form.value as FaqFilter);
    });
  }

  enabledCheckChanged() {
    switch (this.enabled.value) {
      case null:
        this.enabled.setValue(true);
        break;
      case true:
        this.enabled.setValue(false);
        break;
      case false:
        this.enabled.setValue(null);
        break;
    }
  }

  clearFilters(): void {
    this.search.reset();
    this.tags.reset([]);
    this.enabled.reset(null);
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
}
