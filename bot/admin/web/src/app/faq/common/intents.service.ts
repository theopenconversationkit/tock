import {Injectable} from '@angular/core';
import {StateService} from "../../core-nlp/state.service";
import {Intent} from "../../model/nlp";
import {DialogService} from "../../core-nlp/dialog.service";
import {NlpService} from "../../nlp-tabs/nlp.service";
import {map, take, takeUntil, tap} from 'rxjs/operators';
import {empty, Observable} from 'rxjs';
import {IntentDialogComponent} from "../../sentence-analysis/intent-dialog/intent-dialog.component";
import {ConfirmDialogComponent} from "../../shared-nlp/confirm-dialog/confirm-dialog.component";

/**
 * TODO: Makes it a common service
 */

const UNQUALIFIED_UNKNOWN_NAME = Intent.unknown.split(":")[1];

@Injectable()
export class IntentsService {

  constructor(
    private readonly state: StateService,
    private readonly nlp: NlpService,
    private readonly dialog: DialogService
  ) {
  }

  /**
   * Try to creates new Intent
   *
   * Note: User has ability to cancel the save
   * @param cancel$
   */
  public async newIntent(cancel$: Observable<any> = empty()): Promise<Intent> {

    // ask user
    const dialogRef = this.dialog.openDialog(IntentDialogComponent, {context: {create: true}});
    const result = await dialogRef.onClose
      .pipe(takeUntil(cancel$), take(1))
      .toPromise();

    if (!result?.name || !(await this.canSaveIntent(result.name, result.label, result.description, result.category))) {
      console.log("reject");
      return Promise.reject("cancelled");
    }

    // save
    return await this.saveIntent(result.name, result.label, result.description, result.category)
      .pipe(takeUntil(cancel$), take(1))
      .toPromise();
  }

  private canSaveIntent(name: string, label: string, description: string, category: string): Promise<boolean> {
    if (StateService.intentExistsInApp(this.state.currentApplication, name) || name === UNQUALIFIED_UNKNOWN_NAME) {
      console.log("intentExistsInApp");
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
