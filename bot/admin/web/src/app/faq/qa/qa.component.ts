import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ReplaySubject } from 'rxjs';
import { StateService } from 'src/app/core-nlp/state.service';
import { Qa } from '../common/model/qa';
import { FaqQaFilter, QaGridComponent } from './qa-grid/qa-grid.component';

@Component({
  selector: 'tock-qa',
  templateUrl: './qa.component.html',
  styleUrls: ['./qa.component.scss']
})
export class QaComponent implements OnInit, OnDestroy {

  applicationName: string;

  public filter: FaqQaFilter;
  @ViewChild(QaGridComponent) grid;

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);

  constructor(
    private readonly state: StateService
  ) { }

  ngOnInit(): void {
    this.filter = {
      sort: [],
      search: null,
      clone: function () {
        return {...this};
      }
    };

    this.applicationName = this.state.currentApplication.name;
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  search(filter: Partial<FaqQaFilter>): void {

    this.filter.search = filter.search;
    this.filter.sort = filter.sort;

    this.grid.refresh();
  }

  details(qa: Qa) {
    console.log("qa", qa);
  }

}
