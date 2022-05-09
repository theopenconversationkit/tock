import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { Subscription } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

import { Filter, Scenario } from '../../models';

@Component({
  selector: 'tock-scenario-filters',
  templateUrl: './scenario-filters.component.html',
  styleUrls: ['./scenario-filters.component.scss']
})
export class ScenarioFiltersComponent implements OnInit, OnChanges, OnDestroy {
  @Input()
  scenarios!: Scenario[];

  @Output()
  onFilter = new EventEmitter<Filter>();

  subscription = new Subscription();

  tagsAvailableValues: string[] = [];

  form = new FormGroup({
    search: new FormControl(''),
    tags: new FormControl([])
  });

  get search(): FormControl {
    return this.form.get('search') as FormControl;
  }

  get tags(): FormControl {
    return this.form.get('tags') as FormControl;
  }

  get isFiltered(): boolean {
    return this.search.value || this.tags.value?.length;
  }

  ngOnInit(): void {
    this.subscription = this.form.valueChanges.pipe(debounceTime(500)).subscribe(() => {
      console.log(this.form.value);
      this.onFilter.emit(this.form.value as Filter);
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    const scenarios = changes.scenarios.currentValue;

    if (scenarios && !this.tagsAvailableValues.length) {
      this.tagsAvailableValues = [
        ...new Set(
          <string>[].concat.apply(
            [],
            scenarios.map((v: Scenario) => v.tags)
          )
        )
      ];
    }
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  clearFilters(): void {
    this.search.reset('');
    this.tags.reset([]);
  }
}
