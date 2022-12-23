import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogService, NbTagComponent, NbTagInputAddEvent } from '@nebular/theme';
import { Observable, of } from 'rxjs';

import { ScenarioGroup } from '../../models';
import { ScenarioService } from '../../services';
import { ChoiceDialogComponent } from '../../../shared/components';

@Component({
  selector: 'tock-scenario-edit',
  templateUrl: './scenario-edit.component.html',
  styleUrls: ['./scenario-edit.component.scss']
})
export class ScenarioEditComponent implements OnChanges {
  @Input() loading: boolean;
  @Input() scenarioGroup?: ScenarioGroup;

  @Output() onClose = new EventEmitter<boolean>();
  @Output() onSave = new EventEmitter<{ scenarioGroup: ScenarioGroup; redirect: boolean }>();

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

  constructor(private nbDialogService: NbDialogService, private scenarioService: ScenarioService) {}

  categories: string[];
  categoriesAutocompleteValues: Observable<string[]>;
  tagsAutocompleteValues: Observable<string[]>;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.scenarioGroup?.currentValue) {
      const scenarioGroup: ScenarioGroup = changes.scenarioGroup.currentValue;

      this.form.reset();
      this.tags.clear();
      this.isSubmitted = false;

      if (scenarioGroup) {
        this.form.patchValue(scenarioGroup);

        if (scenarioGroup.tags?.length) {
          scenarioGroup.tags.forEach((tag) => {
            this.tags.push(new FormControl(tag));
          });
        }
      }
    }

    this.categories = [...this.scenarioService.getState().categories];
    this.tagsAutocompleteValues = of([...this.scenarioService.getState().tags]);
  }

  updateTagsAutocompleteValues(event: any): void {
    this.tagsAutocompleteValues = of(
      this.scenarioService.getState().tags.filter((tag) => tag.toLowerCase().includes(event.target.value.toLowerCase()))
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
      const dialogRef = this.nbDialogService.open(ChoiceDialogComponent, {
        context: {
          title: `Cancel ${this.scenarioGroup?.id ? 'edit' : 'create'} scenario`,
          subtitle: 'Are you sure you want to cancel ? Changes will not be saved.',
          actions: [
            { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
            { actionName: validAction, buttonStatus: 'danger' }
          ],
          modalStatus: 'danger'
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
      const enabled = typeof this.scenarioGroup.enabled === 'boolean' ? this.scenarioGroup.enabled : null;

      this.onSave.emit({
        redirect: redirect,
        scenarioGroup: { id: this.scenarioGroup.id, ...this.form.value, enabled }
      });
    }
  }

  eventPreventDefault(e: KeyboardEvent): void {
    e.preventDefault();
  }
}
