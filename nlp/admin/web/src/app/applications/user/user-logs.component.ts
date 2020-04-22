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

import {AfterViewInit, Component, EventEmitter, Input, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {StateService} from "../../core-nlp/state.service";
import {ApplicationService} from "../../core-nlp/applications.service";
import {MatPaginator} from "@angular/material";
import {DataSource} from "@angular/cdk/collections";
import {UserLog} from "../../model/application";
import {BehaviorSubject, merge, Observable, Subscription} from "rxjs";
import {PaginatedQuery} from "../../model/commons";
import {JsonEditorComponent, JsonEditorOptions} from 'ang-jsoneditor';
import {NbDialogRef, NbDialogService} from '@nebular/theme';

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
              private dialogService: NbDialogService) {

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
    this.dialogService.open(DisplayUserDataComponent, {
      context: {
        data: JSON.parse(log.data())
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
  template: `
    <nb-card status="primary">
      <nb-card-header>User Action Data</nb-card-header>
      <nb-card-body class="no-padding">
        <json-editor [options]="editorOptions" [data]="data"></json-editor>
      </nb-card-body>
      <nb-card-footer class="btn-align">
        <button nbButton status="primary" (click)="close()">Close</button>
      </nb-card-footer>
    </nb-card>`,
  styles: [`:host ::ng-deep json-editor,
            :host ::ng-deep json-editor .jsoneditor,
            :host ::ng-deep json-editor > div,
            :host ::ng-deep json-editor jsoneditor-outer {
                height: 30rem;
                width: 30rem;
            }
            .no-padding{
              padding: 0;
            }
            .btn-align {
              text-align: right;
            }
  `
  ]
})
export class DisplayUserDataComponent {
  public editorOptions: JsonEditorOptions;

  @ViewChild(JsonEditorComponent, {static: true}) editor: JsonEditorComponent;

  @Input() data: string;

  constructor(public dialogRef: NbDialogRef<DisplayUserDataComponent>) {
    this.editorOptions = new JsonEditorOptions()
    this.editorOptions.modes = ['code', 'text', 'tree', 'view'];
    this.editorOptions.mode = 'view';
    this.editorOptions.expandAll = true;
    this.editorOptions.mainMenuBar = false;
    this.editorOptions.navigationBar = false;
  }

  close() {
    this.dialogRef.close();
  }
}
