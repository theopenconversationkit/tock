import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NbDialogRef } from '@nebular/theme';
import { Source, SourceImportData, SourceImportParams } from '../../models';

@Component({
  selector: 'tock-source-normalization-csv',
  templateUrl: './source-normalization-csv.component.html',
  styleUrls: ['./source-normalization-csv.component.scss']
})
export class SourceNormalizationCsvComponent {
  @Input() source?: Source;

  @Output() onNormalize = new EventEmitter<SourceImportParams>();

  constructor(public dialogRef: NbDialogRef<SourceNormalizationCsvComponent>) {}

  form: FormGroup = new FormGroup({
    content: new FormControl<string>(null, Validators.required),
    source_ref: new FormControl<string>(null)
  });

  get content(): FormControl {
    return this.form.get('content') as FormControl;
  }
  get source_ref(): FormControl {
    return this.form.get('source_ref') as FormControl;
  }

  get canSave(): boolean {
    return this.isSubmitted ? this.form.valid : this.form.dirty;
  }

  isSubmitted: boolean = false;

  submit(): void {
    this.isSubmitted = true;
    if (this.canSave) {
      const columns = this.getColumnNames();
      const normalization = {
        content: columns.findIndex((col) => col === this.content.value),
        source_ref: columns.findIndex((col) => col === this.source_ref.value)
      };

      const rawData: [] = this.source.rawData;
      rawData.splice(0, 1);

      const normalizedData = rawData.map((entry) => {
        const frag: SourceImportData = { content: undefined, source_ref: undefined };
        Object.entries(normalization).forEach((norm) => {
          if (norm[1]) frag[norm[0]] = entry[norm[1]];
        });
        return frag;
      });

      this.onNormalize.emit({
        content_path: this.content.value,
        source_path: this.source_ref.value,
        content: normalizedData
      });
    }
  }

  getColumnNames(): any[] {
    return this.source.rawData[0];
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
