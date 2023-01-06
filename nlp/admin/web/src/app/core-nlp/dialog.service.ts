/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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
import { ComponentType } from '@angular/cdk/overlay';
import { Injectable, TemplateRef, Type } from '@angular/core';
import { MatDialog, MatDialogConfig, MatDialogRef } from '@angular/material/dialog';
import { NbDialogConfig, NbDialogRef, NbDialogService, NbToastrConfig, NbToastrService } from '@nebular/theme';

@Injectable()
export class DialogService {
  constructor(private toastrService: NbToastrService, private nbDialogService: NbDialogService) {}

  /**
   * Opens a snackbar with a message and an optional action.
   * @param message The message to show in the snackbar.
   * @param action The label for the snackbar action.
   * @param config Additional configuration options for the snackbar.
   */
  notify(message: string, action?: string, config?: Partial<NbToastrConfig>) {
    this.toastrService.show(message, action ? action : 'Error', config ? config : { duration: 3000 });
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
  open<T, D = any, R = any>(
    scopedDialog: MatDialog,
    componentOrTemplateRef: ComponentType<T> | TemplateRef<T>,
    config?: MatDialogConfig<D>
  ): MatDialogRef<T, R> {
    return scopedDialog.open(componentOrTemplateRef, config);
  }

  /**
   * Opens a modal dialog containing the given component.
   *
   * @returns Reference to the newly-opened dialog.
   */
  openDialog<T, D = any, R = any>(
    content: Type<T> | TemplateRef<T>,
    userConfig?: Partial<NbDialogConfig<Partial<T> | string>>
  ): NbDialogRef<T> {
    return this.nbDialogService.open(content, userConfig);
  }
}
