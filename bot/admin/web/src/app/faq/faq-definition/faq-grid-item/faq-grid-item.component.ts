/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {saveAs} from "file-saver";
import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {Observable, of, ReplaySubject } from 'rxjs';
import { delay, take, tap } from 'rxjs/operators';
import { DialogService } from 'src/app/core-nlp/dialog.service';
import { FaqDefinition } from '../../common/model/faq-definition';
import {isDocked, isDockedOrSmall, ViewMode} from '../../common/model/view-mode';
import { FaqDefinitionService } from '../../common/faq-definition.service';
import { truncate } from '../../common/util/string-utils';
import { ConfirmDialogComponent } from 'src/app/shared-nlp/confirm-dialog/confirm-dialog.component';
import { NbToastrService } from "@nebular/theme/components/toastr/toastr.service";
import { StateService } from "src/app/core-nlp/state.service";

@Component({
  selector: 'tock-faq-grid-item',
  templateUrl: './faq-grid-item.component.html',
  styleUrls: ['./faq-grid-item.component.scss'],
  host: {'class': 'd-block mb-3'}
})
export class FaqGridItemComponent implements OnInit, OnDestroy {

  /* Template-available constants (vertical vs horizontal layout) */

  public readonly CSS_WIDER_CARD_BODY = 'align-items-center flex-wrap';
  public readonly CSS_SMALL_CARD_BODY = 'tock--small flex-column flex-nowrap align-items-stretch';

  public readonly CSS_WIDER_FIRST_CARD_PART = 'tock-col-a d-flex flex-column justify-content-between';
  public readonly CSS_SMALL_FIRST_CARD_PART = 'tock-col-a d-flex justify-content-between mb-3 mr-4 ml-2';

  /* Input/Output */

  @Input()
  item: FaqDefinition;

  @Input()
  viewMode: ViewMode;

  @Output()
  onRemove = new EventEmitter<boolean>();

  @Output()
  onEdit= new EventEmitter<FaqDefinition>();

  @Output()
  onDownload = new EventEmitter<boolean>();

  public cssState = "tock--opened"; // Current state style class

  private readonly destroy$: ReplaySubject<boolean> = new ReplaySubject(1);

  constructor(
    private readonly state: StateService,
    private readonly qaService: FaqDefinitionService,
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

  isDockedOrSmall(): boolean {
    return isDockedOrSmall(this.viewMode);
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

    if (result?.toLowerCase() !== 'yes') {
      return;
    }

    let done$: Observable<unknown>;
    if (newValue) {
      done$ = this.qaService.activate(this.item, this.destroy$);
    } else {
      done$ = this.qaService.disable(this.item, this.destroy$);
    }

    await done$.pipe(take(1)).toPromise();

    this.item.enabled = newValue; // update visible state for user
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
    this.cssState = 'tock--closed';

    return of(true)
      .pipe(
        delay(800),
        tap(_ =>  this.cssState = 'tock--hidden' )
      );
  }
}
