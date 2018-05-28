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
import {NlpService} from "../nlp-tabs/nlp.service";
import {MdDialog, MdDialogConfig, MdSnackBar, MdSnackBarConfig} from "@angular/material";
import {ApplicationService} from "../core/applications.service";
import {EntityDefinition, EntityType, PredefinedValue} from "../model/nlp";
import {ConfirmDialogComponent} from "../shared/confirm-dialog/confirm-dialog.component";
import {DataSource} from "@angular/cdk/collections";

@Component({
  selector: 'tock-entities',
  templateUrl: './entities.component.html',
  styleUrls: ['./entities.component.css']
})
export class EntitiesComponent implements OnInit {

  private selectedEntityType: EntityType;

  constructor(public state: StateService,
              private nlp: NlpService,
              private snackBar: MdSnackBar,
              private dialog: MdDialog,
              private applicationService: ApplicationService) {
  }

  ngOnInit() {
  }

  update(entity: EntityDefinition) {
    this.nlp.updateEntityDefinition(
      this.state.createUpdateEntityDefinitionQuery(entity)
    ).map(_ => this.applicationService.reloadCurrentApplication())
      .subscribe(_ => this.snackBar.open(`Entity updated`, "Update", {duration: 1000} as MdSnackBarConfig));
  }

  deleteEntityType(entityType:EntityType) {
    let dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: `Remove the entity type ${entityType.name}`,
        subtitle: "Are you sure? This can completely cleanup your model!",
        action: "Remove"
      }
    } as MdDialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result === "remove") {
        this.nlp.removeEntityType(entityType).subscribe(
          _ => {
            this.state.resetConfiguration();
            this.snackBar.open(`Entity Type ${entityType.name} removed`, "Remove Entity Type", {duration: 1000} as MdSnackBarConfig);
          },
          _ => this.snackBar.open(`Delete Entity Type ${entityType.name} failed`, "Error", {duration: 5000} as MdSnackBarConfig)
        );
      }
    });
  }

  selectEntityType(entityType:EntityType) {
    console.log("Selected entity type :" + entityType.name);
    this.selectedEntityType = entityType;
  }

  createPredefinedValue(name: string) {
    console.log("Create predefined value for entity [" + this.selectedEntityType.name + "] -> [" + name + ", " + this.state.currentLocale + "]");
    this.nlp.createPredefinedValue(this.selectedEntityType.name, name).subscribe(
      next => {

      },
      error => this.snackBar.open(`Create Predefined Value '${name}' failed`, "Error", {duration: 5000} as MdSnackBarConfig))
  }

  deletePredefinedValue(name: string) {
    console.log("Delete predefined value for entity [" + this.selectedEntityType.name + "] -> [" + name + ", " + this.state.currentLocale + "]");
    this.nlp.deletePredefinedValue(this.selectedEntityType.name, name).subscribe(
      next => {

      },
      error => this.snackBar.open(`Delete Predefined Value '${name}' failed`, "Error", {duration: 5000} as MdSnackBarConfig))
  }

  createSynonym(predefinedValue: PredefinedValue, name: string) {
    console.log("Create synonym for predefined value [" + predefinedValue.value + ", " + this.state.currentLocale + "] -> " + name);
    this.nlp.createSynonym(
        this.selectedEntityType.name,
        predefinedValue.value,
        this.state.currentLocale,
        name)
      .subscribe(
      next => {

      },
      error => this.snackBar.open(`Create Synonym '${name}' for Predefined Value '${predefinedValue.value}' failed`, "Error", {duration: 5000} as MdSnackBarConfig))
  }

  deleteSynonym(predefinedValue: PredefinedValue, name: string) {
    console.log("Create synonym for predefined value [" + predefinedValue.value + ", " + this.state.currentLocale + "] -> " + name);
    this.nlp.deleteSynonym(
        this.selectedEntityType.name,
        predefinedValue.value,
        this.state.currentLocale,
        name)
      .subscribe(
      next => {

      },
      error => this.snackBar.open(`Delete Synonym '${name}' for Predefined Value '${predefinedValue.value}' failed`, "Error", {duration: 5000} as MdSnackBarConfig))
  }

}
