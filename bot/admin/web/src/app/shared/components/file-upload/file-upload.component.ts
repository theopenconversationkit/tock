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

import { Component, ElementRef, forwardRef, HostListener, Input, OnDestroy, OnInit } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { Subscription } from 'rxjs';

@Component({
  selector: 'tock-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => FileUploadComponent),
      multi: true
    }
  ]
})
export class FileUploadComponent implements OnInit, OnDestroy, ControlValueAccessor {
  @Input() autofocus: boolean = false;
  @Input() currentFile?: File[];
  @Input() disabled: boolean = false;
  @Input() fullWidth: boolean = false;
  @Input() multiple: boolean = false;
  @Input() fileTypeAccepted?: string[];
  @Input() filesInError?: string[];

  public fileTypes: string = '';
  public isFilesContainerHover: boolean = false;

  private _files: File[] = [];
  private onChange: Function = () => {};
  private onTouch: Function = () => {};
  private subscriptions = new Subscription();

  get files(): File[] {
    return this._files;
  }

  set files(f: File[]) {
    if (f.length) {
      this._files = this.multiple ? this.concatFilesWhithoutDuplicate(this.files, f) : f;
      this.onChange(this._files);
      this.onTouch(this._files);
    }
  }

  constructor(private host: ElementRef<HTMLInputElement>) {}

  ngOnInit(): void {
    if (this.currentFile?.length) {
      this.files = [...this.currentFile];
    }

    if (this.fileTypeAccepted?.length) {
      this.fileTypes = this.fileTypeAccepted.join(', ');
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  @HostListener('change', ['$event.target.files']) emitFiles(fileList: FileList) {
    const files = this.getFiles(fileList);

    this.files = files;
  }

  @HostListener('dragover', ['$event']) onDragOver(event: DragEvent) {
    if (!this.disabled) {
      this.preventDefault(event);
      this.isFilesContainerHover = true;
    }
  }

  @HostListener('dragleave', ['$event']) onDragLeave(event: DragEvent) {
    if (!this.disabled) {
      this.preventDefault(event);
      this.isFilesContainerHover = false;
    }
  }

  @HostListener('drop', ['$event']) onDrop(event: DragEvent) {
    if (!this.disabled) {
      this.preventDefault(event);
      this.isFilesContainerHover = false;
      const fileList = event.dataTransfer.files;
      this.emitFiles(fileList);
    }
  }

  private getFiles(fileList: FileList): File[] {
    const files: File[] = [];

    if (!this.multiple && fileList.item(0)) {
      files.push(fileList.item(0));

      return files;
    }

    for (let i = 0; i < fileList.length; i++) {
      files.push(fileList.item(i));
    }

    return files;
  }

  private concatFilesWhithoutDuplicate(arr1: File[], arr2: File[]): File[] {
    return Array.from([...arr1, ...arr2].reduce((a, o) => a.set(o.name, o), new Map()).values());
  }

  removeFile(name: string): void {
    const index = this._files.findIndex((f: File) => f.name === name);

    if (index !== undefined) {
      this._files.splice(index, 1);
      this.onChange(this._files);
      this.onTouch(this._files);
    }
  }

  wrongType(file: File): boolean {
    if (this.fileTypeAccepted) {
      for (let f of this.fileTypeAccepted) {
        switch (f) {
          case 'json':
            if (file.type === 'application/json') return false;
        }
      }
    }

    return true;
  }

  wrongFormat(file: File): boolean {
    return this.filesInError?.includes(file.name);
  }

  writeValue(files: File[]): void {
    this.host.nativeElement.value = '';
    this._files = files;
    this.onChange(this._files);
    this.onTouch(this._files);
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouch = fn;
  }

  preventDefault(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
  }
}
