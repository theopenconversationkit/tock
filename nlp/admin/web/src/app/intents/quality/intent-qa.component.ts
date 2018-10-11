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

import {AfterViewInit, Component, OnInit} from "@angular/core";
import {DataSource} from "@angular/cdk/collections";
import {Observable} from "rxjs/Observable";
import {QualityService} from "../../quality/quality.service";
import {IntentQA} from "../../model/nlp";
import {StateService} from "../../core/state.service";

@Component({
  selector: 'intent-qa',
  templateUrl: './intent-qa.component.html',
  styleUrls: ['./intent-qa.component.css']
})
export class IntentQAComponent implements OnInit, AfterViewInit {

  displayedColumns = ['mainIntent', 'secondaryIntent', 'occurrence', 'average'];

  public dataSource: IntentQADataSource | null

  constructor(private state:StateService, private quality: QualityService) {
  }

  ngOnInit(): void {
    var r : Array<IntentQA>
    this.quality.intentQA(this.state.currentApplication.name)
      .subscribe(result => {
        const r = result.map(p => {
          console.log('element')
          console.log(p)
          return {
            mainIntent: p.mainIntent,
            secondaryIntent: p.secondaryIntent,
            occurrence: p.occurrence,
            average: p.average
          };
        });
        this.dataSource = new IntentQADataSource(r)
    })

  }

  ngAfterViewInit(): void {
  }

}

export class IntentQADataSource extends DataSource<IntentQA> {

  constructor(private intentQa: IntentQA[]) {
    super();
  }

  connect(): Observable<IntentQA[]> {
    return Observable.of(this.intentQa);
  }

  disconnect() {
  }

}
