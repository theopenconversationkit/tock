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
import {Intent, nameFromQualifiedName, Sentence, SentenceStatus} from "../model/nlp";
import {StateService} from "../core-nlp/state.service";
import {NlpService} from "../nlp-tabs/nlp.service";
import {IntentDialogComponent} from "./intent-dialog/intent-dialog.component";
import {MatDialog, MatSnackBar} from "@angular/material";
import {ApplicationConfig} from "../core-nlp/application.config";
import {ConfirmDialogComponent} from "../shared-nlp/confirm-dialog/confirm-dialog.component";
import {ReviewRequestDialogComponent} from "./review-request-dialog/review-request-dialog.component";

@Component({
  selector: 'tock-sentence-analysis',
  templateUrl: './sentence-analysis.component.html',
  styleUrls: ['./sentence-analysis.component.css']
})
export class SentenceAnalysisComponent implements OnInit {

  @Input() @Output() sentence: Sentence;
  @Input() displayUnknownButton: boolean = true;
  @Input() displayProbabilities: boolean = false;
  @Input() displayStatus: boolean = false;
  @Output() closed = new EventEmitter();
  @Input() displayEntities: Boolean = true;
  intentBeforeClassification: string;

  constructor(public state: StateService,
              private nlp: NlpService,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              public config: ApplicationConfig) {
  }

  ngOnInit() {
    this.intentBeforeClassification = this.sentence.classification.intentId
  }

  onIntentChange(value) {
    if (value === "newIntent") {
      this.newIntent();
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
          .filter(e => intent && intent.containsEntity(e.type, e.role));
      this.sentence = newSentence;
    }
  }

  newIntent() {
    //cleanup entities
    this.sentence.classification.entities = [];
    let dialogRef = this.dialog.open(IntentDialogComponent, {data: {create: true}});
    dialogRef.afterClosed().subscribe(result => {
      if (result.name) {
        if (this.createIntent(result.name, result.label, result.description, result.category)) {
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
  }

  onSentenceChange() {
    this.sentence = this.sentence.clone();
  }

  onLanguageChange(value) {
    //do nothing
  }

  onValidate() {
    this.sentence.forReview = false;
    this.sentence.reviewComment = "";
    this.validate();
  }

  private validate() {
    const intent = this.sentence.classification.intentId;
    if (!intent) {
      this.snackBar.open(`Please select an intent first`, "Error", {duration: 3000});
    } else if (intent === Intent.unknown) {
      this.onUnknown();
    } else {
      this.update(SentenceStatus.validated);
    }
  }

  onReviewRequest() {
    let dialogRef = this.dialog.open(ReviewRequestDialogComponent, {
      data: {
        beforeClassification: this.intentBeforeClassification,
        reviewComment: this.sentence.reviewComment
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result && result.status === 'confirm') {
        this.sentence.forReview = true;
        this.sentence.reviewComment = result.description;
        this.validate()
      }
    });
  }

  onUnknown() {
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

  private createIntent(name: string, label: string, description: string, category: string): boolean {
    if (StateService.intentExistsInApp(this.state.currentApplication, name) || name === nameFromQualifiedName(Intent.unknown)) {
      this.snackBar.open(`Intent ${name} already exists`, "Error", {duration: 5000});
      return false
    } else {
      if (this.state.intentExistsInOtherApplication(name)) {
        let dialogRef = this.dialog.open(ConfirmDialogComponent, {
          data: {
            title: "This intent is already used in an other application",
            subtitle: "If you confirm the name, the intent will be shared between the two applications.",
            action: "Confirm"
          }
        });
        dialogRef.afterClosed().subscribe(result => {
          if (result === "confirm") {
            this.saveIntent(name, label, description, category);
          }
        });
        return false;
      } else {
        this.saveIntent(name, label, description, category);
        return true;
      }
    }
  }

  private saveIntent(name: string, label: string, description: string, category: string) {
    this.nlp
      .saveIntent(
        new Intent(
          name,
          this.state.user.organization,
          [],
          [this.state.currentApplication._id],
          [],
          [],
          label,
          description,
          category)
      )
      .subscribe(intent => {
          this.state.addIntent(intent);
          this.onIntentChange(intent._id);
        },
        _ => this.onIntentChange(Intent.unknown));
  }

}
