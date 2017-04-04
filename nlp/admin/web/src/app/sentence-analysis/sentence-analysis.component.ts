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

import {Component, Input, OnInit, Output} from "@angular/core";
import {ClassifiedEntity, Sentence, SentenceStatus} from "../model/nlp";
import {EventEmitter} from "@angular/common/src/facade/async";
import {StateService} from "../core/state.service";
import {Intent} from "../model/application";
import {NlpService} from "../nlp-tabs/nlp.service";
import {CreateIntentDialogComponent} from "./create-intent-dialog/create-intent-dialog.component";
import {MdDialog, MdSnackBar} from "@angular/material";

@Component({
  selector: 'tock-sentence-analysis',
  templateUrl: './sentence-analysis.component.html',
  styleUrls: ['./sentence-analysis.component.css']
})
export class SentenceAnalysisComponent implements OnInit {

  @Input() @Output() sentence: Sentence;
  @Output() closed = new EventEmitter();

  constructor(public state: StateService,
    private nlp: NlpService,
    private snackBar: MdSnackBar,
    private dialog: MdDialog) {
  }

  ngOnInit() {
  }

  onDeleteEntity(entity: ClassifiedEntity) {
    const entities = this.sentence.classification.entities;
    entities.splice(entities.indexOf(entity, 0), 1);
    this.sentence = this.sentence.clone();
  }

  onIntentChange(value) {
    //cleanup entities
    this.sentence.classification.entities = [];
    if (value === "newIntent") {
      let dialogRef = this.dialog.open(CreateIntentDialogComponent);
      dialogRef.afterClosed().subscribe(result => {
        if (result !== "cancel") {
          this.createIntent(result.name);
        } else {
          this.sentence.classification.intentId = undefined;
        }
      });
    } else {
      this.sentence.classification.intentId = value;
      this.sentence = this.sentence.clone();
    }

  }

  onLanguageChange(value) {
    //do nothing
  }

  onValidate() {
    this.update(SentenceStatus.validated);
  }

  onArchive() {
    this.sentence.classification.intentId = Intent.unknown;
    this.sentence.classification.entities = [];
    this.update(SentenceStatus.validated);
  }

  onDelete() {
    this.update(SentenceStatus.deleted);
  }

  private update(status: SentenceStatus) {
    this.sentence.status = status;
    this.nlp.updateSentence(this.sentence)
      .subscribe((s) => {
        this.closed.emit(this.sentence);
      });
    //delete old language
    if(this.sentence.language !== this.state.currentLocale) {
      const s = this.sentence.clone();
      s.language = this.state.currentLocale;
      s.status = SentenceStatus.deleted;
      this.nlp.updateSentence(s)
        .subscribe((s) => {
          this.snackBar.open(`Language change to ${this.state.localeName(this.sentence.language)}`, "Language change", {duration: 1000})
        });
    }

  }

  private createIntent(name) {
    this.nlp.saveIntent(new Intent(name, this.state.user.organization, [], [this.state.currentApplication._id], null)).subscribe(intent => {
      this.state.currentApplication.intents.push(intent);
      this.onIntentChange(intent._id);
    });
  }

}
