/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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
import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { NbDialogRef, NbDialogService } from '@nebular/theme';
import { JsonEditorComponent, JsonEditorOptions } from 'ang-jsoneditor';

import { ApplicationService } from '../../core-nlp/applications.service';
import { StateService } from '../../core-nlp/state.service';
import { UserLog } from '../../model/application';
import { PaginatedQuery } from '../../model/commons';

@Component({
  selector: 'tock-user-logs',
  templateUrl: './user-logs.component.html',
  styleUrls: ['./user-logs.component.css']
})
export class UserLogsComponent implements OnInit {
  dataSource: UserLog[];
  totalSize: number;
  pageSize: number = 10;
  pageIndex: number = 0;
  loading: boolean = false;

  constructor(private state: StateService, private applicationService: ApplicationService, private dialogService: NbDialogService) {}

  ngOnInit(): void {
    this.search();
  }

  appName(appId: string): string {
    if (!appId) {
      return null;
    }
    const r = this.state.applications.find((a) => a._id === appId);
    return r ? r.name : appId;
  }

  displayData(log: UserLog) {
    this.dialogService.open(DisplayUserDataComponent, {
      context: {
        data: JSON.parse(log.data())
      }
    });
  }

  getIndex() {
    if (this.pageIndex > 0) return this.pageIndex - 1;
    else return this.pageIndex;
  }

  search() {
    this.loading = true;
    const startIndex = this.getIndex() * this.pageSize;
    this.applicationService
      .searchUserLogs(
        new PaginatedQuery(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          startIndex,
          this.pageSize
        )
      )
      .subscribe((r) => {
        this.loading = false;
        this.totalSize = r.total;
        this.dataSource = r.logs;
      });
  }
}

@Component({
  selector: 'tock-display-full-log',
  template: ` <nb-card status="primary">
    <nb-card-header>User Action Data</nb-card-header>
    <nb-card-body class="no-padding">
      <json-editor
        [options]="editorOptions"
        [data]="data"
      ></json-editor>
    </nb-card-body>
    <nb-card-footer class="btn-align">
      <button
        nbButton
        status="primary"
        (click)="close()"
      >
        Close
      </button>
    </nb-card-footer>
  </nb-card>`,
  styles: [
    `
      :host ::ng-deep json-editor,
      :host ::ng-deep json-editor .jsoneditor,
      :host ::ng-deep json-editor > div,
      :host ::ng-deep json-editor jsoneditor-outer {
        height: 30rem;
        width: 30rem;
      }
      .no-padding {
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

  @ViewChild(JsonEditorComponent, { static: true }) editor: JsonEditorComponent;

  @Input() data: string;

  constructor(public dialogRef: NbDialogRef<DisplayUserDataComponent>) {
    this.editorOptions = new JsonEditorOptions();
    this.editorOptions.modes = ['code', 'view'];
    this.editorOptions.mode = 'view';
    this.editorOptions.expandAll = true;
    this.editorOptions.mainMenuBar = true;
    this.editorOptions.navigationBar = true;
  }

  close() {
    this.dialogRef.close();
  }
}
