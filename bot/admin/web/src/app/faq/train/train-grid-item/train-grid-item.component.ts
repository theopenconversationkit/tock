import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';

import {Observable, of, ReplaySubject} from "rxjs";
import {delay, take, tap} from 'rxjs/operators';
import {Intent, Sentence, SentenceStatus} from "../../../model/nlp";
import {StateService} from "../../../core-nlp/state.service";
import {DialogService} from "../../../core-nlp/dialog.service";
import {NlpService} from "../../../nlp-tabs/nlp.service";
import {IntentsService} from "../../common/intents.service";
import {SentencesService} from "../../common/sentences.service";

function truncate(input?: string): string {
  if (input && input.length > 40) {
    return input.substring(0, 40) + '...';
  }
  return input;
}

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

  @Output()
  onRemove = new EventEmitter<boolean>();

  @Output()
  onValidate = new EventEmitter<boolean>();

  @Output()
  onUnknown = new EventEmitter<boolean>();

  public cardCssClass = "tock--opened"; // card closing animation

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);

  constructor(
      public readonly state: StateService,
      private readonly nlp: NlpService,
      private readonly dialog: DialogService,
      private readonly intentsService: IntentsService,
      private readonly sentencesService: SentencesService
  ) { }

  ngOnInit(): void {
  }

  ngOnDestroy() {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  public async newIntent(): Promise<void> {
    // cleanup entities
    this.sentence.classification.entities = [];
    const savedIntention = await this.intentsService.newIntent(
      this.destroy$
    );

    this.sentence = this.sentence.withIntent(this.state, savedIntention._id);
  }

  public async validate(): Promise<void> {
    const intentId = this.sentence.classification.intentId;
    if (!intentId) {
      this.dialog.notify(`Please select an intent first`);
      return;
    }

    if (intentId === Intent.unknown) {
      this.sentence.classification.intentId = Intent.unknown;
      this.sentence.classification.entities = [];
    }
    this.sentence.status = SentenceStatus.validated;

    await this.sentencesService.save(this.sentence, this.destroy$)
      .pipe(take(1))
      .toPromise();

    this.dialog.notify(`Validated`,
      truncate(this.sentence.text), {duration: 2000, status: "basic"});

    this.hide().subscribe(_ => {
      this.onValidate.emit(true);
    });
  }

  public async unknown(): Promise<void> {
    this.sentence.classification.intentId = Intent.unknown;
    this.sentence.classification.entities = [];
    this.sentence.status = SentenceStatus.validated;

    await this.sentencesService.save(this.sentence, this.destroy$)
      .pipe(take(1))
      .toPromise();

    this.dialog.notify(`Unknown`,
      truncate(this.sentence.text), {duration: 2000, status: "basic"});

    this.hide().subscribe(_ => {
      this.onUnknown.emit(true);
    });
  }

  public async remove(): Promise<void> {
    this.sentence.status = SentenceStatus.deleted;

    await this.sentencesService.save(this.sentence, this.destroy$)
      .pipe(take(1))
      .toPromise();

    this.dialog.notify(`Deleted`,
      truncate(this.sentence.text), {duration: 2000, status: "basic"});


    this.hide().subscribe(_ => {
      this.onRemove.emit(true);
    });
  }

  private hide(): Observable<boolean> {
    this.cardCssClass = 'tock--closed';

    return of(true)
      .pipe(
        delay(500),
        tap(_ =>  this.cardCssClass = 'tock--hidden' )
        );
  }

}
