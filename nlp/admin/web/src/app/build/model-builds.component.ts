/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
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

import * as moment from 'moment';
import {AfterViewInit, Component, EventEmitter, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {StateService} from "../core-nlp/state.service";
import {ApplicationService} from "../core-nlp/applications.service";
import {MatPaginator} from "@angular/material/paginator";
import {DataSource} from "@angular/cdk/collections";
import {ModelBuild} from "../model/application";
import {BehaviorSubject, merge, Observable, Subscription} from "rxjs";
import {PaginatedQuery} from "../model/commons";

@Component({
  selector: 'tock-model-builds',
  templateUrl: './model-builds.component.html',
  styleUrls: ['./model-builds.component.css']
})
export class ModelBuildsComponent implements OnInit, AfterViewInit, OnDestroy {

  displayedColumns = ['date', 'type', 'intent', 'count', 'duration', 'error'];
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  dataSource: ModelBuildDataSource | null;

  private subscription: Subscription;

  constructor(private state: StateService,
              private applicationService: ApplicationService) {

  }

  ngOnInit(): void {
    this.dataSource = new ModelBuildDataSource(this, this.state, this.applicationService);
    this.subscription = this.state.configurationChange.subscribe(_ => this.dataSource.refresh());
  }

  ngAfterViewInit(): void {
    this.dataSource.refresh();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  duration(d: number): string {
    const duration = moment.duration(d, 's');
    return this.formatDuration(duration.get('hours')) + ":"
      + this.formatDuration(duration.get('minutes')) + ":"
      + this.formatDuration(duration.get('seconds'));
  }

  private formatDuration(d: number): string {
    return d <= 9 ? "0" + d : d.toString()
  }

  intentName(build: ModelBuild): string {
    if (build.intentId) {
      const i = this.state.findIntentById(build.intentId);
      return i ? i.intentLabel() : "unknown";
    } else {
      return "";
    }
  }

  intentOrEntityName(build: ModelBuild): string {
    const i = this.intentName(build);
    if (i) {
      return i;
    } else {
      const e = this.state.findEntityTypeByName(build.entityTypeName);
      return e ? e.simpleName() : "";
    }
  }
}

export class ModelBuildDataSource extends DataSource<ModelBuild> {

  size: number = 0;
  private refreshEvent = new EventEmitter();
  private subject = new BehaviorSubject<ModelBuild[]>([]);

  constructor(private component: ModelBuildsComponent,
              private state: StateService,
              private  applicationService: ApplicationService) {
    super();
  }

  refresh() {
    this.refreshEvent.emit(true);
  }

  /** Connect function called by the table to retrieve one stream containing the data to render. */
  connect(): Observable<ModelBuild[]> {
    const displayDataChanges = [
      this.component.paginator.page,
      this.refreshEvent
    ];

    merge(...displayDataChanges).subscribe(() => {
      const startIndex = this.component.paginator.pageIndex * this.component.paginator.pageSize;

      this.applicationService.builds(
        new PaginatedQuery(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          startIndex,
          this.component.paginator.pageSize
        )
      ).subscribe(r => {
        this.size = r.total;
        this.subject.next(r.data);
      });
    });

    return this.subject;
  }

  disconnect() {
  }
}
