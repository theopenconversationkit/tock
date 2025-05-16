/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

import { saveAs } from 'file-saver-es';
import { Component, EventEmitter, Input, Output } from '@angular/core';

import { FaqDefinitionExtended } from '../faq-management.component';
import { StateService } from '../../../core-nlp/state.service';
import { DialogService } from '../../../core-nlp/dialog.service';
import { copyToClipboard, getExportFileName } from '../../../shared/utils';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { ChoiceDialogComponent, IntentStoryDetailsComponent } from '../../../shared/components';
import { TestDialogService } from '../../../shared/components/test-dialog/test-dialog.service';

@Component({
  selector: 'tock-faq-management-list',
  templateUrl: './faq-management-list.component.html',
  styleUrls: ['./faq-management-list.component.scss']
})
export class FaqManagementListComponent {
  @Input() faqs!: FaqDefinitionExtended[];
  @Input() selectedFaq?: FaqDefinitionExtended;

  @Output() onEdit = new EventEmitter<FaqDefinitionExtended>();
  @Output() onDelete = new EventEmitter<FaqDefinitionExtended>();
  @Output() onEnable = new EventEmitter<FaqDefinitionExtended>();

  constructor(
    public state: StateService,
    private dialogService: DialogService,
    private toastrService: NbToastrService,
    private nbDialogService: NbDialogService,
    private testDialogService: TestDialogService
  ) {}

  getCurrentLocaleAnswerLabel(faq: FaqDefinitionExtended) {
    let localeAnswer = faq.answer.defaultLabel;

    let localeI18n = faq.answer.i18n.find((i18n) => {
      return i18n.locale === this.state.currentLocale;
    });

    if (localeI18n?.label.length) localeAnswer = localeI18n.label;

    return localeAnswer;
  }

  isCurrentLocaleAnswerLabelProvided(faq: FaqDefinitionExtended): boolean {
    let localeI18n = faq.answer.i18n.find((i18n) => {
      return i18n.locale === this.state.currentLocale;
    });

    if (localeI18n?.label?.trim().length) return true;

    return false;
  }

  toggleEnabled(faq: FaqDefinitionExtended) {
    let action = 'Enable';
    if (faq.enabled) {
      action = 'Disable';
    }

    const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
      context: {
        title: `${action} faq "${faq.title}"`,
        subtitle: `Are you sure you want to ${action.toLowerCase()} this faq ?`,
        actions: [
          { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
          { actionName: action, buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result === action.toLowerCase()) {
        this.onEnable.emit(faq);
      }
    });
  }

  editFaq(faq: FaqDefinitionExtended): void {
    this.onEdit.emit(faq);
  }

  delete(faq: FaqDefinitionExtended): void {
    const action = 'delete';
    const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
      context: {
        title: `Delete faq "${faq.title}"`,
        subtitle: 'Are you sure you want to delete this faq ?',
        actions: [
          { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
          { actionName: action, buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result === action) {
        this.onDelete.emit(faq);
      }
    });
  }

  download(faq: FaqDefinitionExtended): void {
    const jsonBlob = new Blob([JSON.stringify(faq)], {
      type: 'application/json'
    });

    const exportFileName = getExportFileName(
      this.state.currentApplication.namespace,
      this.state.currentApplication.name,
      'Faq',
      'json',
      faq.title
    );

    saveAs(jsonBlob, exportFileName);
  }

  copyString(str: string) {
    copyToClipboard(str);
    this.toastrService.success(`String copied to clipboard`, 'Clipboard');
  }

  displayStoryDetails(faq: FaqDefinitionExtended): void {
    this.nbDialogService.open(IntentStoryDetailsComponent, {
      context: {
        intentId: faq.intentId
      }
    });
  }

  testDialogSentence(message: string, locale: string) {
    this.testDialogService.testSentenceDialog({
      sentenceText: message,
      sentenceLocale: locale
    });
  }
}
