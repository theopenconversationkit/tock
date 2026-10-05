import { saveAs } from 'file-saver-es';
import { Component, EventEmitter, Input, Output } from '@angular/core';

import { FaqDefinitionExtended } from '../faq-management.component';
import { StateService } from '../../../core-nlp/state.service';
import { DialogService } from '../../../core-nlp/dialog.service';
import { copyToClipboard, getExportFileName } from '../../../shared/utils';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { ChoiceDialogComponent, IntentStoryDetailsComponent } from '../../../shared/components';
import { TestDialogService } from '../../../shared/components/test-dialog/test-dialog.service';
import { TranslocoService } from '@jsverse/transloco';

@Component({
    selector: 'tock-faq-management-list',
    templateUrl: './faq-management-list.component.html',
    styleUrls: ['./faq-management-list.component.scss'],
    standalone: false
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
    private testDialogService: TestDialogService,
    private transloco: TranslocoService
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

    return !!localeI18n?.label?.trim().length;
  }

  toggleEnabled(faq: FaqDefinitionExtended) {
    const actionLabel = faq.enabled
      ? this.transloco.translate('faq.faq-management-list.disableAction')
      : this.transloco.translate('faq.faq-management-list.enableAction');

    const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
      context: {
        title: this.transloco.translate('faq.faq-management-list.toggleFaqTitle', { action: actionLabel, title: faq.title }),
        subtitle: this.transloco.translate('faq.faq-management-list.toggleFaqSubtitle', { action: actionLabel.toLowerCase() }),
        actions: [
          { actionName: this.transloco.translate('common.actions.cancel'), buttonStatus: 'basic', ghost: true },
          { actionName: actionLabel, buttonStatus: 'danger' }
        ]
      }
    });

    dialogRef.onClose.subscribe((result) => {
      if (result.toLowerCase() === actionLabel.toLowerCase()) {
        this.onEnable.emit(faq);
      }
    });
  }

  editFaq(faq: FaqDefinitionExtended): void {
    this.onEdit.emit(faq);
  }

  delete(faq: FaqDefinitionExtended): void {
    const action = this.transloco.translate('faq.faq-management-list.deleteAction');
    const dialogRef = this.dialogService.openDialog(ChoiceDialogComponent, {
      context: {
        title: this.transloco.translate('faq.faq-management-list.deleteFaqTitle', { title: faq.title }),
        subtitle: this.transloco.translate('faq.faq-management-list.deleteFaqSubtitle'),
        actions: [
          { actionName: this.transloco.translate('common.actions.cancel'), buttonStatus: 'basic', ghost: true },
          { actionName: this.transloco.translate('faq.faq-management-list.deleteAction'), buttonStatus: 'danger' }
        ]
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result.toLowerCase() === action.toLowerCase()) {
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
    this.toastrService.success(
      this.transloco.translate('common.messages.stringCopiedToClipboard'),
      this.transloco.translate('common.messages.clipboard')
    );
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
