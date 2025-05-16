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

import { Component, ElementRef, Input, ViewChild } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { PromptDefinitionFormatter, ProvidersConfigurationParam } from '../../model/ai-settings';

@Component({
  selector: 'tock-ai-settings-engine-config-param-input',
  templateUrl: './ai-settings-engine-config-param-input.component.html',
  styleUrls: ['./ai-settings-engine-config-param-input.component.scss']
})
export class AiSettingsEngineConfigParamInputComponent {
  @Input() configurationParam: ProvidersConfigurationParam;
  @Input() parentGroup: string;
  @Input() form: FormGroup;
  @Input() isSubmitted: boolean;
  @Input() defaultPrompt: string = '';

  @ViewChild('clearInput') clearInput: ElementRef;

  inputVisible: boolean = false;

  get isRequired(): boolean {
    return this.configurationParam.required || typeof this.configurationParam.required === 'undefined';
  }

  getFormControl(): FormControl {
    return this.form.get(this.parentGroup).get(this.configurationParam.key) as FormControl;
  }

  getFormControlLabel() {
    let label = this.configurationParam.label;
    if (this.configurationParam.type === 'prompt' && this.configurationParam.key === 'template') {
      const formatter = this.form.get(this.parentGroup).get('formatter').value;

      if (formatter === PromptDefinitionFormatter.jinja2) {
        label += ' (Jinja2 format)';
      }
      if (formatter === PromptDefinitionFormatter.fstring) {
        label += ' (f-string format | deprecated)';
      }
    }

    return label;
  }

  restoreDefaultPrompt(): void {
    this.form.get(this.parentGroup).get('formatter').setValue(PromptDefinitionFormatter.jinja2);
    this.form.get(this.parentGroup).get('template').setValue(this.defaultPrompt);
    this.form.get(this.parentGroup).get('template').markAsDirty();
  }

  showInput(event: FocusEvent): void {
    const target = event.target as HTMLInputElement;

    setTimeout(() => {
      const selectionStart = target.selectionStart;
      this.inputVisible = true;
      setTimeout(() => {
        const clearInputElem = this.clearInput.nativeElement;
        clearInputElem.focus();
        if (Number.isInteger(selectionStart)) clearInputElem.setSelectionRange(selectionStart, selectionStart);
      });
    });
  }

  hideInput(): void {
    this.inputVisible = false;
  }

  getControlObfuscatedValue(): string {
    let str = this.getFormControl().value;
    if (!str) return '';

    let url: URL;
    let strLen: number;
    let protocol: string;

    try {
      url = new URL(str);
    } catch (_) {}

    if (url) {
      protocol = url.protocol;
      str = str.replace(protocol, '');
      strLen = str.length;
    } else {
      strLen = str.length;
    }

    const nbVisibleChars = Math.min(3, Math.floor(strLen / 8.1));
    if (nbVisibleChars) {
      const nbHiddens = strLen - 2 * nbVisibleChars;
      const obfuscation = [str.substring(0, nbVisibleChars), '*'.repeat(nbHiddens), str.slice(nbVisibleChars * -1)].join('');

      return [protocol, obfuscation].join('');
    }

    return '*'.repeat(strLen);
  }
}
