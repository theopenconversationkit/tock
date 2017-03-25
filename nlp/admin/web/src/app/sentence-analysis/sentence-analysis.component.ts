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

import {Component, OnInit, Input, Output} from "@angular/core";
import {Sentence, ClassifiedEntity, SentenceStatus} from "../model/nlp";
import {EventEmitter} from "@angular/common/src/facade/async";
import {StateService} from "../core/state.service";
import {Intent} from "../model/application";
import {NlpService} from "../nlp-tabs/nlp.service";

@Component({
  selector: 'tock-sentence-analysis',
  templateUrl: './sentence-analysis.component.html',
  styleUrls: ['./sentence-analysis.component.css']
})
export class SentenceAnalysisComponent implements OnInit {

  @Input() @Output() sentence: Sentence;
  @Output() closed = new EventEmitter();

  intentCreation: boolean = false;

  constructor(public state: StateService, private nlp: NlpService) {
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
      this.intentCreation = true;
    } else {
      this.sentence.classification.intentId = value;
    }
    this.sentence = this.sentence.clone();
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

  }

  createIntent(name) {
    this.nlp.saveIntent(new Intent(name, this.state.user.organization, [], [this.state.currentApplication._id], null)).subscribe(intent => {
      this.state.currentApplication.intents.push(intent);
      this.onIntentChange(intent._id);
      this.intentCreation = false;
    });
  }

}
