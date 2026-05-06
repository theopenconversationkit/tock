import { Component, ElementRef, OnDestroy, OnInit, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { AbstractControl, FormArray, FormControl, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { NbDialogRef, NbToastrService } from '@nebular/theme';
import { Subject, takeUntil } from 'rxjs';
import { StateService } from '../../../core-nlp/state.service';
import { Dataset } from '../models';
import { DatasetsService } from '../services/datasets.service';
import { getExportFileName } from '../../../shared/utils';
import { saveAs } from 'file-saver-es';

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
  return hasFilled ? null : { custom: 'At least one question is required.' };
}

const question_minLength = 2;
const question_maxLength = 1500;
const groundtruth_maxLength = 1500;

@Component({
  selector: 'tock-dataset-create',
  templateUrl: './dataset-create.component.html',
  styleUrl: './dataset-create.component.scss'
})
export class DatasetCreateComponent implements OnInit, OnDestroy {
  private readonly destroy$: Subject<boolean> = new Subject();

  isSubmitted: boolean = false;
  isLoading: boolean = false;

  /** Displayed once when the user modifies a pre-existing question in edit mode. */
  showQuestionEditWarning: boolean = false;

  /**
   * Snapshot of the original question text for each pre-existing question,
   * keyed by its index in the FormArray (before the trailing empty row is appended).
   */
  private _initialQuestionValues: Map<number, string> = new Map();

  // Injected by the dialog caller when editing an existing dataset
  dataset?: Dataset;

  get isEditMode(): boolean {
    return !!this.dataset;
  }

  form = new FormGroup<DatasetForm>({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(5), Validators.maxLength(100)] }),
    description: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(750)] }),
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
    if (this.isEditMode && this.dataset) {
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

    dataset.questions.forEach((q, i) => {
      this.questions.push(
        new FormGroup<QuestionForm>({
          question: new FormControl(q.question, {
            nonNullable: true,
            validators: [Validators.minLength(question_minLength), Validators.maxLength(question_maxLength)]
          }),
          groundTruth: new FormControl(q.groundTruth ?? '', {
            nonNullable: true,
            validators: [Validators.maxLength(groundtruth_maxLength)]
          })
        })
      );

      // Snapshot the original text so we can detect meaningful edits later.
      this._initialQuestionValues.set(i, q.question.trim());
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

  getFormArray(name: keyof DatasetForm): FormArray {
    return this.form.get(name) as FormArray;
  }

  isLastEntry(index: number): boolean {
    return index === this.questions.length - 1;
  }

  onQuestionInput(index: number): void {
    if (this.isLastEntry(index) && this.questions.at(index).controls.question.value.trim()) {
      this._appendEmptyQuestion();
    }

    // Show the one-time warning when a pre-existing question is modified in edit mode.
    if (this.isEditMode && !this.showQuestionEditWarning && this._initialQuestionValues.has(index)) {
      const current = this.questions.at(index).controls.question.value.trim();
      const initial = this._initialQuestionValues.get(index)!;
      if (current !== initial) {
        this.showQuestionEditWarning = true;
      }
    }

    this.questions.updateValueAndValidity();
  }

  private _appendEmptyQuestion(): void {
    this.questions.push(
      new FormGroup<QuestionForm>({
        question: new FormControl('', {
          nonNullable: true,
          validators: [Validators.minLength(question_minLength), Validators.maxLength(question_maxLength)]
        }),
        groundTruth: new FormControl('', { nonNullable: true, validators: [Validators.maxLength(groundtruth_maxLength)] })
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

  private _getFilledQuestions(omitIds: boolean = false): { id?: string; question: string; groundTruth: string }[] {
    return this.questions.controls
      .filter((g) => g.controls.question.value.trim())
      .map((g, i) => ({
        // Preserve the original question ID when editing so the backend can diff
        ...(this.isEditMode && !omitIds && this.dataset.questions[i] ? { id: this.dataset.questions[i].id } : {}),
        question: g.controls.question.value.trim(),
        groundTruth: g.controls.groundTruth.value.trim()
      }));
  }

  get invalidQuestionCount(): number {
    return this.questions.controls.filter((g, i) => !this.isLastEntry(i) && (g.controls.question.invalid || g.controls.groundTruth.invalid))
      .length;
  }

  private _scrollToFirstInvalidField(): void {
    setTimeout(() => {
      if (this.form.controls.name.invalid) {
        const nameInput = document.querySelector('[formControlName="name"]') as HTMLElement;
        nameInput?.scrollIntoView({ behavior: 'smooth', block: 'center' });
        nameInput?.focus();
        return;
      }

      if (this.form.controls.description.invalid) {
        const descInput = document.querySelector('[formControlName="description"]') as HTMLElement;
        descInput?.scrollIntoView({ behavior: 'smooth', block: 'center' });
        descInput?.focus();
        return;
      }

      const invalidIndex = this.questions.controls.findIndex(
        (g, i) => !this.isLastEntry(i) && (g.controls.question.invalid || g.controls.groundTruth.invalid)
      );

      if (invalidIndex !== -1) {
        const inputs = this.questionInputs.toArray();
        const el = inputs[invalidIndex]?.nativeElement;
        el?.scrollIntoView({ behavior: 'smooth', block: 'center' });
        el?.focus();
      }
    }, 50);
  }

  submit(): void {
    this.isSubmitted = true;
    this.questions.updateValueAndValidity();

    if (!this.form.valid) {
      this._scrollToFirstInvalidField();
      return;
    }

    this.isLoading = true;

    if (this.isEditMode) {
      this._update();
    } else {
      this._create();
    }
  }

  private _create(): void {
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

  exportDataset(): void {
    const dataStr = JSON.stringify({
      name: this.form.controls.name.value,
      description: this.form.controls.description.value,
      questions: this._getFilledQuestions(true) // Omit question IDs in export since they are only relevant for diffing during updates
    });

    const exportFileName = getExportFileName(
      this.stateService.currentApplication.namespace,
      this.stateService.currentApplication.name,
      'dataset',
      'json'
    );

    const blob = new Blob([dataStr], {
      type: 'application/json'
    });

    saveAs(blob, exportFileName);
  }

  onQuestionPaste(event: ClipboardEvent, index: number): void {
    const plainText = event.clipboardData?.getData('text');
    if (!plainText) return;

    const html = event.clipboardData?.getData('text/html') ?? '';
    const isFromSpreadsheet = /<table[\s>]/i.test(html);

    const lines = plainText
      .split(/\r\n|\r|\n/)
      .map((l) => l.trim())
      .filter((l) => l.length > 0);

    if (lines.length <= 1 || !isFromSpreadsheet) return;

    event.preventDefault();

    lines.forEach((line, i) => {
      const targetIndex = index + i;

      if (targetIndex < this.questions.length) {
        this.questions.at(targetIndex).controls.question.setValue(line);
      } else {
        this.questions.push(
          new FormGroup<QuestionForm>({
            question: new FormControl(line, [Validators.minLength(question_minLength), Validators.maxLength(question_maxLength)]),
            groundTruth: new FormControl('', [Validators.maxLength(groundtruth_maxLength)])
          })
        );
      }
    });

    const last = this.questions.at(this.questions.length - 1);
    if (last.controls.question.value.trim()) {
      this._appendEmptyQuestion();
    }

    this.questions.updateValueAndValidity();

    setTimeout(() => {
      const inputs = this.questionInputs.toArray();
      inputs[index + lines.length]?.nativeElement.focus();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }
}
