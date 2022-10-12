import { Component, Input } from '@angular/core';
import { FormArray, FormControl, FormGroup } from '@angular/forms';

@Component({
  selector: 'tock-error-helper',
  templateUrl: './error-helper.component.html',
  styleUrls: ['./error-helper.component.scss']
})
export class ErrorHelperComponent {
  @Input()
  field!: FormArray | FormControl | FormGroup;
}
