import { Component, OnInit } from '@angular/core';
import { ModelBuild } from '../../model/application';
import { StateService } from '../../core-nlp/state.service';
import { ApplicationService } from '../../core-nlp/applications.service';
import moment from 'moment';
import { PaginatedQuery } from '../../model/commons';
import { Subject, takeUntil } from 'rxjs';
import { Pagination } from '../../shared/components';

@Component({
  selector: 'tock-model-builds',
  templateUrl: './model-builds.component.html',
  styleUrls: ['./model-builds.component.scss']
})
export class ModelBuildsComponent implements OnInit {
  destroy = new Subject();

  dataSource: ModelBuild[] = [];

  loading: boolean = false;

  pagination: Pagination = {
    start: 0,
    end: undefined,
    size: 10,
    total: undefined
  };

  constructor(private state: StateService, private applicationService: ApplicationService) {}

  ngOnInit(): void {
    this.state.configurationChange.pipe(takeUntil(this.destroy)).subscribe((_) => this.search());
    this.search();
  }

  duration(d: number): string {
    const duration = moment.duration(d, 's');
    return (
      this.formatDuration(duration.get('hours')) +
      ':' +
      this.formatDuration(duration.get('minutes')) +
      ':' +
      this.formatDuration(duration.get('seconds'))
    );
  }

  private formatDuration(d: number): string {
    return d <= 9 ? '0' + d : d.toString();
  }

  intentName(build: ModelBuild): string {
    if (build.intentId) {
      const i = this.state.findIntentById(build.intentId);
      return i ? i.intentLabel() : 'unknown';
    } else {
      return '';
    }
  }

  intentOrEntityName(build: ModelBuild): string {
    const i = this.intentName(build);
    if (i) {
      return i;
    } else {
      const e = this.state.findEntityTypeByName(build.entityTypeName);
      return e ? e.simpleName() : '';
    }
  }

  refresh(): void {
    this.search(this.pagination.start, this.pagination.size);
  }

  search(start: number = 0, size: number = this.pagination.size): void {
    this.loading = true;

    this.applicationService
      .builds(
        new PaginatedQuery(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          start,
          size
        )
      )
      .subscribe((result) => {
        this.pagination.total = result.total;

        this.pagination.end = Math.min(start + this.pagination.size, this.pagination.total);

        this.dataSource = result.data;
        this.pagination.start = start;

        this.loading = false;
      });
  }

  ngOnDestroy() {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
