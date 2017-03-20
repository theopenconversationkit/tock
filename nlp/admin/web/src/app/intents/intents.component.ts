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

import {Component, OnInit} from "@angular/core";
import {StateService} from "../core/state.service";
import {Intent} from "../model/application";
import {EntityDefinition} from "../model/nlp";
import {MdDialog, MdSnackBar} from "@angular/material";
import {ConfirmDialogComponent} from "../shared/confirm-dialog/confirm-dialog.component";
import {NlpService} from "../nlp-tabs/nlp.service";

@Component({
  selector: 'tock-intents',
  templateUrl: './intents.component.html',
  styleUrls: ['./intents.component.css']
})
export class IntentsComponent implements OnInit {

  constructor(public state: StateService,
    private nlp: NlpService,
    private snackBar: MdSnackBar,
    private dialog: MdDialog) {
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
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result === "remove") {
        this.nlp.removeIntent(this.state.currentApplication, intent).subscribe(
          result => {
            this.state.currentApplication.removeIntentById(intent._id);
            this.snackBar.open(`Intent ${intent.name} removed`, "Remove Intent", {duration: 1000});
          });
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
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result === "remove") {
        this.nlp.removeEntity(this.state.currentApplication, intent, entity).subscribe(
          result => {
            this.state.currentApplication.intentById(intent._id).removeEntity(entity);
            this.snackBar.open(`Entity ${entityName} removed from intent`, "Remove Entity", {duration: 1000});
          });
      }
    });
  }

}
