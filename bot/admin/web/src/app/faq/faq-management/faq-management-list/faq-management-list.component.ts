import { saveAs } from 'file-saver-es';
import { Component, EventEmitter, Input, Output } from '@angular/core';

import { FaqDefinitionExtended } from '../faq-management.component';
import { StateService } from '../../../core-nlp/state.service';
import { DialogService } from '../../../core-nlp/dialog.service';
import { copyToClipboard, getExportFileName } from '../../../shared/utils';
import { NbToastrService } from '@nebular/theme';
import { ChoiceDialogComponent } from '../../../shared/components';

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

  constructor(public state: StateService, private dialogService: DialogService, private toastrService: NbToastrService) {}

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
}
