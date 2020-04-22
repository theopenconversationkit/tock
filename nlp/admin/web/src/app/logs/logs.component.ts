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

import {saveAs} from "file-saver";
import {Component, Input, ViewChild} from "@angular/core";
import {Log, LogsQuery, PaginatedResult} from "../model/nlp";
import {ScrollComponent} from "../scroll/scroll.component";
import {StateService} from "../core-nlp/state.service";
import {NlpService} from "../nlp-tabs/nlp.service";
import {PaginatedQuery, SearchMark} from "../model/commons";
import {Observable} from "rxjs";
import {ApplicationConfig} from "../core-nlp/application.config";
import {NbDialogRef, NbDialogService, NbToastrService} from '@nebular/theme';
import {JsonEditorComponent, JsonEditorOptions} from 'ang-jsoneditor';

@Component({
  selector: 'tock-logs',
  templateUrl: './logs.component.html',
  styleUrls: ['./logs.component.css']
})
export class LogsComponent extends ScrollComponent<Log> {

  title: string = "Logs";
  text: string;
  test: boolean = false;
  onlyCurrentLocale: boolean = false;

  constructor(state: StateService,
              private nlp: NlpService,
              private dialogService: NbDialogService,
              private config: ApplicationConfig,
              private toastrService: NbToastrService) {
    super(state);
  }

  protected searchMark(t: Log): SearchMark {
    return new SearchMark(
      t.textRequest(),
      t.date
    );
  }

  search(query: PaginatedQuery): Observable<PaginatedResult<Log>> {
    return this.nlp.searchLogs(new LogsQuery(
      query.namespace,
      query.applicationName,
      this.onlyCurrentLocale ? query.language : null,
      query.start,
      query.size,
      query.searchMark,
      this.text,
      this.test
    ))
      ;
  }


  dataEquals(d1: Log, d2: Log): boolean {
    return d1.request === d2.request;
  }

  displayFullLog(log: Log) {
    this.dialogService.open(DisplayFullLogComponent, {
      context: {
        request: JSON.parse(log.requestDetails()),
        response: JSON.parse(log.responseDetails())
      }
    });
  }

  downloadDump() {
    setTimeout(_ => {
      this.nlp.exportLogs(
        this.state.currentApplication,
        this.state.currentLocale)
        .subscribe(blob => {
          saveAs(blob, this.state.currentApplication.name + "_" + this.state.currentLocale + "_logs.csv");
          this.toastrService.show(`Export provided`, "Dump", {duration: 2000});
        })
    }, 1);
  }
}

@Component({
  selector: 'tock-display-full-log',
  template: `
    <nb-card status="primary">
      <nb-card-header>
        Full Log
      </nb-card-header>
      <nb-card-body class="body-content">
        <nb-card status="info" style="margin: 0">
          <nb-card-header style="border-top-left-radius: 0; border-top-right-radius: 0; text-align: center;">
            Request
          </nb-card-header>
          <nb-card-body style="padding: 0">
            <json-editor [options]="editorOptions" [data]="request"></json-editor>
          </nb-card-body>
        </nb-card>

        <nb-card status="success" style="margin: 0">
          <nb-card-header style="border-top-left-radius: 0; border-top-right-radius: 0; text-align: center;">
            Response
          </nb-card-header>
          <nb-card-body style="padding: 0">
            <json-editor [options]="editorOptions" [data]="response"></json-editor>
          </nb-card-body>
        </nb-card>
      </nb-card-body>
      <nb-card-footer class="btn-align">
        <button nbButton status="primary" (click)="close()">Close</button>
      </nb-card-footer>
    </nb-card>
  `,
  styles: [`:host ::ng-deep json-editor,
            :host ::ng-deep json-editor .jsoneditor,
            :host ::ng-deep json-editor > div,
            :host ::ng-deep json-editor jsoneditor-outer {
              height: 30rem;
              width: 25rem;
            }
            .body-content {
              padding: 0;
              display: inline-flex;
            }
            .btn-align {
              text-align: right;
            }
  `
  ]
})
export class DisplayFullLogComponent {
  public editorOptions: JsonEditorOptions;
  @Input() request: string;
  @Input() response: string;

  @ViewChild(JsonEditorComponent, { static: true }) editor: JsonEditorComponent;

  constructor(public dialogRef: NbDialogRef<DisplayFullLogComponent>) {

    this.editorOptions = new JsonEditorOptions();
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
