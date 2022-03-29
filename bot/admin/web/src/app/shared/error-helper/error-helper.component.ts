import { Component, Input, OnInit } from '@angular/core';
import { FormArray, FormControl, FormGroup } from '@angular/forms';

@Component({
  selector: 'tock-error-helper',
  templateUrl: './error-helper.component.html',
  styleUrls: ['./error-helper.component.css']
})
export class ErrorHelperComponent {
  @Input()
  field!: FormArray | FormControl | FormGroup;
}
