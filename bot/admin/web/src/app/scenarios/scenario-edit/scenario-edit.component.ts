import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { NbTagComponent, NbTagInputAddEvent } from '@nebular/theme';
import { Observable, of } from 'rxjs';

import { ConfirmDialogComponent } from '../../shared-nlp/confirm-dialog/confirm-dialog.component';
import { DialogService } from '../../core-nlp/dialog.service';
import { Scenario } from '../models';
import { ScenarioService } from '../services/scenario.service';

@Component({
  selector: 'tock-scenario-edit',
  templateUrl: './scenario-edit.component.html',
  styleUrls: ['./scenario-edit.component.scss']
})
export class ScenarioEditComponent implements OnChanges {
  @Input()
  loading: boolean;

  @Input()
  scenario?: Scenario;

  @Input()
  scenarios?: Scenario[];

  @Output()
  onClose = new EventEmitter<boolean>();

  @Output()
  onSave = new EventEmitter();

  @ViewChild('nameInput') nameInput: ElementRef;

  isSubmitted: boolean = false;

  form = new FormGroup({
    category: new FormControl(),
    description: new FormControl(),
    name: new FormControl(undefined, Validators.required),
    tags: new FormArray([])
  });

  get category(): FormControl {
    return this.form.get('category') as FormControl;
  }

  get description(): FormControl {
    return this.form.get('description') as FormControl;
  }

  get name(): FormControl {
    return this.form.get('name') as FormControl;
  }

  get tags(): FormArray {
    return this.form.get('tags') as FormArray;
  }

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  constructor(private dialogService: DialogService, private scenarioService: ScenarioService) {}

  categoriesAutocompleteValues: Observable<string[]>;
  tagsAutocompleteValues: Observable<string[]>;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.scenario?.currentValue) {
      const scenario: Scenario = changes.scenario.currentValue;

      this.form.reset();
      this.tags.clear();
      this.isSubmitted = false;

      if (scenario) {
        this.form.patchValue(scenario);

        if (scenario.tags?.length) {
          scenario.tags.forEach((tag) => {
            this.tags.push(new FormControl(tag));
          });
        }
      }
    }

    this.categoriesAutocompleteValues = of([...this.scenarioService.getState().categories]);
    this.tagsAutocompleteValues = of([...this.scenarioService.getState().tags]);

    setTimeout(() => {
      this.nameInput.nativeElement.focus();
    }, 100);
  }

  updateCategoriesAutocompleteValues(event: any) {
    this.categoriesAutocompleteValues = of(
      this.scenarioService
        .getState()
        .categories.filter((category) =>
          category.toLowerCase().includes(event.target.value.toLowerCase())
        )
    );
  }

  updateTagsAutocompleteValues(event: any) {
    this.tagsAutocompleteValues = of(
      this.scenarioService
        .getState()
        .tags.filter((tag) => tag.toLowerCase().includes(event.target.value.toLowerCase()))
    );
  }

  tagAdd({ value, input }: NbTagInputAddEvent): void {
    if (value && !this.tags.value.find((v: string) => v.toUpperCase() === value.toUpperCase())) {
      this.tags.push(new FormControl(value));
      this.form.markAsDirty();
      this.form.markAsTouched();
    }

    input.nativeElement.value = '';
  }

  tagRemove(tag: NbTagComponent): void {
    const tagToRemove = this.tags.value.findIndex((t: string) => t === tag.text);

    if (tagToRemove !== -1) {
      this.tags.removeAt(tagToRemove);
      this.form.markAsDirty();
      this.form.markAsTouched();
    }
  }

  close(): Observable<any> {
    const validAction = 'yes';
    if (this.form.dirty) {
      const dialogRef = this.dialogService.openDialog(ConfirmDialogComponent, {
        context: {
          title: `Cancel ${this.scenario?.id ? 'edit' : 'create'} scenario`,
          subtitle: 'Are you sure you want to cancel ? Changes will not be saved.',
          action: validAction
        }
      });
      dialogRef.onClose.subscribe((result) => {
        if (result === validAction) {
          this.onClose.emit(true);
        }
      });
      return dialogRef.onClose;
    } else {
      this.onClose.emit(true);
      return of(validAction);
    }
  }

  save(redirect = false): void {
    this.isSubmitted = true;

    if (this.canSave) {
      this.onSave.emit({
        redirect: redirect,
        scenario: { ...this.scenario, ...this.form.value }
      });
    }
  }

  eventPreventDefault(e: KeyboardEvent): void {
    e.preventDefault();
  }
}
