import { Component, Inject, Input, OnInit } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';

@Component({
  selector: 'scenario-intents-search',
  templateUrl: './intents-search.component.html',
  styleUrls: ['./intents-search.component.scss']
})
export class IntentsSearchComponent implements OnInit {
  @Input() intentSentence: string;
  constructor(public dialogRef: NbDialogRef<IntentsSearchComponent>) {}
  loading: boolean = true;
  title: string = 'Searching existing intents';

  ngOnInit(): void {
    setTimeout(() => {
      this.loading = false;
      this.title = `Intents corresponding to sentence : ${this.intentSentence}`;
    }, 1000);
  }
}
