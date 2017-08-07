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

import {saveAs} from "file-saver";
import {Component, OnInit} from "@angular/core";
import {StateService} from "../core/state.service";
import {Intent} from "../model/application";
import {EntityDefinition} from "../model/nlp";
import {MdDialog, MdDialogConfig, MdSnackBar, MdSnackBarConfig} from "@angular/material";
import {ConfirmDialogComponent} from "../shared/confirm-dialog/confirm-dialog.component";
import {NlpService} from "../nlp-tabs/nlp.service";
import {ApplicationService} from "../core/applications.service";
import {AddStateDialogComponent} from "./add-state/add-state-dialog.component";

@Component({
  selector: 'tock-intents',
  templateUrl: './intents.component.html',
  styleUrls: ['./intents.component.css']
})
export class IntentsComponent implements OnInit {

  constructor(public state: StateService,
              private nlp: NlpService,
              private snackBar: MdSnackBar,
              private dialog: MdDialog,
              private applicationService: ApplicationService) {
  }

  ngOnInit() {
  }

  deleteIntent(intent: Intent) {
    let dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: `Remove the Intent ${intent.name}`,
        subtitle: "Are you sure?",
        action: "Remove"
      }
    } as MdDialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result === "remove") {
        this.nlp.removeIntent(this.state.currentApplication, intent).subscribe(
          _ => {
            this.state.currentApplication.removeIntentById(intent._id);
            this.snackBar.open(`Intent ${intent.name} removed`, "Remove Intent", {duration: 1000} as MdSnackBarConfig);
          },
          _ => this.snackBar.open(`Delete Intent ${intent.name} failed`, "Error", {duration: 5000} as MdSnackBarConfig)
        );
      }
    });
  }

  removeState(intent: Intent, state: string) {
    intent.mandatoryStates.splice(intent.mandatoryStates.indexOf(state), 1);
    this.nlp.saveIntent(intent).subscribe(
      result => {
        this.snackBar.open(`State ${state} removed from Intent ${intent.name}`, "Remove State", {duration: 1000} as MdSnackBarConfig);
      },
      _ => {
        this.snackBar.open(`Remove State failed`, "Error", {duration: 5000} as MdSnackBarConfig)
      }
    );
  }

  addState(intent: Intent) {
    let dialogRef = this.dialog.open(AddStateDialogComponent, {
      data: {
        title: `Add a state for intent \"${intent.name}\"`
      }
    } as MdDialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result !== "cancel") {
        intent.mandatoryStates.push(result.name);
        this.nlp.saveIntent(intent).subscribe(
          result => {
            this.snackBar.open(`State ${result.name} added for Intent ${intent.name}`, "Add State", {duration: 1000} as MdSnackBarConfig);
          },
          _ => {
            intent.mandatoryStates.splice(intent.mandatoryStates.length - 1, 1);
            this.snackBar.open(`Add State failed`, "Error", {duration: 5000} as MdSnackBarConfig)
          }
        );
      }
    });
  }

  removeEntity(intent: Intent, entity: EntityDefinition) {
    const entityName = entity.qualifiedName(this.state.user);
    let dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: `Remove the Entity ${entityName}`,
        subtitle: "Are you sure?",
        action: "Remove"
      }
    } as MdDialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result === "remove") {
        this.nlp.removeEntity(this.state.currentApplication, intent, entity).subscribe(
          deleted => {
            this.state.currentApplication.intentById(intent._id).removeEntity(entity);
            if (deleted) {
              this.state.removeEntityTypeByName(entity.entityTypeName)
            }
            this.snackBar.open(`Entity ${entityName} removed from intent`, "Remove Entity", {duration: 1000} as MdSnackBarConfig);
          });
      }
    });
  }

  downloadSentencesDump(intent: Intent) {
    this.applicationService.getSentencesDumpForIntent(this.state.currentApplication, intent)
      .subscribe(blob => {
        saveAs(blob, intent.name + "_sentences.json");
        this.snackBar.open(`Dump provided`, "Dump", {duration: 1000} as MdSnackBarConfig);
      })
  }

}
