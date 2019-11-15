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
import {map} from 'rxjs/operators';
import {Component, OnInit} from "@angular/core";
import {StateService} from "../core-nlp/state.service";
import {NlpService} from "../nlp-tabs/nlp.service";
import {MatDialog, MatDialogConfig, MatInput, MatSnackBar, MatSnackBarConfig} from "@angular/material";
import {ApplicationService} from "../core-nlp/applications.service";
import {Dictionary, EntityDefinition, EntityType, PredefinedValue} from "../model/nlp";
import {ConfirmDialogComponent} from "../shared-nlp/confirm-dialog/confirm-dialog.component";
import {JsonUtils} from "../model/commons";
import {FileItem, FileUploader, ParsedResponseHeaders} from "ng2-file-upload";

@Component({
  selector: 'tock-entities',
  templateUrl: './entities.component.html',
  styleUrls: ['./entities.component.css']
})
export class EntitiesComponent implements OnInit {

  selectedEntityType: EntityType;
  selectedDictionary: Dictionary;
  public uploader: FileUploader;

  constructor(public state: StateService,
              private nlp: NlpService,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private applicationService: ApplicationService) {
  }

  ngOnInit() {
    this.uploader = new FileUploader({autoUpload: true, removeAfterUpload: true});
    this.uploader.onCompleteItem =
      (item: FileItem, response: string, status: number, headers: ParsedResponseHeaders) => {
        const d = Dictionary.fromJSON(JSON.parse(response));
        this.selectedDictionary = d;
        this.selectedEntityType.dictionary = d.values.length !== 0;
        this.nlp.updateEntityType(this.selectedEntityType).subscribe(s => {
          if (s) this.refreshEntityType(this.selectedEntityType);
        });
        this.snackBar.open(`Dictionary imported`, "Dictionary", {duration: 1000});
      };
  }

  downloadDictionary() {
    saveAs(new Blob([JsonUtils.stringify(this.selectedDictionary)]), "dictionary_" + this.selectedEntityType.name + ".json");
    this.snackBar.open(`Dictionary exported`, "Dictionary", {duration: 1000});
  }

  update(entity: EntityDefinition) {
    this.nlp.updateEntityDefinition(
      this.state.createUpdateEntityDefinitionQuery(entity)
    ).pipe(map(_ => this.applicationService.reloadCurrentApplication()))
      .subscribe(_ => this.snackBar.open(`Entity updated`, "Update", {duration: 1000} as MatSnackBarConfig<any>));
  }

  deleteEntityType(entityType: EntityType) {
    let dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: `Remove the entity type ${entityType.name}`,
        subtitle: "Are you sure? This can completely cleanup your model!",
        action: "Remove"
      }
    } as MatDialogConfig<any>);
    dialogRef.afterClosed().subscribe(result => {
      if (result === "remove") {
        this.nlp.removeEntityType(entityType).subscribe(
          _ => {
            this.state.resetConfiguration();
            this.snackBar.open(`Entity Type ${entityType.name} removed`, "Remove Entity Type", {duration: 1000} as MatSnackBarConfig<any>);
          },
          _ => this.snackBar.open(`Delete Entity Type ${entityType.name} failed`, "Error", {duration: 5000} as MatSnackBarConfig<any>)
        );
      }
    });
  }

  private refreshEntityType(entityType: EntityType) {
    this.selectedEntityType = entityType;
    const types = this.state.entityTypes.getValue();
    types[types.findIndex(e => e.name === entityType.name)] = entityType;
    this.state.entityTypes.next(types);
  }

  selectEntityType(entityType: EntityType) {
    if (entityType.namespace() === this.state.currentApplication.namespace) {
      this.selectedEntityType = entityType;
      this.nlp.getDictionary(entityType).subscribe(
        d => {
          this.selectedDictionary = d;
          this.nlp.prepareDictionaryJsonDumpUploader(this.uploader, d.entityName);
          //save dictionary if not exists
          if (d.values.length === 0) {
            this.nlp.saveDictionary(d).subscribe(
              _ => this.selectedDictionary = d
            );
          }
        })
    } else {
      this.selectedEntityType = null;
      this.selectedDictionary = null;
    }
  }

  updateDictionary() {
    this.nlp.saveDictionary(this.selectedDictionary).subscribe(
      _ => this.snackBar.open(`Configuration Updated`, "Update", {duration: 5000} as MatSnackBarConfig<any>)
    );
  }

  updatePredefinedValueName(predefinedValue: PredefinedValue, input: MatInput) {
    const newValue = input.value;
    const oldValue = predefinedValue.value;
    if (oldValue !== newValue) {
      if (newValue.trim() === "") {
        this.snackBar.open(`Empty Predefined Value is not allowed`, "Error", {duration: 5000} as MatSnackBarConfig<any>);
        input.value = oldValue;
        input.focus();
      } else {
        if (this.selectedDictionary.values.some(v => v.value === newValue)) {
          this.snackBar.open(`Predefined Value already exist`, "Error", {duration: 5000} as MatSnackBarConfig<any>);
          input.value = oldValue;
          input.focus();
        } else {
          this.nlp.createOrUpdatePredefinedValue(
            this.state.createPredefinedValueQuery(this.selectedEntityType.name, newValue, oldValue)).subscribe(
            next => {
              this.selectedDictionary = next;
            },
            error => {
              input.value = oldValue;
              input.focus();
              this.snackBar.open(`Update Predefined Value '${name}' failed`, "Error", {duration: 5000} as MatSnackBarConfig<any>)
            })
        }
      }
    }
  }

  createPredefinedValue(name: string) {
    if (name.trim() === "") {
      this.snackBar.open(`Empty Predefined Value is not allowed`, "Error", {duration: 5000} as MatSnackBarConfig<any>);
    } else {
      this.nlp.createOrUpdatePredefinedValue(
        this.state.createPredefinedValueQuery(this.selectedEntityType.name, name)).subscribe(
        next => {
          this.selectedDictionary = next;
          if (next.values.length === 1) {
            this.selectedEntityType.dictionary = true;
            this.nlp.updateEntityType(this.selectedEntityType).subscribe(s => {
              if (s) this.refreshEntityType(this.selectedEntityType);
            });
          }
        },
        _ => this.snackBar.open(`Create Predefined Value '${name}' failed`, "Error", {duration: 5000} as MatSnackBarConfig<any>))
    }
  }

  deletePredefinedValue(name: string) {
    this.nlp.deletePredefinedValue(
      this.state.createPredefinedValueQuery(this.selectedEntityType.name, name)).subscribe(
      next => {
        let index = -1;
        this.selectedDictionary.values.forEach((pv, i) => {
          if (pv.value === name) {
            index = i;
          }
        });
        if (index > -1) {
          this.selectedDictionary.values.splice(index, 1);
        }
        if (this.selectedDictionary.values.length === 0) {
          this.selectedEntityType.dictionary = false;
          this.nlp.updateEntityType(this.selectedEntityType).subscribe(s => {
            if (s) this.refreshEntityType(this.selectedEntityType);
          });
        }
      },
      _ => this.snackBar.open(`Delete Predefined Value '${name}' failed`, "Error", {duration: 5000} as MatSnackBarConfig<any>))
  }

  createLabel(predefinedValue: PredefinedValue, name: string) {
    if (name.trim() === "") {
      this.snackBar.open(`Empty Label is not allowed`, "Error", {duration: 5000} as MatSnackBarConfig<any>);
    } else {
      this.nlp.createLabel(
        this.state.createPredefinedLabelQuery(
          this.selectedEntityType.name,
          predefinedValue.value,
          this.state.currentLocale,
          name))
        .subscribe(
          next => {
            this.selectedDictionary = next;
          },
          _ => this.snackBar.open(`Create Label '${name}' for Predefined Value '${predefinedValue.value}' failed`, "Error", {duration: 5000} as MatSnackBarConfig<any>))
    }
  }

  deleteLabel(predefinedValue: PredefinedValue, name: string) {
    this.nlp.deleteLabel(
      this.state.createPredefinedLabelQuery(
        this.selectedEntityType.name,
        predefinedValue.value,
        this.state.currentLocale,
        name))
      .subscribe(
        next => {
          let locale = this.state.currentLocale;
          this.selectedDictionary.values.forEach(function (pv) {
            if (pv.value === predefinedValue.value) {
              pv.labels.set(locale, pv.labels.get(locale).filter(s => {
                return s !== name
              }));
            }
          })
        },
        _ => this.snackBar.open(`Delete Label '${name}' for Predefined Value '${predefinedValue.value}' failed`, "Error", {duration: 5000} as MatSnackBarConfig<any>))
  }

}
