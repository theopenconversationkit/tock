import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { Subscription } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

import { Filter, Scenario } from '../../models';
import { ScenarioService } from '../../services/scenario.service';

@Component({
  selector: 'tock-scenario-filters',
  templateUrl: './scenario-filters.component.html',
  styleUrls: ['./scenario-filters.component.scss']
})
export class ScenarioFiltersComponent implements OnInit, OnDestroy {
  @Output() onFilter = new EventEmitter<Filter>();

  subscription = new Subscription();
  tagsCache: string[] = [];

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
    return this.search.value || this.tags.value.length;
  }

  constructor(private scenarioService: ScenarioService) {}

  ngOnInit(): void {
    this.subscription.add(
      this.form.valueChanges.pipe(debounceTime(500)).subscribe(() => {
        this.onFilter.emit(this.form.value as Filter);
      })
    );

    this.subscription.add(
      this.scenarioService.state$.subscribe((state) => {
        this.tagsCache = state.tags;
      })
    );
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  clearFilters(): void {
    this.search.reset('');
    this.tags.reset([]);
  }
}
