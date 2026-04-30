import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { Subject } from 'rxjs';
import { Dataset, DatasetRun } from '../models';
import { DatePipe } from '@angular/common';

interface SampleCreateFromRunForm {
  name: FormControl<string>;
  description: FormControl<string>;
}

@Component({
  selector: 'tock-sample-create-from-run',
  templateUrl: './sample-create-from-run.component.html',
  styleUrl: './sample-create-from-run.component.scss'
})
export class SampleCreateFromRunComponent implements OnInit, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  isSubmitted: boolean = false;
  isLoading: boolean = false;

  // Injected by the dialog caller
  dataset: Dataset;
  run: DatasetRun;

  form = new FormGroup<SampleCreateFromRunForm>({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(3), Validators.maxLength(100)]
    }),
    description: new FormControl('', {
      nonNullable: true,
      validators: [Validators.maxLength(750)]
    })
  });

  constructor(public dialogRef: NbDialogRef<SampleCreateFromRunComponent>, private datePipe: DatePipe) {}

  ngOnInit(): void {
    this.form.patchValue({
      name: this.dataset.name,
      description: this._buildDefaultDescription()
    });
  }

  private _buildDefaultDescription(): string {
    const dateTimeFormat: string = 'y/MM/dd HH:mm';

    const date = this.datePipe.transform(this.run.startTime, dateTimeFormat);
    const total = this.run.stats?.totalQuestions ?? 0;
    return `Dataset run from ${date} — ${total} question${total > 1 ? 's' : ''}`;
  }

  getFormControl(name: keyof SampleCreateFromRunForm): FormControl {
    return this.form.get(name) as FormControl;
  }

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  submit(): void {
    this.isSubmitted = true;

    if (!this.form.valid) return;

    this.isLoading = true;

    // TODO: wire up to the backend endpoint when available
    const payload = {
      name: this.form.controls.name.value.trim(),
      description: this.form.controls.description.value.trim(),
      datasetId: this.dataset.id,
      runId: this.run.id
    };

    console.log('Create sample from run payload (TODO: connect to endpoint)', payload);

    // Temporary: close immediately until the endpoint is ready
    this.dialogRef.close(payload);
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
