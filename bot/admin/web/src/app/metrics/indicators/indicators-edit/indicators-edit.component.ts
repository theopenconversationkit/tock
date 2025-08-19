/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  QueryList,
  SimpleChanges,
  ViewChild,
  ViewChildren
} from '@angular/core';
import { AbstractControl, FormArray, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { NbTagComponent, NbTagInputAddEvent } from '@nebular/theme';
import { Observable, of } from 'rxjs';
import { DialogService } from '../../../core-nlp/dialog.service';
import { normalizedCamelCase } from '../../../shared/utils';
import { IndicatorDefinition, IndicatorValueDefinition } from '../../models';
import { IndicatorEdition } from '../indicators.component';
import { ChoiceDialogComponent } from '../../../shared/components';

interface IndicatorValueEditForm {
  name: FormControl<string>;
  label: FormControl<string>;
}
interface IndicatorEditForm {
  label: FormControl<string>;
  description: FormControl<string>;
  dimensions: FormArray<FormControl<string>>;
  values: FormArray<FormGroup<IndicatorValueEditForm>>;
}

@Component({
  selector: 'tock-indicators-edit',
  templateUrl: './indicators-edit.component.html',
  styleUrls: ['./indicators-edit.component.scss']
})
export class IndicatorsEditComponent implements OnChanges {
  @Input() loading: boolean;
  @Input() indicatorEdition: IndicatorEdition;
  @Input() dimensionsCache: string[];
  @Input() indicators: IndicatorDefinition[];

  @Output() onClose = new EventEmitter<boolean>();
  @Output() onSave = new EventEmitter<IndicatorEdition>();

  @ViewChild('dimensionInput') dimensionInput: ElementRef;
  @ViewChildren('labelInput') valuesLabelInputs: QueryList<ElementRef>;

  dimensionsAutocompleteValues: Observable<any[]>;

  constructor(private dialogService: DialogService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.indicatorEdition?.currentValue) {
      const indicator: IndicatorDefinition = changes.indicatorEdition.currentValue.indicator;
      this.form.reset();
      this.values.clear();
      this.dimensions.clear();

      this.isSubmitted = false;

      this.form.patchValue(indicator);

      if (indicator.dimensions?.length) {
        indicator.dimensions.forEach((dimension) => {
          this.dimensions.push(new FormControl(dimension));
        });
      }

      if (indicator.values?.length) {
        indicator.values.forEach((value) => {
          this.values.push(
            new FormGroup({
              name: new FormControl(value.name),
              label: this.getValueLabelFormControl(value.label)
            })
          );
        });
      } else {
        this.values.push(
          new FormGroup({
            name: new FormControl(),
            label: this.getValueLabelFormControl('')
          })
        );
      }
    }

    this.dimensionsAutocompleteValues = of(this.dimensionsCache);
  }

  getValueLabelFormControl(initVal: string): FormControl {
    return new FormControl(initVal, [
      Validators.required,
      Validators.minLength(2),
      this.customPatternValid({ pattern: /[A-Za-z]+/, msg: 'Value label must contain at least one letter' })
    ]);
  }

  customPatternValid(config: any): ValidatorFn {
    return (control: FormControl) => {
      let urlRegEx: RegExp = config.pattern;
      if (control.value && !control.value.match(urlRegEx)) {
        // TODO : le message d'erreur ne s'affichera qu'apres le merge avec scenarios. A checker apres le merge
        return {
          custom: config.msg
        };
      } else {
        return null;
      }
    };
  }

  isSubmitted: boolean = false;

  descriptionMaxLength: number = 500;

  form = new FormGroup<IndicatorEditForm>({
    label: new FormControl('', [
      Validators.required,
      Validators.maxLength(50),
      this.isIndicatorLabelUnic(),
      this.customPatternValid({ pattern: /[A-Za-z]+/, msg: 'Label must contain at least one letter' })
    ]),
    description: new FormControl('', Validators.maxLength(this.descriptionMaxLength)),
    dimensions: new FormArray([], [Validators.required]),
    values: new FormArray([], [Validators.required])
  });

  get label(): FormControl {
    return this.form.get('label') as FormControl;
  }

  get description(): FormControl {
    return this.form.get('description') as FormControl;
  }

  get dimensions(): FormArray {
    return this.form.get('dimensions') as FormArray;
  }

  get values(): FormArray {
    return this.form.get('values') as FormArray;
  }

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  private isIndicatorLabelUnic(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value || !this.indicators) return null;

      const indicatorAsSameLabel = this.indicators.find(
        (indicator) =>
          indicator != this.indicatorEdition.indicator && indicator.label.trim().toLowerCase() === control.value.trim().toLowerCase()
      );

      // TODO : le message d'erreur ne s'affichera qu'apres le merge avec scenarios. A checker apres le merge
      return indicatorAsSameLabel ? { custom: 'There is already an indicator with the same label' } : null;
    };
  }

  getDescMaxLengthIndicatorClass(): string {
    return this.description.value.length > this.descriptionMaxLength ? 'text-danger' : 'text-muted';
  }

  addValueDefinition(): void {
    this.values.push(
      new FormGroup({
        name: new FormControl(undefined),
        label: new FormControl('', [Validators.required])
      })
    );
    setTimeout(() => {
      this.valuesLabelInputs.last.nativeElement.focus();
    });
  }

  removeValueDefinition(index: number): void {
    this.values.removeAt(index);
    this.form.markAsDirty();
  }

  save(): void {
    this.isSubmitted = true;
    if (this.canSave) {
      let form = this.form.value;

      form.values.forEach((val) => {
        if (!val.name) val.name = normalizedCamelCase(val.label);
      });

      this.onSave.emit({
        existing: this.indicatorEdition.existing,
        indicator: {
          type: this.indicatorEdition.indicator.type,
          name: this.indicatorEdition.indicator.name,
          label: form.label,
          description: form.description,
          dimensions: form.dimensions,
          values: form.values as IndicatorValueDefinition[]
        }
      });
    }
  }

  close(): Observable<any> {
    const action = 'yes';
    if (this.form.dirty) {
      const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
        context: {
          title: `Cancel ${this.indicatorEdition.existing ? 'edit' : 'create'} indicator`,
          subtitle: 'Are you sure you want to cancel ? Changes will not be saved.',
          actions: [
            { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
            { actionName: action, buttonStatus: 'danger' }
          ]
        }
      });
      dialogRef.onClose.subscribe((result) => {
        if (result === action) {
          this.onClose.emit(true);
        }
      });
      return dialogRef.onClose;
    } else {
      this.onClose.emit(true);
      return of(action);
    }
  }

  updateDimensionsAutocompleteValues(event: any): void {
    this.dimensionsAutocompleteValues = of(
      this.dimensionsCache.filter((tag) => tag.toLowerCase().includes(event.target.value.toLowerCase()))
    );
  }

  dimensionSelected(value: string): void {
    this.onDimensionAdd({ value, input: this.dimensionInput });
  }

  onDimensionAdd({ value, input }: NbTagInputAddEvent): void {
    let deduplicatedSpaces = value.replace(/\s\s+/g, ' ').toLowerCase().trim();
    if (deduplicatedSpaces && !this.dimensions.value.find((v: string) => v.toUpperCase() === deduplicatedSpaces.toUpperCase())) {
      this.dimensions.push(new FormControl(deduplicatedSpaces));
      this.form.markAsDirty();
      this.form.markAsTouched();
    }

    input.nativeElement.value = '';
  }

  onDimensionRemove(dimension: NbTagComponent): void {
    const dimensionToRemove = this.dimensions.value.findIndex((t: string) => t === dimension.text);

    if (dimensionToRemove !== -1) {
      this.dimensions.removeAt(dimensionToRemove);
      this.form.markAsDirty();
      this.form.markAsTouched();
    }
  }
}
