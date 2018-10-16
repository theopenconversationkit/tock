/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {AfterViewInit, Component, EventEmitter, OnInit, ViewChild} from "@angular/core";
import {StateService} from "../core/state.service";
import {ApplicationService} from "../core/applications.service";
import {MatPaginator} from "@angular/material";
import {DataSource} from "@angular/cdk/collections";
import {ModelBuild} from "../model/application";
import {BehaviorSubject} from "rxjs/BehaviorSubject";
import {Observable} from "rxjs/Observable";
import {PaginatedQuery} from "../model/commons";
import {merge} from "rxjs";

@Component({
  selector: 'tock-model-builds',
  templateUrl: './model-builds.component.html',
  styleUrls: ['./model-builds.component.css']
})
export class ModelBuildsComponent implements OnInit, AfterViewInit {

  displayedColumns = ['date', 'type', 'intent', 'count', 'duration', 'error'];
  @ViewChild(MatPaginator) paginator: MatPaginator;
  dataSource: ModelBuildDataSource | null;

  constructor(private state: StateService,
              private applicationService: ApplicationService) {

  }

  ngOnInit(): void {
    this.dataSource = new ModelBuildDataSource(this.paginator, this.state, this.applicationService);
  }

  ngAfterViewInit(): void {
    this.dataSource.refresh();
  }

  intentName(build: ModelBuild): string {
    if (build.intentId) {
      const i = this.state.findIntentById(build.intentId)
      return i ? i.name : "unknown";
    } else {
      return "";
    }
  }

}

export class ModelBuildDataSource extends DataSource<ModelBuild> {

  size: number = 0;
  private refreshEvent = new EventEmitter();
  private subject = new BehaviorSubject([]);

  constructor(private _paginator: MatPaginator,
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
      this._paginator.page,
      this.refreshEvent
    ];

    merge(...displayDataChanges).subscribe(() => {
      const startIndex = this._paginator.pageIndex * this._paginator.pageSize;

      this.applicationService.builds(
        new PaginatedQuery(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          startIndex,
          this._paginator.pageSize
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
