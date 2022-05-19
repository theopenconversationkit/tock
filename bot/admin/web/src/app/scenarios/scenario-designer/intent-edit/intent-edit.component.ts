import { Component, EventEmitter, Inject, Input, OnInit, Output } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { StateService } from 'src/app/core-nlp/state.service';
import { scenarioItem } from '../../models';

@Component({
  selector: 'scenario-intent-edit',
  templateUrl: './intent-edit.component.html',
  styleUrls: ['./intent-edit.component.scss']
})
export class IntentEditComponent implements OnInit {
  @Input() item: scenarioItem;
  constructor(public dialogRef: NbDialogRef<IntentEditComponent>, protected state: StateService) {}

  ngOnInit(): void {}

  dissociateIntent() {
    delete this.item.intentDefinition;
    this.cancel();
  }

  addSentence($event) {
    if ($event.target.value.trim()) {
      if (!this.item._sentences) this.item._sentences = [];
      this.item._sentences.push($event.target.value.trim());
      $event.target.value = '';
    }
  }

  removeSentence(sentence) {
    this.item._sentences = this.item._sentences.filter((s) => s != sentence);
  }

  save() {}

  cancel(): void {
    this.dialogRef.close();
  }
}
