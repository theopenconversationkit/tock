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
import { TranslocoService } from '@jsverse/transloco';

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
    private applicationService: ApplicationService,
    private transloco: TranslocoService
  ) {}

  ngOnInit(): void {
    this.uploader = new FileUploader({ url: undefined, autoUpload: true, removeAfterUpload: true });
    this.uploader.onCompleteItem = (item: FileItem, response: string, status: number, headers: ParsedResponseHeaders) => {
      const d = Dictionary.fromJSON(JSON.parse(response));
      this.selectedDictionary = d;
      this.selectedEntityType.dictionary = d.values.length !== 0;
      this.nlp.updateEntityType(this.selectedEntityType).subscribe((s) => {
        if (s) this.refreshEntityType(this.selectedEntityType);
        this.toastrService.show(
          this.transloco.translate('lu.entities.dictionary_imported'),
          this.transloco.translate('lu.entities.dictionary_title'),
          {
            duration: 2000,
            status: 'success'
          }
        );
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

    this.toastrService.show(
      this.transloco.translate('lu.entities.dictionary_exported'),
      this.transloco.translate('lu.entities.dictionary_title'),
      {
        duration: 2000,
        status: 'success'
      }
    );
  }

  update(entity: EntityDefinition) {
    this.nlp
      .updateEntityDefinition(this.state.createUpdateEntityDefinitionQuery(entity))
      .pipe(map((_) => this.applicationService.reloadCurrentApplication()))
      .subscribe((_) =>
        this.toastrService.show(
          this.transloco.translate('lu.entities.entity_updated'),
          this.transloco.translate('lu.entities.update_title'),
          { duration: 2000, status: 'success' }
        )
      );
  }

  deleteEntityType(entityType: EntityType) {
    const action = this.transloco.translate('common.actions.remove');
    let dialogRef = this.dialog.openDialog(ChoiceDialogComponent, {
      context: {
        title: this.transloco.translate('lu.entities.remove_entity_type_title', { entityType: entityType.name }),
        subtitle: this.transloco.translate('lu.entities.remove_entity_type_confirmation'),
        actions: [
          { actionName: this.transloco.translate('common.actions.cancel'), buttonStatus: 'basic', ghost: true },
          { actionName: action, buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result.toLowerCase() === action.toLowerCase()) {
        this.nlp.removeEntityType(entityType).subscribe(
          (_) => {
            this.state.resetConfiguration();
            this.toastrService.show(
              this.transloco.translate('lu.entities.entity_type_removed', { entityType: entityType.name }),
              this.transloco.translate('lu.entities.remove_entity_type_success_title'),
              {
                duration: 2000,
                status: 'success'
              }
            );
          },
          (_) =>
            this.toastrService.show(
              this.transloco.translate('lu.entities.delete_entity_type_failed', { entityType: entityType.name }),
              this.transloco.translate('lu.entities.error_title'),
              {
                duration: 5000,
                status: 'danger'
              }
            )
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
      this.toastrService.show(
        this.transloco.translate('lu.entities.configuration_updated'),
        this.transloco.translate('lu.entities.update_title'),
        {
          duration: 5000,
          status: 'success'
        }
      )
    );
  }

  updateDictionary(): void {
    this.nlp.saveDictionary(this.selectedDictionary).subscribe((_) =>
      this.toastrService.show(
        this.transloco.translate('lu.entities.configuration_updated'),
        this.transloco.translate('lu.entities.update_title'),
        {
          duration: 5000,
          status: 'success'
        }
      )
    );
  }

  updatePredefinedValueName(predefinedValue: PredefinedValue, input): void {
    const newValue = input.value;
    const oldValue = predefinedValue.value;
    if (oldValue !== newValue) {
      if (newValue.trim() === '') {
        this.toastrService.show(
          this.transloco.translate('lu.entities.empty_predefined_value_error'),
          this.transloco.translate('lu.entities.error_title'),
          {
            duration: 5000,
            status: 'warning'
          }
        );
        input.value = oldValue;
        input.focus();
      } else {
        if (this.selectedDictionary.values.some((v) => v.value === newValue)) {
          this.toastrService.show(
            this.transloco.translate('lu.entities.predefined_value_exists_error'),
            this.transloco.translate('lu.entities.error_title'),
            {
              duration: 5000,
              status: 'warning'
            }
          );
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
                this.toastrService.show(
                  this.transloco.translate('lu.entities.update_predefined_value_failed', { name: newValue }),
                  this.transloco.translate('lu.entities.error_title'),
                  {
                    duration: 5000,
                    status: 'danger'
                  }
                );
              }
            );
        }
      }
    }
  }

  createPredefinedValue(name: string): void {
    if (name.trim() === '') {
      this.toastrService.show(
        this.transloco.translate('lu.entities.empty_predefined_value_error'),
        this.transloco.translate('lu.entities.error_title'),
        {
          duration: 5000,
          status: 'danger'
        }
      );
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
          this.toastrService.show(
            this.transloco.translate('lu.entities.create_predefined_value_failed', { name: name }),
            this.transloco.translate('lu.entities.error_title'),
            {
              duration: 5000,
              status: 'danger'
            }
          )
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
        this.toastrService.show(
          this.transloco.translate('lu.entities.delete_predefined_value_failed', { name: name }),
          this.transloco.translate('lu.entities.error_title'),
          {
            duration: 5000,
            status: 'danger'
          }
        )
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
          this.toastrService.show(
            this.transloco.translate('lu.entities.create_label_failed', { name: name, value: predefinedValue.value }),
            this.transloco.translate('lu.entities.error_title'),
            {
              duration: 5000,
              status: 'danger'
            }
          )
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
          this.toastrService.show(
            this.transloco.translate('lu.entities.delete_label_failed', { name: name, value: predefinedValue.value }),
            this.transloco.translate('lu.entities.error_title'),
            {
              duration: 5000,
              status: 'danger'
            }
          )
      });
  }
}
