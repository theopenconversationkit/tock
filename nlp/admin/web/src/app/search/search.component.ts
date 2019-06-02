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
import {
  EntityDefinition,
  EntityType,
  getRoles,
  Sentence,
  SentenceStatus,
  TranslateSentencesQuery,
  UpdateSentencesQuery
} from "../model/nlp";
import {StateService} from "../core-nlp/state.service";
import {ActivatedRoute} from "@angular/router";
import {NlpService} from "../nlp-tabs/nlp.service";
import {MatSnackBar} from "@angular/material";
import {UserRole} from "../model/auth";

@Component({
  selector: 'tock-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {

  UserRole = UserRole;
  filter: SentenceFilter = new SentenceFilter();
  status: SentenceStatus;
  entityTypes: EntityType[];
  entityRoles: string[];
  selectedSentences: Sentence[];
  update: SentencesUpdate = new SentencesUpdate();
  targetLocale: string;

  @ViewChild(SentencesScrollComponent) scroll;

  constructor(public state: StateService,
              private nlp: NlpService,
              private snackBar: MatSnackBar,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.status = null;
    this.route.queryParams.subscribe(params => {
      if (params["text"]) {
        this.filter.search = params["text"];
      }
      if (params["status"]) {
        this.status = SentenceStatus[SentenceStatus[params["status"]]];
      }
      this.state.currentIntents.subscribe(i => {
        const search = this.filter.search;
        this.filter = new SentenceFilter();
        this.filter.search = search;
        this.selectedSentences = null;
        this.update = new SentencesUpdate();
        this.fillEntitiesFilter();
        this.search();
      })
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

  changeIntent() {
    this.filter.entityType = "";
    this.changeEntityType();
  }

  changeEntityType() {
    this.filter.entityRole = "";
    this.search();
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
    }, 1);
  }

  updateSentencesIntent() {
    this.update.oldEntity = null;
    this.update.newEntity = null;
    this.updateSentences();
  }

  updateSentencesEntity() {
    this.update.newIntentId = null;
    this.updateSentences();
  }

  updateSentences() {
    if (this.selectedSentences && this.selectedSentences.length === 0) {
      this.snackBar.open(`Please select at least one sentence first`, "UPDATE", {duration: 1000})
    } else {
      this.nlp.updateSentences(
        new UpdateSentencesQuery(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          this.selectedSentences ? this.selectedSentences : [],
          this.selectedSentences
            ? null
            : this.scroll.toSearchQuery(this.state.createPaginatedQuery(0, 100000)),
          this.update.newIntentId,
          this.update.oldEntity,
          this.update.newEntity
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

  translateSentences() {
    if(!this.targetLocale || this.targetLocale.length === 0) {
      this.snackBar.open(`Please select a target language first`, "UPDATE", {duration: 1000})
    } else if (this.selectedSentences && this.selectedSentences.length === 0) {
      this.snackBar.open(`Please select at least one sentence first`, "UPDATE", {duration: 1000})
    } else {
      this.nlp.translateSentences(
        new TranslateSentencesQuery(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          this.targetLocale,
          this.selectedSentences ? this.selectedSentences : [],
          this.selectedSentences
            ? null
            : this.scroll.toSearchQuery(this.state.createPaginatedQuery(0, 100000))
        )
      ).subscribe(r => {
        const n = r.nbTranslations;
        if (n === 0) {
          this.snackBar.open(`No sentence translated`, "UPDATE", {duration: 1000})
        } else if (n === 1) {
          this.snackBar.open(`1 sentence translated`, "UPDATE", {duration: 1000})
        } else {
          this.snackBar.open(`${n} sentences translated`, "UPDATE", {duration: 1000})
        }
      });
    }
  }
}

export class SentencesUpdate {

  constructor(public newIntentId?: string,
              public oldEntity?: EntityDefinition,
              public newEntity?: EntityDefinition) {
  }

}
