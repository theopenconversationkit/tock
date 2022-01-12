import { Component, OnInit, Input } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { Utterance } from '../../model/frequent-question';
import { EditUtteranceResult } from './edit-utterance-result';

/**
 * Edit Utterance DIALOG
 */

@Component({
  selector: 'tock-edit-utterance',
  templateUrl: './edit-utterance.component.html',
  styleUrls: ['./edit-utterance.component.scss']
})
export class EditUtteranceComponent implements OnInit {

  @Input()
  public title: string;

  @Input()
  public value: string;

  @Input()
  public lookup?: (string) => (Utterance | null);

  public existingQuestion?: string;

  constructor(
    private readonly dialogRef: NbDialogRef<EditUtteranceComponent>
  ) { }

  ngOnInit(): void {
  }

  cancel(): void {
    const result: EditUtteranceResult = {
      cancelled: true
    };
    this.dialogRef.close(result);
  }

  save(): void {
    const result: EditUtteranceResult = {
      cancelled: false,
      value: this.value || ''
    };
    this.dialogRef.close(result);
  }

  canSave(): boolean {
    if (!this.value) {
      return false;
    }

    return this.value.trim().length > 0;
  }

  ensureUniq(evt): void {
    const res = this.lookup ? this.lookup(this.value) : undefined; // look for similar question
    if (res) {
      this.existingQuestion = res;
    } else {
      this.existingQuestion = undefined;
    }
  }
}
