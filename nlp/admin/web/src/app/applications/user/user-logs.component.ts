/*
 * Copyright (C) 2017/2019 VSCT
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

import {AfterViewInit, Component, EventEmitter, Inject, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {StateService} from "../../core-nlp/state.service";
import {ApplicationService} from "../../core-nlp/applications.service";
import {MatPaginator} from "@angular/material";
import {DataSource} from "@angular/cdk/collections";
import {UserLog} from "../../model/application";
import {BehaviorSubject, merge, Observable, Subscription} from "rxjs";
import {PaginatedQuery} from "../../model/commons";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'tock-user-logs',
  templateUrl: './user-logs.component.html',
  styleUrls: ['./user-logs.component.css']
})
export class UserLogsComponent implements OnInit, AfterViewInit, OnDestroy {

  displayedColumns = ['date', 'type', 'user', 'application', 'data', 'error'];
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  dataSource: UserLogsDataSource | null;

  private subscription: Subscription;

  constructor(private state: StateService,
              private applicationService: ApplicationService,
              private dialog: MatDialog) {

  }

  ngOnInit(): void {
    this.dataSource = new UserLogsDataSource(this, this.state, this.applicationService);
    this.subscription = this.state.configurationChange.subscribe(_ => this.dataSource.refresh());
  }

  ngAfterViewInit(): void {
    this.dataSource.refresh();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  appName(appId: string): string {
    if (!appId) {
      return null;
    }
    const r = this.state.applications.find(a => a._id === appId);
    return r ? r.name : appId;
  }

  displayData(log: UserLog) {
    this.dialog.open(DisplayUserDataComponent, {
      data: {
        data: log.data()
      }
    });
  }

}

export class UserLogsDataSource extends DataSource<UserLog> {

  size: number = 0;
  private refreshEvent = new EventEmitter();
  private subject = new BehaviorSubject<UserLog[]>([]);

  constructor(private component: UserLogsComponent,
              private state: StateService,
              private  applicationService: ApplicationService) {
    super();
  }

  refresh() {
    this.refreshEvent.emit(true);
  }

  /** Connect function called by the table to retrieve one stream containing the data to render. */
  connect(): Observable<UserLog[]> {
    const displayDataChanges = [
      this.component.paginator.page,
      this.refreshEvent
    ];

    merge(...displayDataChanges).subscribe(() => {
      const startIndex = this.component.paginator.pageIndex * this.component.paginator.pageSize;

      this.applicationService.searchUserLogs(
        new PaginatedQuery(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          startIndex,
          this.component.paginator.pageSize
        )
      ).subscribe(r => {
        this.size = r.total;
        this.subject.next(r.logs);
      });
    });

    return this.subject;
  }

  disconnect() {
  }
}

@Component({
  selector: 'tock-display-full-log',
  template: `<h1 mat-dialog-title>User Action Data</h1>
  <div mat-dialog-content>
      Data:
      <pre>{{data.data}}</pre>
  </div>
  <div mat-dialog-actions>
      <button mat-raised-button mat-dialog-close color="primary">Close</button>
  </div>`
})
export class DisplayUserDataComponent {

  constructor(public dialogRef: MatDialogRef<DisplayUserDataComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {
  }
}
