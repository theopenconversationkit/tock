import { Component, ElementRef, OnDestroy, OnInit, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { AbstractControl, FormArray, FormControl, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { NbDialogRef, NbToastrService } from '@nebular/theme';
import { Subject, takeUntil } from 'rxjs';
import { StateService } from '../../../core-nlp/state.service';
import { Dataset } from '../models';
import { DatasetsService } from '../services/datasets.service';

interface QuestionForm {
  question: FormControl<string>;
  groundTruth: FormControl<string>;
}

interface DatasetForm {
  name: FormControl<string>;
  description: FormControl<string>;
  questions: FormArray<FormGroup<QuestionForm>>;
}

function atLeastOneFilledQuestion(control: AbstractControl): ValidationErrors | null {
  const array = control as FormArray;
  const hasFilled = array.controls.some((g) => (g as FormGroup).get('question')?.value?.trim());
  return hasFilled ? null : { noFilledQuestion: true };
}

@Component({
  selector: 'tock-dataset-create',
  templateUrl: './dataset-create.component.html',
  styleUrl: './dataset-create.component.scss'
})
export class DatasetCreateComponent implements OnInit, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  isSubmitted: boolean = false;
  isLoading: boolean = false;

  // Injected by the dialog caller when editing an existing dataset
  dataset?: Dataset;

  get isEditMode(): boolean {
    return !!this.dataset;
  }

  form = new FormGroup<DatasetForm>({
    name: new FormControl('', [Validators.required, Validators.minLength(5), Validators.maxLength(100)]),
    description: new FormControl('', [Validators.maxLength(750)]),
    questions: new FormArray<FormGroup<QuestionForm>>([], [atLeastOneFilledQuestion])
  });

  @ViewChildren('questionInput') questionInputs: QueryList<ElementRef<HTMLInputElement>>;
  @ViewChild('submitButton') submitButton: ElementRef<HTMLButtonElement>;

  constructor(
    public dialogRef: NbDialogRef<DatasetCreateComponent>,
    private stateService: StateService,
    private datasetsService: DatasetsService,
    private toastrService: NbToastrService
  ) {}

  ngOnInit(): void {
    if (this.isEditMode) {
      this._populateForm(this.dataset);
    } else {
      this._appendEmptyQuestion();
    }
  }

  private _populateForm(dataset: Dataset): void {
    this.form.patchValue({
      name: dataset.name,
      description: dataset.description
    });

    dataset.questions.forEach((q) => {
      this.questions.push(
        new FormGroup<QuestionForm>({
          question: new FormControl(q.question, [Validators.minLength(5), Validators.maxLength(500)]),
          groundTruth: new FormControl(q.groundTruth ?? '', [Validators.maxLength(1000)])
        })
      );
    });

    // Append the empty trailing row for adding new questions
    this._appendEmptyQuestion();
  }

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  get questions(): FormArray<FormGroup<QuestionForm>> {
    return this.form.controls.questions;
  }

  getFormControl(name: keyof DatasetForm): FormControl {
    return this.form.get(name) as FormControl;
  }

  isLastEntry(index: number): boolean {
    return index === this.questions.length - 1;
  }

  onQuestionInput(index: number): void {
    if (this.isLastEntry(index) && this.questions.at(index).controls.question.value.trim()) {
      this._appendEmptyQuestion();
    }
    this.questions.updateValueAndValidity();
  }

  private _appendEmptyQuestion(): void {
    this.questions.push(
      new FormGroup<QuestionForm>({
        question: new FormControl('', [Validators.minLength(5), Validators.maxLength(500)]),
        groundTruth: new FormControl('', [Validators.maxLength(1000)])
      })
    );
  }

  removeQuestion(index: number): void {
    this.questions.removeAt(index);
    if (this.questions.length === 0) {
      this._appendEmptyQuestion();
    }
    this.questions.updateValueAndValidity();
  }

  private _getFilledQuestions() {
    return this.questions.controls
      .filter((g) => g.controls.question.value.trim())
      .map((g, i) => ({
        // Preserve the original question ID when editing so the backend can diff
        ...(this.isEditMode && this.dataset.questions[i] ? { id: this.dataset.questions[i].id } : {}),
        question: g.controls.question.value.trim(),
        groundTruth: g.controls.groundTruth.value.trim()
      }));
  }

  submit(): void {
    this.isSubmitted = true;
    this.questions.updateValueAndValidity();

    if (!this.form.valid) return;

    this.isLoading = true;

    if (this.isEditMode) {
      this._update();
    } else {
      this._create();
    }
  }

  private _create(): void {
    const { namespace, name: botId } = this.stateService.currentApplication;

    this.datasetsService
      .createDataset({
        name: this.form.controls.name.value,
        description: this.form.controls.description.value,
        questions: this._getFilledQuestions()
      })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (created: Dataset) => this.dialogRef.close(created),
        error: () => {
          this.isLoading = false;
          this.toastrService.danger('An error occured', 'Error', { duration: 5000 });
        }
      });
  }

  private _update(): void {
    this.datasetsService
      .updateDataset(this.dataset.id, {
        name: this.form.controls.name.value,
        description: this.form.controls.description.value,
        questions: this._getFilledQuestions()
      })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updated: Dataset) => this.dialogRef.close(updated),
        error: () => {
          this.isLoading = false;
          this.toastrService.danger('An error occured', 'Error', { duration: 5000 });
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
