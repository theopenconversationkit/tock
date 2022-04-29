import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { NbTagComponent, NbTagInputAddEvent } from '@nebular/theme';

import { Scenario } from '../models';

@Component({
  selector: 'tock-scenario-edit',
  templateUrl: './scenario-edit.component.html',
  styleUrls: ['./scenario-edit.component.scss']
})
export class ScenarioEditComponent implements OnInit, OnChanges {
  @Input()
  scenario?: Scenario;

  @Output()
  handleClose = new EventEmitter<boolean>();

  @Output()
  handleSave = new EventEmitter<Scenario>();

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

  constructor() {}

  ngOnInit(): void {}

  ngOnChanges(changes: SimpleChanges): void {
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

  onTagAdd({ value, input }: NbTagInputAddEvent): void {
    if (value && !this.tags.value.find((v: string) => v.toUpperCase() === value.toUpperCase())) {
      this.tags.push(new FormControl(value));
    }

    input.nativeElement.value = '';
  }

  onTagRemove(tag: NbTagComponent): void {
    const tagToRemove = this.tags.value.findIndex((t: string) => t === tag.text);

    if (tagToRemove !== -1) {
      this.tags.removeAt(tagToRemove);
    }
  }

  close(): void {
    this.handleClose.emit(true);
  }

  save(): void {
    this.isSubmitted = true;

    if (this.canSave) {
      console.log({ ...this.scenario, ...this.form.value });
      this.handleSave.emit({ ...this.scenario, ...this.form.value } as Scenario);
    }
  }

  saveRedirectDesigner(): void {
    this.save();
    // TODO redirect to designer
  }

  eventPreventDefault(e: KeyboardEvent): void {
    e.preventDefault();
  }
}
