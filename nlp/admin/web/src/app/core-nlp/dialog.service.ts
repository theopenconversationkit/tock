/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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

import {ChangeDetectorRef, Injectable, TemplateRef} from "@angular/core";
import {MatSnackBar, MatSnackBarConfig, MatSnackBarRef, SimpleSnackBar} from "@angular/material/snack-bar";
import {MatDialog, MatDialogConfig, MatDialogRef} from "@angular/material/dialog";
import {ComponentType} from "@angular/cdk/overlay";

@Injectable()
export class DialogService {

  private changeDetectorRef: ChangeDetectorRef;

  constructor(private snackBar: MatSnackBar) {
  }

  private doFreeze() {
    this.changeDetectorRef.detach();
  }

  private undoFreeze() {
    this.changeDetectorRef.reattach();
  }

  setupRootChangeDetector(changeDetectorRef: ChangeDetectorRef) {
    this.changeDetectorRef = changeDetectorRef;
  }

  /**
   * Opens a snackbar with a message and an optional action.
   * @param message The message to show in the snackbar.
   * @param action The label for the snackbar action.
   * @param config Additional configuration options for the snackbar.
   */
  notify(message: string, action?: string, config?: MatSnackBarConfig): MatSnackBarRef<SimpleSnackBar> {
    return this.snackBar.open(message, action ? action : "Error", config ? config : {duration: 3000});
  }

  /**
   * Opens a modal dialog containing the given component.
   *
   * @param scopedDialog the original dialog
   * @param componentOrTemplateRef Type of the component to load into the dialog,
   *     or a TemplateRef to instantiate as the dialog content.
   * @param config Extra configuration options.
   * @returns Reference to the newly-opened dialog.
   */
  open<T, D = any, R = any>(scopedDialog: MatDialog, componentOrTemplateRef: ComponentType<T> | TemplateRef<T>, config?: MatDialogConfig<D>): MatDialogRef<T, R> {
    this.doFreeze();
    const d = scopedDialog.open(componentOrTemplateRef, config);
    d.beforeClosed().subscribe(_ => this.undoFreeze());
    return d;
  }


}
