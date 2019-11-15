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

import {
  ApplicationRef,
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnInit,
  Output
} from "@angular/core";
import {Intent, nameFromQualifiedName, Sentence, SentenceStatus} from "../model/nlp";
import {StateService} from "../core-nlp/state.service";
import {NlpService} from "../nlp-tabs/nlp.service";
import {IntentDialogComponent} from "./intent-dialog/intent-dialog.component";
import {ApplicationConfig} from "../core-nlp/application.config";
import {ConfirmDialogComponent} from "../shared-nlp/confirm-dialog/confirm-dialog.component";
import {ReviewRequestDialogComponent} from "./review-request-dialog/review-request-dialog.component";
import {DialogService} from "../core-nlp/dialog.service";
import {MatDialog} from "@angular/material/dialog";

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
  @Input() displayEntities: boolean = true;
  @Input() minimalView: boolean = true;
  @Input() displayClose: boolean = false;
  intentBeforeClassification: string;

  constructor(public state: StateService,
              private nlp: NlpService,
              private dialog: DialogService,
              private matDialog: MatDialog,
              public config: ApplicationConfig,
              private applicationRef: ApplicationRef,
              private changeDetectorRef: ChangeDetectorRef,
              private elementRef: ElementRef) {
  }

  ngOnInit() {
    this.intentBeforeClassification = this.sentence.classification.intentId;
    if (this.minimalView) {
      setTimeout(_ => {
        this.changeDetectorRef.detach()
      });
    }
  }

  onIntentChange() {
    const value = this.sentence.classification.intentId;
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

  newIntent() {
    //cleanup entities
    this.sentence.classification.entities = [];
    let dialogRef = this.dialog.open(this.matDialog, IntentDialogComponent, {data: {create: true}});
    dialogRef.afterClosed().subscribe(result => {
      if (result.name) {
        if (this.createIntent(result.name, result.label, result.description, result.category)) {
          return;
        }
      }
      //we need to be sure the selected value has changed to avoid side effects
      if (!this.sentence.classification.intentId) {
        this.sentence.classification.intentId = Intent.unknown;
        this.onIntentChange();
      }
    });
  }

  displayAllView() {
    if (this.minimalView) {
      this.changeDetectorRef.reattach();
      this.minimalView = false;
    }
  }

  focusOnIntentSelect() {
    this.displayAllView();
    setTimeout(_ => {
      const e = this.elementRef.nativeElement.querySelector("nb-select");
      e.click();
      e.focus();
    });
  }

  onSentenceChange() {
    this.sentence = this.sentence.clone();
  }

  onLanguageChange() {
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
      this.dialog.notify(`Please select an intent first`);
    } else if (intent === Intent.unknown) {
      this.onUnknown();
    } else {
      this.update(SentenceStatus.validated);
    }
  }

  onReviewRequest() {
    setTimeout(_ => {
      let dialogRef = this.dialog.open(
        this.matDialog,
        ReviewRequestDialogComponent,
        {
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
          this.dialog.notify(`Language change to ${this.state.localeName(this.sentence.language)}`, "Language change");
        });
    }
  }

  private createIntent(name: string, label: string, description: string, category: string): boolean {
    if (StateService.intentExistsInApp(this.state.currentApplication, name) || name === nameFromQualifiedName(Intent.unknown)) {
      this.dialog.notify(`Intent ${name} already exists`);
      return false
    } else {
      if (this.state.intentExistsInOtherApplication(name)) {
        let dialogRef = this.dialog.open(this.matDialog, ConfirmDialogComponent, {
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
          this.sentence.classification.intentId = intent._id;
          this.onIntentChange();
        },
        _ => {
          this.sentence.classification.intentId = Intent.unknown;
          this.onIntentChange();
        });
  }

}
