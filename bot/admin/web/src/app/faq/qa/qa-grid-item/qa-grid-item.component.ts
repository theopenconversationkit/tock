import {saveAs} from "file-saver";
import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {Observable, of, ReplaySubject } from 'rxjs';
import { delay, take, tap } from 'rxjs/operators';
import { DialogService } from 'src/app/core-nlp/dialog.service';
import { FrequentQuestion } from '../../common/model/frequent-question';
import {isDocked, ViewMode } from '../../common/model/view-mode';
import { QaService } from '../../common/qa.service';
import { truncate } from '../../common/util/string-utils';
import { ConfirmDialogComponent } from 'src/app/shared-nlp/confirm-dialog/confirm-dialog.component';
import { NbToastrService } from "@nebular/theme/components/toastr/toastr.service";
import { StateService } from "src/app/core-nlp/state.service";

@Component({
  selector: 'tock-qa-grid-item',
  templateUrl: './qa-grid-item.component.html',
  styleUrls: ['./qa-grid-item.component.scss'],
  host: {'class': 'd-block mb-3'}
})
export class QaGridItemComponent implements OnInit, OnDestroy {

  @Input()
  item: FrequentQuestion;

  @Input()
  viewMode: ViewMode;

  @Output()
  onRemove = new EventEmitter<boolean>();

  @Output()
  onEdit= new EventEmitter<FrequentQuestion>();

  @Output()
  onDownload = new EventEmitter<boolean>();

  public hideableCssClass = "tock--opened"; // card closing animation

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);

  constructor(
    private readonly state: StateService,
    private readonly qaService: QaService,
    private readonly dialog: DialogService,
  ) {
  }

  ngOnInit(): void {
  }

  ngOnDestroy() {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  edit(): void {
    this.onEdit.emit(this.item);
  }

  isDocked(): boolean {
    return isDocked(this.viewMode);
  }

  getFirstUtterance(): string {
    return this.item.utterances[0] || '';
  }

  async remove(): Promise<any> {
    await this.qaService.delete(this.item, this.destroy$)
      .pipe(take(1))
      .toPromise();

    this.dialog.notify(`Deleted`,
      truncate(this.item.title), {duration: 2000, status: "basic"});


    this.hide().subscribe(_ => {
      this.onRemove.emit(true);
    });
  }

  async toggleEnabled(evt): Promise<any> {
    const newValue = !this.item.enabled;

    const result = await this.dialog.openDialog(ConfirmDialogComponent, {
      context: {
        title: `Toggle ${newValue? 'On' : 'Off'}`,
        subtitle: newValue ?
          `Activate '${this.item.title}' ?`
          : `Disable '${this.item.title}' ?`,
        action: 'Yes'
      }
    }).onClose.pipe(take(1)).toPromise();

    console.log('result', result);
    if (result.toLowerCase() !== 'yes') {
      return;
    }

    let done$: Observable<unknown>;
    if (newValue) {
      done$ = this.qaService.activate(this.item, this.destroy$);
    } else {
      done$ = this.qaService.disable(this.item, this.destroy$);
    }

    await done$.pipe(take(1)).toPromise();

    // this way user will be aware of a failure if any
    this.item.enabled = newValue;
  }

  download(): void {
    var jsonBlob = new Blob([JSON.stringify(this.item)], {
      type: 'application/json'
    });

    // TODO: A more useful download
    saveAs(jsonBlob, this.state.currentApplication.name +
      "_" +
      this.state.currentLocale +
      "_faq_" +
      new Date().getTime() +
      ".json"
    );
  }

  private hide(): Observable<boolean> {
    this.hideableCssClass = 'tock--closed';

    return of(true)
      .pipe(
        delay(800),
        tap(_ =>  this.hideableCssClass = 'tock--hidden' )
      );
  }
}
