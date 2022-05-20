import { Component, EventEmitter, Inject, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { StateService } from 'src/app/core-nlp/state.service';
import { scenarioItem } from '../../models';

@Component({
  selector: 'scenario-api-edit',
  templateUrl: './api-edit.component.html',
  styleUrls: ['./api-edit.component.scss']
})
export class ApiEditComponent implements OnInit {
  @Input() item: scenarioItem;
  @Output() saveModifications = new EventEmitter();
  itemCopy: scenarioItem;
  constructor(public dialogRef: NbDialogRef<ApiEditComponent>, protected state: StateService) {}

  ngOnInit(): void {
    this.itemCopy = JSON.parse(JSON.stringify(this.item));
  }

  form: FormGroup = new FormGroup({
    name: new FormControl(undefined, Validators.required),
    description: new FormControl(),
    responseType: new FormControl(undefined, Validators.required)
  });

  get canSave(): boolean {
    return this.form.valid;
  }

  removeApiDefinition() {}

  save() {
    this.saveModifications.emit(this.itemCopy);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
