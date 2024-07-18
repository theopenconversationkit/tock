import { Component, ElementRef, Input, ViewChild } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { ProvidersConfigurationParam } from '../models/providers-configuration';

@Component({
  selector: 'tock-vector-db-provider-config-param-input',
  templateUrl: './vector-db-provider-config-param-input.component.html',
  styleUrls: ['./vector-db-provider-config-param-input.component.scss']
})
export class VectorDbProviderConfigParamInputComponent {
  @Input() configurationParam: ProvidersConfigurationParam;
  @Input() parentGroup: string;
  @Input() form: FormGroup;
  @Input() isSubmitted: boolean;
  @Input() defaultPrompt: string = '';

  @ViewChild('clearInput') clearInput: ElementRef;

  inputVisible: boolean = false;

  getFormControl(): FormControl {
    return this.form.get(this.parentGroup).get(this.configurationParam.key) as FormControl;
  }

  restoreDefaultPrompt(): void {
    this.form.get(this.parentGroup).get('prompt').setValue(this.defaultPrompt);
    this.form.get(this.parentGroup).get('prompt').markAsDirty();
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
