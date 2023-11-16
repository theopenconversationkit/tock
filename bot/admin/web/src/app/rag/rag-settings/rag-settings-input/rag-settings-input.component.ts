import { Component, Input, OnInit } from '@angular/core';
import { DefaultPrompt, EnginesConfigurationParam } from '../models/engines-configurations';
import { FormControl, FormGroup } from '@angular/forms';

@Component({
  selector: 'tock-rag-settings-input',
  templateUrl: './rag-settings-input.component.html',
  styleUrls: ['./rag-settings-input.component.scss']
})
export class RagSettingsInputComponent implements OnInit {
  @Input() configurationParam: EnginesConfigurationParam;
  @Input() parentGroup: string;
  @Input() isSubmitted: boolean;
  @Input() form: FormGroup;

  constructor() {}

  ngOnInit(): void {}

  getFormControl(): FormControl {
    return this.form.get(this.parentGroup).get(this.configurationParam.key) as FormControl;
  }

  restoreDefaultPrompt(): void {
    this.form.get(this.parentGroup).get('prompt').setValue(DefaultPrompt);
    this.form.get(this.parentGroup).get('prompt').markAsDirty();
  }
}
