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
    if (entityType.namespace() === this.state.currentApplication.namespace) {
      this.selectedEntityType = entityType;
    } else {
      this.selectedEntityType = null;
    }
  }

  createPredefinedValue(name: string) {
    this.nlp.createPredefinedValue(
      this.state.createPredefinedValueQuery(this.selectedEntityType.name, name)).subscribe(
      next => {
        this.selectedEntityType = next
      },
      error => this.snackBar.open(`Create Predefined Value '${name}' failed`, "Error", {duration: 5000} as MdSnackBarConfig))
  }

  deletePredefinedValue(name: string) {
    this.nlp.deletePredefinedValue(
      this.state.createPredefinedValueQuery(this.selectedEntityType.name, name)).subscribe(
      next => {
        let index = -1;
        this.selectedEntityType.predefinedValues.forEach( (pv, i) => {
          if (pv.value === name) {
            index = i;
          }
        });
        if (index > -1) {
          this.selectedEntityType.predefinedValues.splice(index, 1);
        }
      },
      error => this.snackBar.open(`Delete Predefined Value '${name}' failed`, "Error", {duration: 5000} as MdSnackBarConfig))
  }

  createSynonym(predefinedValue: PredefinedValue, name: string) {
    this.nlp.createSynonym(
      this.state.createPredefinedSynonymQuery(
        this.selectedEntityType.name,
        predefinedValue.value,
        this.state.currentLocale,
        name))
      .subscribe(
      next => {
        this.selectedEntityType = next
      },
      error => this.snackBar.open(`Create Synonym '${name}' for Predefined Value '${predefinedValue.value}' failed`, "Error", {duration: 5000} as MdSnackBarConfig))
  }

  deleteSynonym(predefinedValue: PredefinedValue, name: string) {
    this.nlp.deleteSynonym(
      this.state.createPredefinedSynonymQuery(
        this.selectedEntityType.name,
        predefinedValue.value,
        this.state.currentLocale,
        name))
      .subscribe(
      next => {
        let locale = this.state.currentLocale
        this.selectedEntityType.predefinedValues.forEach(function(pv) {
          if (pv.value === predefinedValue.value) {
            let anies = pv.synonyms.get(locale).filter(s => { return s !== name });
            pv.synonyms.set(locale, anies);
            console.log(anies)
          }
        })
      },
      error => this.snackBar.open(`Delete Synonym '${name}' for Predefined Value '${predefinedValue.value}' failed`, "Error", {duration: 5000} as MdSnackBarConfig))
  }

}
