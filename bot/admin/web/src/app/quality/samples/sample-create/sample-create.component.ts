import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef, NbToastrService } from '@nebular/theme';
import { Subject } from 'rxjs';
import { getFirstDayOfPreviousMonth, getLastDayOfPreviousMonth } from '../../../shared/utils';
import { Router } from '@angular/router';
import { getEvaluationBaseUrl } from '../utils';
import { StateService } from '../../../core-nlp/state.service';
import { RestService } from '../../../core-nlp/rest/rest.service';
import { DialogService } from '../../../core-nlp/dialog.service';
import { ChoiceDialogComponent } from '../../../shared/components';

interface DialogListFiltersForm {
  name: FormControl<string>;
  description: FormControl<string>;
  allowTestDialogs: FormControl<boolean>;
  dialogActivityFrom?: FormControl<Date | null>;
  dialogActivityTo?: FormControl<Date | null>;
  requestedDialogCount: FormControl<number>;
}

@Component({
  selector: 'tock-sample-create',
  templateUrl: './sample-create.component.html',
  styleUrl: './sample-create.component.scss'
})
export class SampleCreateComponent implements OnInit, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  isSubmitted: boolean = false;

  constructor(
    public dialogRef: NbDialogRef<SampleCreateComponent>,
    private router: Router,
    private stateService: StateService,
    private rest: RestService,
    private toastrService: NbToastrService,
    private dialogService: DialogService
  ) {}

  ngOnInit(): void {
    const today = new Date();
    const firstDayOfPreviousMonth = getFirstDayOfPreviousMonth(today);
    const lastDayOfPreviousMonth = getLastDayOfPreviousMonth(today);

    const previousMonthName = firstDayOfPreviousMonth.toLocaleString('en-US', { month: 'long' });
    const previousMonthYear = firstDayOfPreviousMonth.getFullYear();

    this.form.patchValue({
      name: `Evaluation sample ${previousMonthName} ${previousMonthYear}`,
      dialogActivityFrom: firstDayOfPreviousMonth,
      dialogActivityTo: lastDayOfPreviousMonth
    });
  }

  form = new FormGroup<DialogListFiltersForm>({
    name: new FormControl('', [Validators.required, Validators.minLength(5), Validators.maxLength(100)]),
    description: new FormControl('', [Validators.maxLength(750)]),
    allowTestDialogs: new FormControl(false),
    dialogActivityFrom: new FormControl<Date | null>(null),
    dialogActivityTo: new FormControl<Date | null>(null),
    requestedDialogCount: new FormControl(50, [Validators.required])
  });

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  nbDialogOptions = [
    { value: 25, label: '25' },
    { value: 50, label: '50' },
    { value: 100, label: '100' },
    { value: 150, label: '150' },
    { value: 200, label: '200' }
  ];

  getFormControl(formControlName: string): FormControl {
    return this.form.get(formControlName) as FormControl;
  }

  validateForm(): boolean {
    const dateFrom = this.form.get('dialogActivityFrom')?.value;
    const dateTo = this.form.get('dialogActivityTo')?.value;

    if (!dateFrom || !(dateFrom instanceof Date) || isNaN(dateFrom.getTime())) {
      this.form.get('dialogActivityFrom')?.setErrors({ custom: 'This field requires a valid date.' });
      return false;
    }

    if (!dateTo || !(dateTo instanceof Date) || isNaN(dateTo.getTime())) {
      this.form.get('dialogActivityTo')?.setErrors({ custom: 'This field requires a valid date.' });
      return false;
    }

    if (dateFrom && dateTo && dateFrom > dateTo) {
      this.form.get('dialogActivityFrom')?.setErrors({ custom: 'The start date must be earlier than the end date.' });
      return false;
    }

    return true;
  }

  createSample(): void {
    this.isSubmitted = true;
    if (this.validateForm() && this.canSave) {
      const url = getEvaluationBaseUrl(this.stateService.currentApplication.name);

      const payload = this.form.value;

      this.rest.post(url, payload, null, null, true).subscribe({
        next: (evaluation: any) => {
          this.router.navigate(['/quality/samples/detail', evaluation._id]);
          this.dialogRef.close(this.form.value);
        },
        error: (error) => {
          if (error?.error?.errors && Array.isArray(error.error.errors)) {
            const mssg = [];
            error.error.errors.forEach((e) => {
              if (e.message) {
                mssg.push(e.message);
              }
            });
            if (mssg.length) {
              const closeAction = 'Close';
              this.dialogService.openDialog(ChoiceDialogComponent, {
                context: {
                  title: `Unable to create sample`,
                  subtitle: mssg.join(' + '),
                  actions: [{ actionName: closeAction, buttonStatus: 'basic' }]
                }
              });

              return;
            }
          }

          this.toastrService.danger('An error occured', 'Error', {
            duration: 5000,
            status: 'danger'
          });
        }
      });
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
