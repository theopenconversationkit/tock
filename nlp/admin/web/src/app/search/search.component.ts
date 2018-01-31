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

import {Component, OnInit, ViewChild} from "@angular/core";
import {SentenceFilter, SentencesScrollComponent} from "../sentences-scroll/sentences-scroll.component";
import {EntityType, getRoles, SentenceStatus, UpdateSentencesQuery} from "../model/nlp";
import {StateService} from "../core/state.service";
import {ActivatedRoute} from "@angular/router";
import {NlpService} from "../nlp-tabs/nlp.service";
import {MdSnackBar} from "@angular/material";

@Component({
  selector: 'tock-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {

  filter: SentenceFilter = new SentenceFilter();
  status: SentenceStatus;
  entityTypes: EntityType[];
  entityRoles: string[];
  update: SentencesUpdate = new SentencesUpdate();

  @ViewChild(SentencesScrollComponent) scroll;

  constructor(public state: StateService,
              private nlp: NlpService,
              private snackBar: MdSnackBar,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.filter.search = params["text"];
      this.fillEntitiesFilter();
    });
  }

  private fillEntitiesFilter() {
    this.state.entityTypesSortedByName()
      .subscribe(entities => {
          if (!this.filter.intentId || this.filter.intentId === "-1") {
            this.entityTypes = entities;
            this.entityRoles = getRoles(this.state.currentIntents.value, this.filter.entityType);
          } else {
            const intent = this.state.findIntentById(this.filter.intentId);
            if (intent) {
              this.entityTypes =
                entities.filter(
                  e => intent.entities.some(intentEntity => intentEntity.entityTypeName === e.name));
              this.entityRoles = getRoles([intent], this.filter.entityType);
            } else {
              this.entityTypes = [];
              this.entityRoles = [];
            }
          }
        }
      );
  }

  search() {
    setTimeout(_ => {
      if (this.status) {
        this.filter.status = [this.status];
      } else {
        this.filter.status = [];
      }
      if (this.filter.intentId === "-1") {
        this.filter.intentId = null;
      }
      this.fillEntitiesFilter();

      if (this.filter.search) {
        this.filter.search = this.filter.search.trim()
      }
      this.scroll.refresh();
    });
  }

  updateSentences() {
    this.nlp.updateSentences(
      new UpdateSentencesQuery(
        this.state.currentApplication.namespace,
        this.state.currentApplication.name,
        this.state.currentLocale,
        this.scroll.toSearchQuery(this.state.createPaginatedQuery(0, 10000)),
        this.update.newIntentId
      )
    ).subscribe(r => {
      const n = r.nbUpdates;
      if (n === 0) {
        this.snackBar.open(`No sentence updated`, "UPDATE", {duration: 1000})
      } else if (n === 1) {
        this.snackBar.open(`1 sentence updated`, "UPDATE", {duration: 1000})
      } else {
        this.snackBar.open(`${n} sentences updated`, "UPDATE", {duration: 1000})
      }
      this.scroll.refresh();
    });
  }
}

export class SentencesUpdate {

  constructor(public newIntentId?: string) {
  }

}
