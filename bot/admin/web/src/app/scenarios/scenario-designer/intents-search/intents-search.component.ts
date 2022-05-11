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
      this.title = `Existing intents`;
    }, 500);
  }

  cancel(): void {
    this.dialogRef.close();
  }

  createNewIntent(): void {
    this.createNewIntentEvent.emit();
  }

  foundIntents = [
    {
      intentId: 'activer_ca',
      sentences: [
        'Comment activer ma carte ?',
        'Je souhaite activer ma carte',
        'Comment activer ma carte bancaire'
      ]
    },
    {
      intentId: 'activer_carte_banc',
      sentences: [
        "Bonjour, je n'arrive pas à activer ma carte Aumax",
        'Je souhaite activer ma carte Aumax',
        'Comment activer ma carte Aumax'
      ]
    }
  ];
}
