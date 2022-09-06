import { Component, Input } from '@angular/core';
import { FormArray, FormControl } from '@angular/forms';

@Component({
  selector: 'tock-form-control',
  templateUrl: './form-control.component.html',
  styleUrls: ['./form-control.component.scss']
})
export class FormControlComponent {
  @Input() controls!: FormControl | FormArray;
  @Input() label?: string;
  @Input() name!: string;
  @Input() showError: boolean = false;
  @Input() required: boolean = false;
}
