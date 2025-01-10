import { Component, EventEmitter, Input, OnDestroy, OnInit, Optional, Output } from '@angular/core';
import { FormArray, FormControl, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { Observable, Subject, take, takeUntil } from 'rxjs';
import { StateService } from '../../../core-nlp/state.service';
import { NbDialogRef, NbToastrService } from '@nebular/theme';
import { deepCopy, getExportFileName, getPropertyByNameSpace, isObject } from '../../../shared/utils';
import { saveAs } from 'file-saver-es';
import Papa from 'papaparse';

enum Formats {
  json = 'json',
  csv = 'csv'
}

enum Delimiters {
  Comma = ',',
  Semi = ';',
  Pipe = '|',
  Tab = '\t',
  Space = ' '
}

enum ListDelimiters {
  Comma = ',',
  Semi = ';',
  Newline = '\n'
}

interface ExportForm {
  format: FormControl<Formats>;
  delimiter: FormControl<Delimiters>;
  listDelimiter: FormControl<ListDelimiters>;
  columns: FormArray<FormGroup<ExportFormColumnsGroup>>;
}

interface ExportFormColumnsGroup {
  name: FormControl<string>;
  selected: FormControl<boolean>;
  deepeningPathKey?: FormControl<string>;
  deepeningPathDirimantValue?: FormControl<string>;
}

type ExtractFormControlType<T> = {
  [K in keyof T]: T[K] extends FormControl<infer U>
    ? U
    : T[K] extends FormArray<FormControl<infer U>>
    ? Array<U>
    : T[K] extends FormArray<FormGroup<infer U>>
    ? Array<Partial<ExtractFormControlType<U>>>
    : T[K] extends FormGroup<infer U>
    ? Partial<ExtractFormControlType<U>>
    : T[K];
};

type GenericObject = { [key: string]: any };

interface DeepeningPropStrategy {
  [key: string]: {
    path: string;
    dirimantKey: string;
    possibleDirimantKeyValues?: string[];
    valueKey: string;
  };
}

@Component({
  selector: 'tock-data-export',
  templateUrl: './data-export.component.html',
  styleUrl: './data-export.component.scss'
})
export class DataExportComponent implements OnInit, OnDestroy {
  destroy$: Subject<unknown> = new Subject();
  loading: boolean = false;
  isSubmitted: boolean = false;

  formats = Formats;
  delimiters = Delimiters;
  listDelimiters = ListDelimiters;

  columnNames: string[];

  @Input() data!: GenericObject[];
  @Input() exportFileNameType!: string;
  @Input() searchQuery?: Observable<any>;

  @Input() deepeningPropertiesStrategies: DeepeningPropStrategy;

  @Output() onClose = new EventEmitter<boolean>();

  constructor(
    private stateService: StateService,
    private toastrService: NbToastrService,
    @Optional() public dialogRef: NbDialogRef<DataExportComponent>
  ) {}

  ngOnInit(): void {
    const referenceLine = this.data[0];
    this.columnNames = Object.keys(referenceLine);

    this.columnNames.forEach((key) => {
      if (this.deepeningPropertiesStrategies?.hasOwnProperty(key)) {
        const deepeningStrategy = this.deepeningPropertiesStrategies[key];

        let dirimants;
        if (deepeningStrategy.possibleDirimantKeyValues?.length) {
          dirimants = deepeningStrategy.possibleDirimantKeyValues;
        } else {
          const targetedObj = referenceLine[key];
          const targetedProp = getPropertyByNameSpace(deepeningStrategy.path, targetedObj);
          dirimants = targetedProp.map((p) => p[deepeningStrategy.dirimantKey]);
        }

        dirimants.forEach((dirimant) => {
          const name = [key, deepeningStrategy.path, dirimant].join('.');
          this.form.controls['columns'].push(
            new FormGroup({
              name: new FormControl(name),
              selected: new FormControl(false),
              deepeningPathKey: new FormControl(key),
              deepeningPathDirimantValue: new FormControl(dirimant)
            }) as FormGroup<ExportFormColumnsGroup>
          );
        });
      } else {
        this.form.controls['columns'].push(
          new FormGroup({
            name: new FormControl(key),
            selected: new FormControl(false)
          })
        );
      }
    });

    this.format.valueChanges.pipe(takeUntil(this.destroy$)).subscribe((format: Formats) => {
      if (format === Formats.csv) {
        this.columns.setValidators([Validators.required, this.validateColumnsFormArray]);
      } else {
        this.columns.clearValidators();
      }

      this.columns.updateValueAndValidity();
    });

    this.columns.valueChanges.pipe(takeUntil(this.destroy$)).subscribe((next: ExtractFormControlType<ExportFormColumnsGroup>[]) => {
      this.updateColumnsIndexes(next);
    });
  }

  form = new FormGroup<ExportForm>({
    format: new FormControl(Formats.json, [Validators.required]),
    delimiter: new FormControl(Delimiters.Comma, [Validators.required]),
    listDelimiter: new FormControl(ListDelimiters.Newline, [Validators.required]),
    columns: new FormArray([])
  });

  get format(): FormControl {
    return this.form.get('format') as FormControl;
  }

  get columns(): FormArray<FormGroup<ExportFormColumnsGroup>> {
    return this.form.get('columns') as FormArray<FormGroup<ExportFormColumnsGroup>>;
  }

  get delimiter(): FormControl {
    return this.form.get('delimiter') as FormControl;
  }

  get listDelimiter(): FormControl {
    return this.form.get('listDelimiter') as FormControl;
  }

  get canSave(): boolean {
    return this.form.valid;
  }

  validateColumnsFormArray(columns: FormArray): ValidationErrors | null {
    if (!columns.value.some((col) => col.selected)) {
      return { custom: 'Please select at least one column' };
    }

    return null;
  }

  getFirstFreeSlotIndex(stack, currIndex: number): number | null {
    for (let i = 0; i < stack.length; i++) {
      if (i !== currIndex && !stack[i].selected) return i;
    }
    return null;
  }

  updateColumnsIndexes(stack: ExtractFormControlType<ExportFormColumnsGroup>[]): void {
    for (let i = stack.length - 1; i > 0; i--) {
      if (stack[i].selected && i > 0) {
        const freeIndex = this.getFirstFreeSlotIndex(stack, i);

        if (freeIndex !== null && freeIndex < i) {
          const col = this.columns.at(i);
          this.columns.removeAt(i);
          this.columns.insert(freeIndex, col);
        }
      }
    }
  }

  canIncreaseColumnIndex(index: number): boolean {
    if (index >= this.columnNames.length - 1) return false;

    const freeIndex = this.getFirstFreeSlotIndex(this.columns.value, index);
    if (freeIndex === null) return true;
    return freeIndex > index + 1;
  }

  changeColumnIndex(shift, index): void {
    const col = this.columns.at(index);
    this.columns.removeAt(index);
    this.columns.insert(index + shift, col);
  }

  trackByFn(index: number, item: any): any {
    return item;
  }

  export(): void {
    this.isSubmitted = true;
    if (this.canSave) {
      this.loading = true;
      if (this.searchQuery) {
        this.searchQuery.pipe(take(1)).subscribe((result) => {
          this.download(Array.isArray(result) ? result : result.rows);
        });
      } else {
        this.download(deepCopy(this.data));
      }
    }
  }

  download(data: GenericObject[]): void {
    const exportFileName = getExportFileName(
      this.stateService.currentApplication.namespace,
      this.stateService.currentApplication.name,
      this.exportFileNameType,
      this.format.value
    );

    let blob;

    if (this.format.value === Formats.json) {
      blob = new Blob([JSON.stringify(data)], {
        type: 'application/json'
      });
    }

    if (this.format.value === Formats.csv) {
      const selectedColumns = this.columns.value.filter((col) => col.selected);
      const columns = selectedColumns.map((col) => col.name);

      selectedColumns.forEach((selectedColumn) => {
        // we apply deepeningStrategy serialization if any
        if (selectedColumn.deepeningPathKey) {
          const deepeningStrategy = this.deepeningPropertiesStrategies[selectedColumn.deepeningPathKey];

          data = data.map((line) => {
            const targetedObj = line[selectedColumn.deepeningPathKey];
            const targetedProp = getPropertyByNameSpace(deepeningStrategy.path, targetedObj);

            const targetedPropEntry = targetedProp.find((entry) => {
              return entry[deepeningStrategy.dirimantKey] === selectedColumn.deepeningPathDirimantValue;
            });

            if (targetedPropEntry) {
              const targetedPropEntryValue = targetedPropEntry[deepeningStrategy.valueKey];
              if (targetedPropEntryValue) {
                line[selectedColumn.name] = targetedPropEntryValue;
              }
            }

            return line;
          });
        }

        // We serialize any remaining object or array of objects as JSON
        data = data.map((line) => {
          if (isObject(line[selectedColumn.name])) {
            line[selectedColumn.name] = JSON.stringify(line[selectedColumn.name]);
          } else if (Array.isArray(line[selectedColumn.name])) {
            line[selectedColumn.name] = line[selectedColumn.name].map((entry) => {
              if (isObject(entry)) entry = JSON.stringify(entry);
              return entry;
            });
          }
          return line;
        });
      });

      // If the required list delimiter is not a comma, we modify it.
      if (this.listDelimiter.value !== ListDelimiters.Comma) {
        data = data.map((line) => {
          Object.entries(line).forEach(([key, value]) => {
            if (Array.isArray(value)) {
              line[key] = value.join(this.listDelimiter.value);
            }
          });
          return line;
        });
      }

      const rawCsv = Papa.unparse(
        {
          fields: columns,
          data: data
        },
        { delimiter: this.delimiter.value }
      );

      blob = new Blob([rawCsv], {
        type: 'text/csv'
      });
    }

    saveAs(blob, exportFileName);

    this.toastrService.show(`${this.exportFileNameType} dump provided`, `${this.exportFileNameType} dump`, {
      duration: 3000,
      status: 'success'
    });

    this.loading = false;
  }

  close(): void {
    this.onClose.emit(true);
    if (this.dialogRef) this.dialogRef.close();
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
