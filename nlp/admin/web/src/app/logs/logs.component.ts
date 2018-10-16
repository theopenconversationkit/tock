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

import {Component, Inject} from "@angular/core";
import {Log, LogsQuery, PaginatedResult} from "../model/nlp";
import {ScrollComponent} from "../scroll/scroll.component";
import {StateService} from "../core/state.service";
import {NlpService} from "../nlp-tabs/nlp.service";
import {PaginatedQuery, SearchMark} from "../model/commons";
import {Observable} from "rxjs/Observable";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material";
import {ApplicationConfig} from "../core/application.config";
import {Router} from "@angular/router";

@Component({
  selector: 'tock-logs',
  templateUrl: './logs.component.html',
  styleUrls: ['./logs.component.css']
})
export class LogsComponent extends ScrollComponent<Log> {

  title: string = "Logs";
  text: string;

  constructor(state: StateService,
              private nlp: NlpService,
              private dialog: MatDialog,
              private config: ApplicationConfig,
              private router: Router) {
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
      query.language,
      query.start,
      query.size,
      query.searchMark,
      this.text));
  }


  dataEquals(d1: Log, d2: Log): boolean {
    return d1.request === d2.request;
  }

  displayConversation(log: Log) {
    this.router.navigate(
      [this.config.displayDialogUrl],
      {
        queryParams: {
          dialogId: log.dialogId,
          text: log.error ? log.textRequest() : log.sentence.text
        }
      }
    );
  }

  displayFullLog(log: Log) {
    this.dialog.open(DisplayFullLogComponent, {
      data: {
        request: log.requestDetails(),
        response: log.responseDetails()
      }
    })
  }
}

@Component({
  selector: 'tock-display-full-log',
  template: `<h1 mat-dialog-title>Request Full Log</h1>
  <div mat-dialog-content>
    Request:
    <pre>{{data.request}}</pre>
    Response:
    <pre>{{data.response}}</pre>
  </div>
  <div mat-dialog-actions>
    <button mat-raised-button mat-dialog-close color="primary">Close</button>
  </div>`
})
export class DisplayFullLogComponent {

  constructor(public dialogRef: MatDialogRef<DisplayFullLogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {

  }


}
