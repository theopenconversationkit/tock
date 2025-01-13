import { Component, Input } from '@angular/core';
import { UntypedFormArray, UntypedFormControl, UntypedFormGroup } from '@angular/forms';

@Component({
  selector: 'tock-form-control',
  templateUrl: './form-control.component.html',
  styleUrls: ['./form-control.component.scss']
})
export class FormControlComponent {
  @Input() controls!: UntypedFormControl | UntypedFormArray | UntypedFormGroup;
  @Input() label?: string;
  @Input() boldLabel?: boolean = true;
  @Input() name!: string;
  @Input() showError: boolean = false;
  @Input() required: boolean = false;
  @Input() hasMargin: boolean = true;
  @Input() noLabelMargin: boolean = false;
  @Input() information?: string;
}
