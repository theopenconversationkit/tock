import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {Observable, of, ReplaySubject } from 'rxjs';
import { delay, take, tap } from 'rxjs/operators';
import { DialogService } from 'src/app/core-nlp/dialog.service';
import { Qa, QaStatus } from '../../common/model/qa';
import { QaService } from '../../common/qa.service';
import { truncate } from '../../common/util/string-utils';

@Component({
  selector: 'tock-qa-grid-item',
  templateUrl: './qa-grid-item.component.html',
  styleUrls: ['./qa-grid-item.component.scss'],
  host: {'class': 'd-block mb-3'}
})
export class QaGridItemComponent implements OnInit, OnDestroy {

  @Input()
  qa: Qa;

  @Output()
  onRemove = new EventEmitter<boolean>();

  @Output()
  onEdit= new EventEmitter<boolean>();

  @Output()
  onDownload = new EventEmitter<boolean>();

  public hideableCssClass = "tock--opened"; // card closing animation

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);

  constructor(
    private qaService: QaService,
    private readonly dialog: DialogService,
  ) {
  }

  ngOnInit(): void {
  }

  ngOnDestroy() {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  download(): void {

  }

  edit(): void {

  }

  async remove(): Promise<any> {
    this.qa.status = QaStatus.deleted;

    await this.qaService.save(this.qa, this.destroy$)
      .pipe(take(1))
      .toPromise();

    this.dialog.notify(`Deleted`,
      truncate(this.qa.title), {duration: 2000, status: "basic"});


    this.hide().subscribe(_ => {
      this.onRemove.emit(true);
    });
  }

  private hide(): Observable<boolean> {
    this.hideableCssClass = 'tock--closed';

    return of(true)
      .pipe(
        delay(500),
        tap(_ =>  this.hideableCssClass = 'tock--hidden' )
      );
  }

}
