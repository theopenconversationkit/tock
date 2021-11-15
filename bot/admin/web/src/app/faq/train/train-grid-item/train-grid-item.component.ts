import {Component, Input, OnDestroy, OnInit} from '@angular/core';

import {ReplaySubject} from "rxjs";
import {Intent, Sentence} from "../../../model/nlp";
import {StateService} from "../../../core-nlp/state.service";
import {DialogService} from "../../../core-nlp/dialog.service";
import {NlpService} from "../../../nlp-tabs/nlp.service";
import {IntentsService} from "../../common/intents.service";

@Component({
  selector: 'tock-train-grid-item',
  templateUrl: './train-grid-item.component.html',
  styleUrls: ['./train-grid-item.component.scss'],
  /* changeDetection: ChangeDetectionStrategy.OnPush */
})
export class TrainGridItemComponent implements OnInit, OnDestroy {

  @Input()
  public sentence: Sentence;

  @Input()
  public intents: Intent[];

  private destroy$: ReplaySubject<boolean> = new ReplaySubject(1);

  constructor(
      public readonly state: StateService,
      private readonly nlp: NlpService,
      private readonly dialog: DialogService,
      private readonly intentsService: IntentsService
  ) { }

  ngOnInit(): void {
  }

  ngOnDestroy() {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  async newIntent(): Promise<void> {
    // cleanup entities
    this.sentence.classification.entities = [];
    const savedIntention = await this.intentsService.newIntent(
      this.destroy$
    );

    this.sentence = this.sentence.withIntent(this.state, savedIntention._id);
  }

}
