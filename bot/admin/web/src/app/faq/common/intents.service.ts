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

import {Injectable} from '@angular/core';
import {StateService} from "../../core-nlp/state.service";
import {Intent} from "../../model/nlp";
import {DialogService} from "../../core-nlp/dialog.service";
import {NlpService} from "../../nlp-tabs/nlp.service";
import {map, take, takeUntil, tap} from 'rxjs/operators';
import {empty, Observable} from 'rxjs';
import {IntentDialogComponent} from "../../sentence-analysis/intent-dialog/intent-dialog.component";
import {ConfirmDialogComponent} from "../../shared-nlp/confirm-dialog/confirm-dialog.component";

const UNQUALIFIED_UNKNOWN_NAME = Intent.unknown.split(":")[1];

/**
 * Intents operations for FAQ module
 */
@Injectable()
export class IntentsService {

  constructor(
    private readonly state: StateService,
    private readonly nlp: NlpService,
    private readonly dialog: DialogService
  ) {
  }

  /**
   * Creates new Intent
   *
   * Note: User has ability to cancel the save
   * @param cancel$ Cancellation observable
   */
  public async newIntent(cancel$: Observable<any> = empty()): Promise<Intent> {

    // ask user
    const dialogRef = this.dialog.openDialog(IntentDialogComponent, {context: {create: true}});
    const result = await dialogRef.onClose
      .pipe(takeUntil(cancel$), take(1))
      .toPromise();

    if (!result?.name || !(await this.canSaveIntent(result.name, result.label, result.description, result.category))) {
      return Promise.reject("cancelled");
    }

    // save
    return await this.saveIntent(result.name, result.label, result.description, result.category)
      .pipe(takeUntil(cancel$), take(1))
      .toPromise();
  }

  private canSaveIntent(name: string, label: string, description: string, category: string): Promise<boolean> {
    if (StateService.intentExistsInApp(this.state.currentApplication, name) || name === UNQUALIFIED_UNKNOWN_NAME) {
      this.dialog.notify(`Intent ${name} already exists`, 'Cancelled',
        {duration: 5000, status: "warning"});
      return Promise.resolve(false);
    }

    if (this.state.intentExistsInOtherApplication(name)) {
      const dialogRef = this.dialog.openDialog(ConfirmDialogComponent, {
        context: {
          title: 'This intent is already used in an other application',
          subtitle: 'If you confirm the name, the intent will be shared between the two applications.',
          action: 'Confirm'
        }
      });
      return dialogRef.onClose.pipe(
        map(res => (res === 'confirm'), take(1))
      ).toPromise();
    } else {
      return Promise.resolve(true);
    }
  }

  private saveIntent(name: string, label: string, description: string, category: string): Observable<Intent> {
    return this.nlp.saveIntent(
      new Intent(
        name,
        this.state.user.organization,
        [],
        [this.state.currentApplication._id],
        [],
        [],
        label,
        description,
        category)
    ).pipe(
      tap(this.state.addIntent.bind(this.state))
    );
  }

}
