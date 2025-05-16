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

import { AfterViewInit, Component, forwardRef, Input, OnInit } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'tock-slider',
  templateUrl: './slider.component.html',
  styleUrls: ['./slider.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      multi: true,
      useExisting: forwardRef(() => SliderComponent)
    }
  ]
})
export class SliderComponent implements AfterViewInit, ControlValueAccessor {
  @Input() min: number;
  @Input() max: number;
  @Input() step: number;

  value: number;
  disabled: boolean = false;
  touched: boolean = false;

  ngAfterViewInit() {
    if (this.value === null) {
      setTimeout(() => {
        this.value = this.min || 0;
        this.onChange(this.value);
      });
    }
  }

  onChange = (val: number) => {};

  registerOnChange(onChange: any) {
    this.onChange = onChange;
  }

  onTouched: Function = () => {};

  registerOnTouched(onTouched: any) {
    this.onTouched = onTouched;
  }

  writeValue(value: number) {
    this.value = value;
  }

  changeValue(event) {
    this.markAsTouched();
    this.onChange(event.target.value);
  }

  markAsTouched() {
    if (!this.touched) {
      this.onTouched();
      this.touched = true;
    }
  }

  setDisabledState(disabled: boolean) {
    this.disabled = disabled;
  }
}
