import { Component, EventEmitter, Inject, Input, OnInit, Output } from '@angular/core';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { StateService } from 'src/app/core-nlp/state.service';
import {
  scenarioItem,
  SCENARIO_ITEM_API_RESPONSETYPE_BOOLEAN,
  SCENARIO_ITEM_API_RESPONSETYPE_CODES,
  SCENARIO_ITEM_API_RESPONSETYPE_STRING
} from '../../models';

@Component({
  selector: 'scenario-api-edit',
  templateUrl: './api-edit.component.html',
  styleUrls: ['./api-edit.component.scss']
})
export class ApiEditComponent implements OnInit {
  readonly SCENARIO_ITEM_API_RESPONSETYPE_STRING = SCENARIO_ITEM_API_RESPONSETYPE_STRING;
  readonly SCENARIO_ITEM_API_RESPONSETYPE_BOOLEAN = SCENARIO_ITEM_API_RESPONSETYPE_BOOLEAN;
  readonly SCENARIO_ITEM_API_RESPONSETYPE_CODES = SCENARIO_ITEM_API_RESPONSETYPE_CODES;

  @Input() item: scenarioItem;
  @Output() saveModifications = new EventEmitter();
  @Output() deleteDefinition = new EventEmitter();
  constructor(public dialogRef: NbDialogRef<ApiEditComponent>, protected state: StateService) {}

  ngOnInit(): void {
    this.form.patchValue(this.item.apiCallDefinition);
    this.item.apiCallDefinition?.responseCodes?.forEach((code) =>
      this.responseCodes.push(new FormControl(code))
    );
  }

  form: FormGroup = new FormGroup({
    name: new FormControl(undefined, Validators.required),
    description: new FormControl(),
    responseType: new FormControl(undefined, Validators.required),
    responseCodes: new FormArray([])
  });

  get responseCodes(): FormArray {
    return this.form.get('responseCodes') as FormArray;
  }

  get canSave(): boolean {
    return this.form.valid;
  }

  private formatResponseCode(code: string): string {
    return code
      .trim()
      .normalize('NFD')
      .replace(/[^\s0-9A-Za-z_-]*/g, '')
      .replace(/\s+/g, '_')
      .toUpperCase();
  }

  addResponseCode($event) {
    this.responseCodes.push(new FormControl(this.formatResponseCode($event.target.value)));
    $event.target.value = '';
  }

  removeResponseCode(code) {
    let toRemove = this.responseCodes.controls.findIndex((c) => c.value == code);
    this.responseCodes.removeAt(toRemove);
  }

  removeApiDefinition() {
    this.deleteDefinition.emit();
  }

  save() {
    this.saveModifications.emit(this.form.value);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
