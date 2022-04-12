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

import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { NbButtonComponent, NbWindowRef } from '@nebular/theme';
import { FileItem, FileUploader, ParsedResponseHeaders } from 'ng2-file-upload';
import { DialogService } from '../../core-nlp/dialog.service';

@Component({
  selector: 'tock-i18n-import-action',
  templateUrl: './i18n-import.component.html',
  styleUrls: ['./i18n-import.component.css']
})
export class I18nImportComponent implements OnInit, AfterViewInit {
  @ViewChild('focusElement')
  focusElement: NbButtonComponent;
  loading: boolean;
  uploader: FileUploader;
  invalidSelectedFile = false;

  constructor(private windowRef: NbWindowRef, private dialog: DialogService) {}

  ngOnInit(): void {
    const refresh = this.windowRef.config.context['refresh'];
    this.uploader = new FileUploader({
      isHTML5: true
    });
    this.uploader.onCompleteItem = (
      item: FileItem,
      response: string,
      status: number,
      headers: ParsedResponseHeaders
    ) => {
      this.loading = false;
      this.closeWindow();
      if (status === 200) {
        if (parseInt(response) > 0) {
          this.dialog.notify(
            response + ' labels have been created or updated.',
            'Labels Imported',
            { duration: 5000, status: 'success' }
          );
        } else {
          this.dialog.notify(
            'No label created or updated: file might be empty or no label is validated.',
            'No Label Imported',
            { duration: 5000, status: 'warning' }
          );
        }
      }
      refresh();
    };
  }

  ngAfterViewInit(): void {
    const focusElement = this.focusElement['hostElement']?.nativeElement;
    if (focusElement) {
      focusElement.focus();
    }
  }

  import(): void {
    this.loading = true;
    const importFrom = this.windowRef.config.context['importFrom'];
    if (!this.invalidSelectedFile && importFrom) {
      importFrom(this.getFileType(), this.uploader);
    }
  }

  private getFileType() {
    return this.uploader.queue.every((fileItem) => fileItem.file.type.toLowerCase() === 'text/csv')
      ? 'CSV'
      : 'JSON';
  }

  closeWindow(): void {
    this.windowRef.close();
  }

  onChange(changeEvent: Event) {
    const files: File[] = changeEvent.target['files'];
    if (files.length > 0) {
      const fileType = files[0].type.toLowerCase();
      if (fileType.indexOf('csv') !== -1 || fileType.indexOf('json') !== -1) {
        this.uploader.clearQueue();
        this.uploader.addToQueue(files);
        this.invalidSelectedFile = false;
      } else {
        this.invalidSelectedFile = true;
      }
    }
  }
}
