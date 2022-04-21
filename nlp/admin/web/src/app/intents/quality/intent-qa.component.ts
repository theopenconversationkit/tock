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

import { Component, OnDestroy, OnInit } from '@angular/core';
import { DataSource } from '@angular/cdk/collections';
import { QualityService } from '../../quality-nlp/quality.service';
import { IntentQA, LogStatsQuery } from '../../model/nlp';
import { StateService } from '../../core-nlp/state.service';
import { Observable, of, Subscription } from 'rxjs';

@Component({
  selector: 'intent-qa',
  templateUrl: './intent-qa.component.html',
  styleUrls: ['./intent-qa.component.css']
})
export class IntentQAComponent implements OnInit, OnDestroy {
  displayedColumns = ['intent1', 'intent2', 'occurrences', 'average'];

  public dataSource: IntentQA[];
  public minOccurrences: number = 30;

  private subscription: Subscription;

  constructor(private state: StateService, private quality: QualityService) {}

  ngOnInit(): void {
    this.updateContent();
    this.subscription = this.state.configurationChange.subscribe((_) => this.updateContent());
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  updateContent(): void {
    this.quality
      .intentQA(
        new LogStatsQuery(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          '',
          this.minOccurrences
        )
      )
      .subscribe((result) => {
        const r = result.map((p) => {
          return new IntentQA(
            this.state.intentLabelByName(p.intent1),
            this.state.intentLabelByName(p.intent2),
            p.occurrences,
            p.average
          );
        });
        this.dataSource = r;
      });
  }
}
