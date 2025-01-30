import { Component, Input, OnInit } from '@angular/core';
import { ActionReport, DialogReport } from '../../model/dialog-data';
import { NbDialogRef, NbToastrService } from '@nebular/theme';
import { Annotation, AnnotationReasons, AnnotationState, AnnotationStates } from './annotations';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { FormElementGroup, FormType, G } from 'ngx-mf';
import { RestService } from '../../../core-nlp/rest/rest.service';
import { StateService } from '../../../core-nlp/state.service';

type AnnotationForm = FormType<
  Omit<Annotation, '_id' | 'user' | 'createdAt' | 'lastUpdateDate' | 'expiresAt'>,
  { events: [FormElementGroup] }
>;
type AnnotationFormGroupKeysType = AnnotationForm[G];

@Component({
  selector: 'tock-annotation',
  templateUrl: './annotation.component.html',
  styleUrl: './annotation.component.scss'
})
export class AnnotationComponent implements OnInit {
  loading: boolean = true;

  annotationStates = AnnotationStates;

  annotationReasons = AnnotationReasons;

  @Input() dialogReport: DialogReport;
  @Input() actionReport: ActionReport;

  isSubmitted: boolean = false;

  constructor(
    private dialogRef: NbDialogRef<AnnotationComponent>,
    private rest: RestService,
    private state: StateService,
    private toastrService: NbToastrService
  ) {}

  ngOnInit(): void {
    this.loading = false;
    if (this.actionReport.annotation) {
      this.form.patchValue(this.actionReport.annotation);
    }
  }

  form = new FormGroup<AnnotationFormGroupKeysType>({
    state: new FormControl(AnnotationState.ANOMALY, [Validators.required]),
    reason: new FormControl(undefined),
    description: new FormControl(undefined, [Validators.required]),

    ground_truth: new FormControl(undefined),
    events: new FormArray([])
  });

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  submit() {
    this.isSubmitted = true;
    if (this.canSave && this.form.dirty) {
      const formValue: any = this.form.value;
      console.log(formValue);

      if (!this.actionReport.annotation._id) {
        this.postAnnotation(formValue);
      } else {
        this.putAnnotation(formValue);
      }
    }
  }

  postAnnotation(formValue) {
    const url = `/bots/${this.state.currentApplication.name}/dialogs/${this.dialogReport.id}/actions/${this.actionReport.id}/annotation`;
    this.rest.post(url, formValue, null, null, true).subscribe({
      next: (annotation: Annotation) => {
        console.log('POST RESULT');
        console.log(annotation);
      },
      error: (error) => {
        this.toastrService.danger('An error occured', 'Error', {
          duration: 5000,
          status: 'danger'
        });
      }
    });
  }

  putAnnotation(formValue) {
    const url = `/bots/${this.state.currentApplication.name}/dialogs/${this.dialogReport.id}/actions/${this.actionReport.id}/annotation/${this.actionReport.annotation._id}`;
    this.rest.put(url, formValue).subscribe({
      next: (annotation: Annotation) => {
        console.log('PUT RESULT');
        console.log(annotation);
      },
      error: (error) => {
        this.toastrService.danger('An error occured', 'Error', {
          duration: 5000,
          status: 'danger'
        });
      }
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
