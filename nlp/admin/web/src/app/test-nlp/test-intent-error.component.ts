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
import {AfterViewInit, Component, EventEmitter, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {MatPaginator} from "@angular/material/paginator";
import {DataSource} from "@angular/cdk/collections";
import {BehaviorSubject, merge, Observable, Subscription} from "rxjs";
import {IntentTestError, TestErrorQuery} from "../model/nlp";
import {StateService} from "../core-nlp/state.service";
import {Router} from "@angular/router";
import {QualityService} from "../quality-nlp/quality.service";
import {escapeRegex} from "../model/commons";
import {DialogService} from "../core-nlp/dialog.service";
import {NbToastrService} from '@nebular/theme';
import {UserRole} from "../model/auth";
import {NlpService} from "../nlp-tabs/nlp.service";

@Component({
  selector: 'tock-test-intent-error',
  templateUrl: './test-intent-error.component.html',
  styleUrls: ['./test-intent-error.component.css']
})
export class TestIntentErrorComponent implements OnInit, AfterViewInit, OnDestroy {

  displayedColumns = ['text', 'currentIntent', 'wrongIntent', 'count', 'percent', 'probability', 'firstErrorDate', 'actions'];
  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  dataSource: TestIntentErrorDataSource | null;
  private subscription: Subscription;

  constructor(public state: StateService,
              private quality: QualityService,
              private toastrService: NbToastrService,
              private router: Router,
              private dialog: DialogService,
              private nlp: NlpService) {
  }

  ngOnInit(): void {
    this.dataSource = new TestIntentErrorDataSource(this.paginator, this.state, this.quality);
    this.subscription = this.state.configurationChange.subscribe(_ => this.search());
  }

  ngAfterViewInit(): void {
    this.search();
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  search() {
    this.dataSource.refresh();
  }

  validate(error: IntentTestError) {
    this.quality.deleteIntentError(error).subscribe(
      e => {
        this.toastrService.show(`Sentence validated`, "Validate Intent", {duration: 2000});
        this.dataSource.refresh()
      }
    )
  }

  change(error: IntentTestError) {
    this.quality.deleteIntentError(error).subscribe(
      e => {
        this.router.navigate(
          ['/nlp/search'],
          {
            queryParams: {
              text: "^" + escapeRegex(error.sentence.text) + "$",
              status: "model"
            }
          }
        );
      }
    )
  }

  download() {
    setTimeout(_ => {
      this.quality.searchIntentErrorsBlob(
        TestErrorQuery.create(
          this.state,
          0,
          100000,
          this.dataSource.intent === "" ? undefined : this.dataSource.intent
        )
      ).subscribe(blob => {
        saveAs(blob, this.state.currentApplication.name + "_intent_errors.json");
        this.dialog.notify(`Dump provided`, "Dump");
      })
    }, 1);
  }

  canReveal(error: IntentTestError): boolean {
    return error.sentence.key && this.state.hasRole(UserRole.admin);
  }

  reveal(error: IntentTestError) {
    const sentence = error.sentence;
    this.nlp.revealSentence(sentence).subscribe(s => {
      sentence.text = s.text;
      sentence.key = null;
      error.sentence = sentence.clone();
    });
  }
}

export class TestIntentErrorDataSource extends DataSource<IntentTestError> {

  size: number = 0;
  intent: string = "";
  private refreshEvent = new EventEmitter();
  private subject = new BehaviorSubject([]);

  constructor(private _paginator: MatPaginator,
              public state: StateService,
              private qualityService: QualityService) {
    super();
  }

  refresh() {
    this.refreshEvent.emit(true);
  }

  /** Connect function called by the table to retrieve one stream containing the data to render. */
  connect(): Observable<IntentTestError[]> {
    const displayDataChanges = [
      this._paginator.page,
      this.refreshEvent
    ];

    merge(...displayDataChanges).subscribe(() => {
      const startIndex = this._paginator.pageIndex * this._paginator.pageSize;

      this.qualityService.searchIntentErrors(
        TestErrorQuery.create(
          this.state,
          startIndex,
          this._paginator.pageSize,
          this.intent === "" ? undefined : this.intent
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
