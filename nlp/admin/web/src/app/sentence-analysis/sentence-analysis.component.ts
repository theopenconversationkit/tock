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

import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {Sentence, SentenceStatus} from "../model/nlp";
import {StateService} from "../core/state.service";
import {Intent} from "../model/nlp";
import {NlpService} from "../nlp-tabs/nlp.service";
import {CreateIntentDialogComponent} from "./create-intent-dialog/create-intent-dialog.component";
import {MdDialog, MdSnackBar} from "@angular/material";
import {ApplicationConfig} from "../core/application.config";
import {Router} from "@angular/router";

@Component({
  selector: 'tock-sentence-analysis',
  templateUrl: './sentence-analysis.component.html',
  styleUrls: ['./sentence-analysis.component.css']
})
export class SentenceAnalysisComponent implements OnInit {

  @Input() @Output() sentence: Sentence;
  @Input() displayArchiveButton: boolean = true;
  @Input() displayProbabilities: boolean = false;
  @Input() displayStatus: boolean = false;
  @Output() closed = new EventEmitter();
  @Input() displayEntities: Boolean = true;

  constructor(public state: StateService,
              private nlp: NlpService,
              private snackBar: MdSnackBar,
              private dialog: MdDialog,
              public config: ApplicationConfig,
              private router: Router) {
  }

  ngOnInit() {
  }

  onIntentChange(value) {
    if (value === "newIntent") {
      //cleanup entities
      this.sentence.classification.entities = [];
      let dialogRef = this.dialog.open(CreateIntentDialogComponent);
      dialogRef.afterClosed().subscribe(result => {
        if (result !== "cancel") {
          if (this.createIntent(result.name)) {
            return;
          }
        }
        //we need to be sure the selected value has changed to avoid side effects
        if (this.sentence.classification.intentId) {
          this.sentence.classification.intentId = undefined;
        } else {
          this.onIntentChange(Intent.unknown);
        }
      });
    } else {
      const oldSentence = this.sentence;
      const newSentence = oldSentence.clone();

      const classification = newSentence.classification;
      classification.intentId = value;
      const intent = this.state.findIntentById(value);
      classification.entities =
        oldSentence
          .classification
          .entities
          .filter(e => intent.containsEntity(e.type, e.role));
      this.sentence = newSentence;
    }
  }

  onSentenceChange() {
    this.sentence = this.sentence.clone();
  }

  onLanguageChange(value) {
    //do nothing
  }

  onValidate() {
    const intent = this.sentence.classification.intentId;
    if (!intent) {
      this.snackBar.open(`Please select an intent first`, "Error", {duration: 3000});
    } else if (intent === Intent.unknown) {
      this.onArchive();
    } else {
      this.update(SentenceStatus.validated);
    }
  }

  onArchive() {
    this.sentence.classification.intentId = Intent.unknown;
    this.sentence.classification.entities = [];
    this.update(SentenceStatus.validated);
  }

  onDelete() {
    this.update(SentenceStatus.deleted);
  }

  displayDialogs() {
    this.router.navigate(
      [this.config.displayDialogUrl],
      {
        queryParams: {
          text: this.sentence.text
        }
      }
    );
  }

  private update(status: SentenceStatus) {
    this.sentence.status = status;
    this.nlp.updateSentence(this.sentence)
      .subscribe((s) => {
        this.closed.emit(this.sentence);
      });
    //delete old language
    if (this.sentence.language !== this.state.currentLocale) {
      const s = this.sentence.clone();
      s.language = this.state.currentLocale;
      s.status = SentenceStatus.deleted;
      this.nlp.updateSentence(s)
        .subscribe((s) => {
          this.snackBar.open(`Language change to ${this.state.localeName(this.sentence.language)}`, "Language change", {duration: 1000})
        });
    }

  }

  private createIntent(name): boolean {
    if (this.state.intentExists(name)) {
      this.snackBar.open(`Intent ${name} already exists`, "Error", {duration: 5000});
      return false
    } else {
      this.nlp
        .saveIntent(
          new Intent(
            name,
            this.state.user.organization,
            [],
            [this.state.currentApplication._id],
            [])
        )
        .subscribe(intent => {
            this.state.currentApplication.intents.push(intent);
            this.onIntentChange(intent._id);
          },
          _ => this.onIntentChange(Intent.unknown));
      return true;
    }
  }

}
