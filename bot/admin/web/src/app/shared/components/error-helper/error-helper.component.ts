import { Component, Input } from '@angular/core';
import { UntypedFormArray, UntypedFormControl, UntypedFormGroup } from '@angular/forms';

@Component({
  selector: 'tock-error-helper',
  templateUrl: './error-helper.component.html',
  styleUrls: ['./error-helper.component.scss']
})
export class ErrorHelperComponent {
  @Input() field!: UntypedFormArray | UntypedFormControl | UntypedFormGroup;
}
