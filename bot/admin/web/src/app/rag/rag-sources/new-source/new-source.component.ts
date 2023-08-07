import { Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { isUrl } from '../../../shared/utils';
import { Source, SourceTypes } from '../models';

interface NewSourceForm {
  id?: FormControl<string>;
  name: FormControl<string>;
  description: FormControl<string>;
  source_type: FormControl<SourceTypes>;
  source_parameters: FormGroup<SourceParametersForm>;
}
interface SourceParametersForm {
  source_url?: FormControl<URL>;
  exclusion_urls?: FormArray<FormControl<URL>>;
  addExclusionUrlInputControl?: FormControl<URL>;
  xpaths?: FormArray<FormControl<string>>;
  addXPathInputControl?: FormControl<string>;
  periodic_update?: FormControl<boolean>;
  periodic_update_frequency?: FormControl<number>;
}

@Component({
  selector: 'tock-new-source',
  templateUrl: './new-source.component.html',
  styleUrls: ['./new-source.component.scss']
})
export class NewSourceComponent implements OnInit {
  isSubmitted: boolean = false;
  sourceTypes = SourceTypes;

  @Input() source?: Source;
  @Output() onSave = new EventEmitter();

  @ViewChild('addExclusionUrlInput') addExclusionUrlInput: ElementRef;
  @ViewChild('addXPathInput') addXPathInput: ElementRef;

  constructor(public dialogRef: NbDialogRef<NewSourceComponent>) {}

  ngOnInit(): void {
    this.source_type.valueChanges.subscribe((type) => {
      const validators = [Validators.required, this.isControlUrl.bind(this)];
      if (type === this.sourceTypes.remote) {
        this.source_url.addValidators(validators);
      } else {
        this.source_url.clearValidators();
      }
      this.source_url.updateValueAndValidity();
    });

    if (this.source) {
      this.source_type.disable();
      this.form.patchValue(this.source);
      this.source.source_parameters.exclusion_urls?.forEach((url) => {
        this.exclusion_urls.push(new FormControl(url));
      });
      this.source.source_parameters.xpaths?.forEach((xpath) => {
        this.xpaths.push(new FormControl(xpath));
      });
      this.setPeriodicUpdateFrequencyDisabledState(this.source.source_parameters?.periodic_update);
    }

    this.periodic_update.valueChanges.subscribe((state) => {
      this.setPeriodicUpdateFrequencyDisabledState(state);
      if (state) {
        this.periodic_update_frequency.addValidators([Validators.required]);
      } else {
        this.periodic_update_frequency.clearValidators();
      }
      this.periodic_update_frequency.updateValueAndValidity();
    });
  }

  setPeriodicUpdateFrequencyDisabledState(state: boolean): void {
    if (state) {
      this.periodic_update_frequency.enable();
    } else {
      this.periodic_update_frequency.disable();
    }
  }

  form = new FormGroup<NewSourceForm>({
    id: new FormControl(undefined),
    name: new FormControl(undefined, [Validators.required, Validators.minLength(6), Validators.maxLength(40)]),
    description: new FormControl(undefined, [Validators.maxLength(80)]),
    source_type: new FormControl(undefined, [Validators.required]),
    source_parameters: new FormGroup<SourceParametersForm>({
      source_url: new FormControl(undefined),
      exclusion_urls: new FormArray([]),
      addExclusionUrlInputControl: new FormControl(undefined, [this.isControlUrl.bind(this)]),
      xpaths: new FormArray([]),
      addXPathInputControl: new FormControl(undefined),
      periodic_update: new FormControl(undefined),
      periodic_update_frequency: new FormControl(undefined)
    })
  });

  get name(): FormControl {
    return this.form.get('name') as FormControl;
  }

  get description(): FormControl {
    return this.form.get('description') as FormControl;
  }

  get source_type(): FormControl {
    return this.form.get('source_type') as FormControl;
  }

  get source_url(): FormControl {
    return this.form.controls.source_parameters.get('source_url') as FormControl;
  }
  get exclusion_urls(): FormArray {
    return this.form.controls.source_parameters.get('exclusion_urls') as FormArray;
  }
  get addExclusionUrlInputControl(): FormControl {
    return this.form.controls.source_parameters.get('addExclusionUrlInputControl') as FormControl;
  }
  get xpaths(): FormArray {
    return this.form.controls.source_parameters.get('xpaths') as FormArray;
  }
  get addXPathInputControl(): FormControl {
    return this.form.controls.source_parameters.get('addXPathInputControl') as FormControl;
  }

  get periodic_update(): FormControl {
    return this.form.controls.source_parameters.get('periodic_update') as FormControl;
  }
  get periodic_update_frequency(): FormControl {
    return this.form.controls.source_parameters.get('periodic_update_frequency') as FormControl;
  }

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  private isControlUrl(control: FormControl): null | {} {
    if (!control?.value?.length) return null;

    if (isUrl(control.value)) {
      return null;
    }

    return { custom: 'This is not a valid url' };
  }

  addExclusionUrlIsSubmitted = false;

  addExclusionUrl(): void {
    this.addExclusionUrlIsSubmitted = true;

    const eventTarget = this.addExclusionUrlInput.nativeElement as HTMLInputElement;
    const exclusionUrlString = eventTarget.value.trim();

    if (isUrl(exclusionUrlString)) {
      const exclusionUrl = new URL(exclusionUrlString);
      if (exclusionUrlString) {
        this.exclusion_urls.push(new FormControl(exclusionUrl));
        if (eventTarget) eventTarget.value = '';
        this.form.markAsDirty();
      }
    }
  }

  removeExclusionUrl(index) {
    this.exclusion_urls.removeAt(index);
  }

  addXPathIsSubmitted = false;

  addXPath(): void {
    this.addXPathIsSubmitted = true;

    const eventTarget = this.addXPathInput.nativeElement as HTMLInputElement;
    const xPathString = eventTarget.value.trim();

    if (xPathString) {
      this.xpaths.push(new FormControl(xPathString));
      if (eventTarget) eventTarget.value = '';
      this.form.markAsDirty();
    }
  }

  removeXPath(index) {
    this.xpaths.removeAt(index);
  }

  saveNewSource(): void {
    this.isSubmitted = true;

    if (this.canSave) {
      this.onSave.emit(this.form.value);
      this.cancel();
    }
  }

  checkUnvalidatedInputs() {
    return !this.addExclusionUrlInput?.nativeElement?.value.trim().length && !this.addXPathInput?.nativeElement?.value.trim().length;
  }

  getUnvalidatedInputsWarning() {
    if (this.addExclusionUrlInput?.nativeElement?.value.trim().length && this.addXPathInput?.nativeElement?.value.trim().length) {
      return 'You have entered an exclusion url and an xPath but have not added them. Click SAVE to ignore.';
    }
    if (this.addExclusionUrlInput?.nativeElement?.value.trim().length) {
      return 'You have entered an exclusion url but not added it. Click SAVE to ignore.';
    }
    if (this.addXPathInput?.nativeElement?.value.trim().length) {
      return 'You have entered an xPath but not added it. Click SAVE to ignore.';
    }
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
