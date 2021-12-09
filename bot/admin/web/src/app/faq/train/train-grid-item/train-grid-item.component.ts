import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output
} from '@angular/core';
import {SelectionModel } from "@angular/cdk/collections";
import {Observable, of, ReplaySubject} from "rxjs";
import {delay, take, tap} from 'rxjs/operators';
import {Intent, Sentence, SentenceStatus} from "../../../model/nlp";
import {StateService} from "../../../core-nlp/state.service";
import {DialogService} from "../../../core-nlp/dialog.service";
import {NlpService} from "../../../nlp-tabs/nlp.service";
import {IntentsService} from "../../common/intents.service";
import {SentencesService} from "../../common/sentences.service";
import {SelectionMode} from "../../common/model/selection-mode";
import {truncate} from "../../common/util/string-utils";
import {isDocked, ViewMode} from "../../common/model/view-mode";

@Component({
  selector: 'tock-train-grid-item',
  templateUrl: './train-grid-item.component.html',
  styleUrls: ['./train-grid-item.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush // better perfs when list is huge
})
export class TrainGridItemComponent implements OnInit, OnDestroy {

  @Input()
  sentence: Sentence;

  @Input()
  intents: Intent[];

  @Input()
  selection: SelectionModel<Sentence>;

  @Input()
  selectionMode: SelectionMode;

  @Input()
  viewMode: ViewMode;

  @Output()
  onRemove = new EventEmitter<boolean>();

  @Output()
  onValidate = new EventEmitter<boolean>();

  @Output()
  onUnknown = new EventEmitter<boolean>();

  @Output()
  onDetails= new EventEmitter<Sentence>();

  selectedIntentId: string;

  public cardCssClass = "tock--opened"; // card closing animation

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);

  constructor(
    private  readonly ref: ChangeDetectorRef,
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

  public getRichIntentLabel() {
    const intentLabel = this.sentence.getIntentLabel(this.state);

    const str = truncate(intentLabel.padEnd(34, '\xa0'), 35)
      + " "
      + Math.trunc(this.sentence.classification.intentProbability * 100)
      + '%';

    return str;
  }

  public async newIntent(): Promise<void> {
    // cleanup entities
    this.sentence.classification.entities = [];
    const savedIntention = await this.intentsService.newIntent(
      this.destroy$
    );

    const prevSentence = this.sentence;
    this.sentence = this.sentence.withIntent(this.state, savedIntention._id);

    this.selectedIntentId = savedIntention._id;
    this.updateSelection(prevSentence, this.sentence);

    this.ref.detectChanges(); // because ChangeDetectionStrategy is OnPush
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

  public details(): void {
    this.onDetails.emit(this.sentence);
  }

  private hide(): Observable<boolean> {
    this.cardCssClass = 'tock--closed';

    return of(true)
      .pipe(
        delay(500),
        tap(_ =>  this.cardCssClass = 'tock--hidden' )
        );
  }

  toggle(): void {
    this.selection.toggle(this.sentence);
  }

  changeIntent(intentId: string): void {
    console.log("changeIntent", intentId);


    const prevSentence = this.sentence;
    this.sentence = this.sentence.withIntent(this.state, intentId);
    this.ref.detectChanges(); // because ChangeDetectionStrategy is OnPush

    this.updateSelection(prevSentence, this.sentence);
  }

  updateSelection(prevSentence: Sentence, newSentence: Sentence): void {
    // we update version of sentence in selection set, so that bulk actions could reuse selection directly
    if (this.selection.isSelected(prevSentence)) {
      this.selection.toggle(prevSentence);
      this.selection.select(newSentence);
    }
  }

  isSelected(): boolean {
    switch (this.selectionMode) {
      case "SELECT_ALWAYS":
        return true;
      case "SELECT_NEVER":
        return false;
      case "SELECT_SOME":
      default:
        return this.selection.isSelected(this.sentence);
    }
  }

  isDocked(): boolean {
    return isDocked(this.viewMode);
  }

}
