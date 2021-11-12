import { Component, Input, OnInit, ChangeDetectionStrategy } from '@angular/core';
import {Intent, Sentence} from "../../../model/nlp";
import {StateService} from "../../../core-nlp/state.service";

@Component({
  selector: 'tock-train-grid-item',
  templateUrl: './train-grid-item.component.html',
  styleUrls: ['./train-grid-item.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TrainGridItemComponent implements OnInit {

  @Input()
  public sentence: Sentence;

  @Input()
  public intents: Intent[];

  constructor(
      public readonly state: StateService
  ) { }

  ngOnInit(): void {
  }

}
