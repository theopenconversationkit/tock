/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { saveAs } from 'file-saver-es';
import { map } from 'rxjs/operators';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { StateService } from '../../core-nlp/state.service';
import { NlpService } from '../../core-nlp/nlp.service';
import { ApplicationService } from '../../core-nlp/applications.service';
import { Dictionary, EntityDefinition, EntityType, PredefinedValue } from '../../model/nlp';
import { JsonUtils } from '../../model/commons';
import { FileItem, FileUploader, ParsedResponseHeaders } from 'ng2-file-upload';
import { NbToastrService } from '@nebular/theme';
import { DialogService } from '../../core-nlp/dialog.service';
import { Observable } from 'rxjs';
import { ChoiceDialogComponent } from '../../shared/components';
import { getExportFileName } from '../../shared/utils';

@Component({
  selector: 'tock-entities',
  templateUrl: './entities.component.html',
  styleUrls: ['./entities.component.scss']
})
export class EntitiesComponent implements OnInit {
  selectedEntityType: EntityType;
  selectedDictionary: Dictionary;
  showUploadDictionaryButton: boolean = true;
  public uploader: FileUploader;

  @ViewChild('addLabelInput') addLabelInput: ElementRef;

  constructor(
    public state: StateService,
    private nlp: NlpService,
    private toastrService: NbToastrService,
    private dialog: DialogService,
    private applicationService: ApplicationService
  ) {}

  ngOnInit(): void {
    this.uploader = new FileUploader({ url: undefined, autoUpload: true, removeAfterUpload: true });
    this.uploader.onCompleteItem = (item: FileItem, response: string, status: number, headers: ParsedResponseHeaders) => {
      const d = Dictionary.fromJSON(JSON.parse(response));
      this.selectedDictionary = d;
      this.selectedEntityType.dictionary = d.values.length !== 0;
      this.nlp.updateEntityType(this.selectedEntityType).subscribe((s) => {
        if (s) this.refreshEntityType(this.selectedEntityType);
        this.toastrService.show(`Dictionary imported`, 'Dictionary', {
          duration: 2000,
          status: 'success'
        });
      });
    };
  }

  entityTypesSortedByName(): Observable<EntityType[]> {
    return this.state.entityTypes.pipe(map((e) => e.sort((e1, e2) => e1.name.localeCompare(e2.name))));
  }

  downloadDictionary(): void {
    const exportFileName = getExportFileName(
      this.state.currentApplication.namespace,
      this.state.currentApplication.name,
      'Dictionary',
      'json',
      this.selectedEntityType.name
    );

    saveAs(new Blob([JsonUtils.stringify(this.selectedDictionary)]), exportFileName);

    this.toastrService.show(`Dictionary exported`, 'Dictionary', {
      duration: 2000,
      status: 'success'
    });
  }

  update(entity: EntityDefinition) {
    this.nlp
      .updateEntityDefinition(this.state.createUpdateEntityDefinitionQuery(entity))
      .pipe(map((_) => this.applicationService.reloadCurrentApplication()))
      .subscribe((_) => this.toastrService.show(`Entity updated`, 'Update', { duration: 2000, status: 'success' }));
  }

  deleteEntityType(entityType: EntityType) {
    const action = 'remove';
    let dialogRef = this.dialog.openDialog(ChoiceDialogComponent, {
      context: {
        title: `Remove the entity type ${entityType.name}`,
        subtitle: 'Are you sure? This can completely cleanup your model!',
        actions: [
          { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
          { actionName: action, buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result === action) {
        this.nlp.removeEntityType(entityType).subscribe(
          (_) => {
            this.state.resetConfiguration();
            this.toastrService.show(`Entity Type ${entityType.name} removed`, 'Remove Entity Type', {
              duration: 2000,
              status: 'success'
            });
          },
          (_) =>
            this.toastrService.show(`Delete Entity Type ${entityType.name} failed`, 'Error', {
              duration: 5000,
              status: 'danger'
            })
        );
      }
    });
  }

  private refreshEntityType(entityType: EntityType): void {
    this.selectedEntityType = entityType;
    const types = this.state.entityTypes.getValue();
    types[types.findIndex((e) => e.name === entityType.name)] = entityType;
    this.state.entityTypes.next(types);
  }

  selectEntityType(entityType: EntityType): void {
    if (entityType.namespace() === this.state.currentApplication.namespace) {
      this.selectedEntityType = entityType;
      this.nlp.getDictionary(entityType).subscribe((d) => {
        this.selectedDictionary = d;
        this.nlp.prepareDictionaryJsonDumpUploader(this.uploader, d.entityName);
        //save dictionary if not exists
        if (d.values.length === 0) {
          this.nlp.saveDictionary(d).subscribe((_) => (this.selectedDictionary = d));
        }
      });
    } else {
      this.unSelectEntityType();
    }
  }

  unSelectEntityType(): void {
    this.selectedEntityType = null;
    this.selectedDictionary = null;
  }

  updateEntityType(): void {
    this.nlp.updateEntityType(this.selectedEntityType).subscribe((_) =>
      this.toastrService.show(`Configuration Updated`, 'Update', {
        duration: 5000,
        status: 'success'
      })
    );
  }

  updateDictionary(): void {
    this.nlp.saveDictionary(this.selectedDictionary).subscribe((_) =>
      this.toastrService.show(`Configuration Updated`, 'Update', {
        duration: 5000,
        status: 'success'
      })
    );
  }

  updatePredefinedValueName(predefinedValue: PredefinedValue, input): void {
    const newValue = input.value;
    const oldValue = predefinedValue.value;
    if (oldValue !== newValue) {
      if (newValue.trim() === '') {
        this.toastrService.show(`Empty Predefined Value is not allowed`, 'Error', {
          duration: 5000,
          status: 'warning'
        });
        input.value = oldValue;
        input.focus();
      } else {
        if (this.selectedDictionary.values.some((v) => v.value === newValue)) {
          this.toastrService.show(`Predefined Value already exist`, 'Error', {
            duration: 5000,
            status: 'warning'
          });
          input.value = oldValue;
          input.focus();
        } else {
          this.nlp
            .createOrUpdatePredefinedValue(this.state.createPredefinedValueQuery(this.selectedEntityType.name, newValue, oldValue))
            .subscribe(
              (next) => {
                this.selectedDictionary = next;
              },
              (error) => {
                input.value = oldValue;
                input.focus();
                this.toastrService.show(`Update Predefined Value '${name}' failed`, 'Error', {
                  duration: 5000,
                  status: 'danger'
                });
              }
            );
        }
      }
    }
  }

  createPredefinedValue(name: string): void {
    if (name.trim() === '') {
      this.toastrService.show(`Empty Predefined Value is not allowed`, 'Error', {
        duration: 5000,
        status: 'danger'
      });
    } else {
      this.nlp.createOrUpdatePredefinedValue(this.state.createPredefinedValueQuery(this.selectedEntityType.name, name)).subscribe(
        (next) => {
          this.selectedDictionary = next;
          if (next.values.length === 1) {
            this.selectedEntityType.dictionary = true;
            this.nlp.updateEntityType(this.selectedEntityType).subscribe((s) => {
              if (s) this.refreshEntityType(this.selectedEntityType);
            });
          }
        },
        (_) =>
          this.toastrService.show(`Create Predefined Value '${name}' failed`, 'Error', {
            duration: 5000,
            status: 'danger'
          })
      );
    }
  }

  deletePredefinedValue(name: string): void {
    this.nlp.deletePredefinedValue(this.state.createPredefinedValueQuery(this.selectedEntityType.name, name)).subscribe(
      (next) => {
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
          this.nlp.updateEntityType(this.selectedEntityType).subscribe((s) => {
            if (s) this.refreshEntityType(this.selectedEntityType);
          });
        }
      },
      (_) =>
        this.toastrService.show(`Delete Predefined Value '${name}' failed`, 'Error', {
          duration: 5000,
          status: 'danger'
        })
    );
  }

  createLabel(predefinedValue: PredefinedValue, name: string): void {
    if (name.trim() === '') return;

    this.nlp
      .createLabel(
        this.state.createPredefinedLabelQuery(this.selectedEntityType.name, predefinedValue.value, this.state.currentLocale, name)
      )
      .subscribe({
        next: (next) => {
          this.selectedDictionary = next;
          setTimeout(() => {
            this.addLabelInput.nativeElement.focus();
          });
        },
        error: (_) =>
          this.toastrService.show(`Create Label '${name}' for Predefined Value '${predefinedValue.value}' failed`, 'Error', {
            duration: 5000,
            status: 'danger'
          })
      });
  }

  deleteLabel(predefinedValue: PredefinedValue, name: string): void {
    this.nlp
      .deleteLabel(
        this.state.createPredefinedLabelQuery(this.selectedEntityType.name, predefinedValue.value, this.state.currentLocale, name)
      )
      .subscribe({
        next: () => {
          let locale = this.state.currentLocale;
          this.selectedDictionary.values.forEach(function (pv) {
            if (pv.value === predefinedValue.value) {
              pv.labels.set(
                locale,
                pv.labels.get(locale).filter((s) => {
                  return s !== name;
                })
              );
            }
          });
        },
        error: (_) =>
          this.toastrService.show(`Delete Label '${name}' for Predefined Value '${predefinedValue.value}' failed`, 'Error', {
            duration: 5000,
            status: 'danger'
          })
      });
  }
}
