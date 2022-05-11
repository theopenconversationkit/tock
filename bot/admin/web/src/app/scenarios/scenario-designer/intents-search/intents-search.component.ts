import { Component, EventEmitter, Inject, Input, OnInit, Output } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';

@Component({
  selector: 'scenario-intents-search',
  templateUrl: './intents-search.component.html',
  styleUrls: ['./intents-search.component.scss']
})
export class IntentsSearchComponent implements OnInit {
  @Input() intentSentence: string;
  @Output() createNewIntentEvent = new EventEmitter();

  constructor(public dialogRef: NbDialogRef<IntentsSearchComponent>) {}
  loading: boolean = true;
  title: string = 'Searching existing intents';

  ngOnInit(): void {
    setTimeout(() => {
      this.loading = false;
      this.title = `Intents corresponding to sentence : ${this.intentSentence}`;
    }, 1000);
  }

  cancel() {
    this.dialogRef.close();
  }

  createNewIntent() {
    this.createNewIntentEvent.emit();
  }

  foundIntents = [
    {
      intentId: '625e821d8e89505241619a6a',
      sentences: [
        'Comment activer ma carte?',
        'Je souhaite activer ma carte',
        'Comment activer ma carte bancaire'
      ]
    },
    {
      intentId: '514e710d7e78505241619a6a',
      sentences: [
        'Comment activer ma carte Aumax?',
        'Je souhaite activer ma carte Aumax',
        'Comment activer ma carte Aumax'
      ]
    }
  ];
}
