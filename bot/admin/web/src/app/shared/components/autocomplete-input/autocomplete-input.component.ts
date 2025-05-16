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

import { Component, EventEmitter, forwardRef, Input, OnInit, Output } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { NbComponentSize } from '@nebular/theme';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';

@Component({
  selector: 'tock-autocomplete-input',
  templateUrl: './autocomplete-input.component.html',
  styleUrls: ['./autocomplete-input.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => AutocompleteInputComponent),
      multi: true
    }
  ]
})
export class AutocompleteInputComponent implements OnInit, ControlValueAccessor {
  @Input() activeFirst: boolean = true;
  @Input() fullWidth: boolean = true;
  @Input() keyupEnter?: Function;
  @Input() name: string = '';
  @Input() options: string[] = [];
  @Input() placeholder: string = 'Select value';
  @Input() fieldSize: NbComponentSize = 'medium';

  @Output() onKeyup = new EventEmitter<{ key: string; value: string }>();
  @Output() onSelectionChange = new EventEmitter<string>();

  public filteredOptions$!: Observable<string[]>;
  public disabled: boolean = false;

  private onChange: Function = () => {};
  private onTouch: Function = () => {};
  private _value?: string = undefined;

  get value(): string {
    return this._value;
  }

  ngOnInit(): void {
    this.filteredOptions$ = of(this.options);
  }

  private filter(value: string): string[] {
    const filterValue = value.toLowerCase();
    return this.options.filter((optionValue) => optionValue.toLowerCase().includes(filterValue));
  }

  private getFilteredOptions(value: string): Observable<string[]> {
    return of(value).pipe(map((filterString) => this.filter(filterString)));
  }

  valueChange(input: Event): void {
    const target: HTMLInputElement = input.target as HTMLInputElement;
    const value = target.value;

    this.filteredOptions$ = this.getFilteredOptions(value);
    this.writeValue(value);
  }

  valueSelectionChange(value: string): void {
    this.filteredOptions$ = this.getFilteredOptions(value);
    this.writeValue(value);
    this.onSelectionChange.emit(value);
  }

  keyUp(e: KeyboardEvent): void {
    const target: HTMLInputElement = e.target as HTMLInputElement;

    this.onKeyup.emit({ key: e.key, value: target.value });
  }

  writeValue(v: string): void {
    this._value = v;
    this.onChange(v);
    this.onTouch(v);
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouch = fn;
  }

  setDisabledState(disabled: boolean) {
    this.disabled = disabled;
  }
}
