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

import { Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';
import { NbDialogRef, NbToastrService } from '@nebular/theme';
import { FileItem, FileUploader, ParsedResponseHeaders } from 'ng2-file-upload';

import { MediaAction, MediaCard, MediaFile } from '../../model/story';
import { CreateI18nLabelRequest } from '../../model/i18n';
import { BotService } from '../../bot-service';
import { StateService } from '../../../core-nlp/state.service';
import { RestService } from '../../../core-nlp/rest/rest.service';

@Component({
  selector: 'tock-media-dialog',
  templateUrl: './media-dialog.component.html',
  styleUrls: ['./media-dialog.component.scss']
})
export class MediaDialogComponent implements OnInit {
  @Input() media: MediaCard;
  @Input() category: string;

  create: boolean;
  fileUpload: string = 'upload';
  fileExternalUrl: string;
  uploader: FileUploader;

  @ViewChild('titleElement') titleElement: ElementRef;
  private loading: boolean;

  private loadingSubtitle: boolean;

  private loadingDescription: boolean;

  constructor(
    public dialogRef: NbDialogRef<MediaDialogComponent>,
    public restService: RestService,
    private stateService: StateService,
    private toastrService: NbToastrService,
    private botService: BotService
  ) {}
  ngOnInit(): void {
    this.category = this.category ? this.category : 'build';
    this.create = this.media === null;
    this.media = this.media ? this.media : new MediaCard([], null, null, null, true);
    if (this.media.title) {
      this.media.titleLabel = this.media.title.defaultLocalizedLabelForLocale(this.stateService.currentLocale).label;
    }
    if (this.media.subTitle) {
      this.media.subTitleLabel = this.media.subTitle.defaultLocalizedLabelForLocale(this.stateService.currentLocale).label;
    }
    if (this.media.file && this.media.file.description) {
      this.media.file.descriptionLabel = this.media.file.description.defaultLocalizedLabelForLocale(this.stateService.currentLocale).label;
    }

    this.media.actions.forEach((a) => (a.titleLabel = a.title.defaultLocalizedLabelForLocale(this.stateService.currentLocale).label));

    this.uploader = new FileUploader({ url: undefined, removeAfterUpload: true, autoUpload: true });
    this.uploader.onCompleteItem = (item: FileItem, response: string, status: number, headers: ParsedResponseHeaders) => {
      this.media.file = MediaFile.fromJSON(JSON.parse(response));
    };
    this.botService.prepareFileDumpUploader(this.uploader);
  }

  private isTitle() {
    return this.media.titleLabel && this.media.titleLabel.trim().length !== 0;
  }

  private isSubtitle() {
    return this.media.subTitleLabel && this.media.subTitleLabel.trim().length !== 0;
  }

  private isDescription() {
    return this.media.file && this.media.file.descriptionLabel && this.media.file.descriptionLabel.trim().length !== 0;
  }

  private isFile() {
    return this.media.file;
  }

  fileTypeChange(type) {
    this.fileUpload = type;
    if (type === 'upload') {
      this.fileExternalUrl = null;
      this.media.file = null;
    }
  }

  checkFileName() {
    const url = this.fileExternalUrl?.trim();
    if (url && url.length !== 0 && url.indexOf('.') !== -1) {
      const suffix = url.substring(url.lastIndexOf('.') + 1);
      this.media.file = new MediaFile(suffix, url, url, MediaFile.attachmentType(suffix), url);
    } else {
      this.media.file = null;
    }
    this.fileExternalUrl = null;
  }

  save() {
    if (!this.isTitle() && !this.isSubtitle() && !this.isFile()) {
      this.toastrService.show(`Please add a Title, Subtitle or File.`, 'Media Message is not complete', {
        duration: 3000,
        status: 'warning'
      });
    } else {
      if (this.isTitle()) {
        this.loading = true;
        if (this.media.title) {
          this.botService
            .saveI18nLabel(this.media.title.changeDefaultLabelForLocale(this.stateService.currentLocale, this.media.titleLabel.trim()))
            .subscribe((_) => {
              this.loading = false;
              this.closeModal();
            });
        } else {
          this.botService
            .createI18nLabel(new CreateI18nLabelRequest(this.category, this.media.titleLabel.trim(), this.stateService.currentLocale))
            .subscribe((i18n) => {
              this.media.title = i18n;
              this.loading = false;
              this.closeModal();
            });
        }
      } else {
        this.media.title = null;
      }

      if (this.isSubtitle()) {
        this.loadingSubtitle = true;
        if (this.media.subTitle) {
          this.botService
            .saveI18nLabel(
              this.media.subTitle.changeDefaultLabelForLocale(this.stateService.currentLocale, this.media.subTitleLabel.trim())
            )
            .subscribe((_) => {
              this.loadingSubtitle = false;
              this.closeModal();
            });
        } else {
          this.botService
            .createI18nLabel(new CreateI18nLabelRequest(this.category, this.media.subTitleLabel.trim(), this.stateService.currentLocale))
            .subscribe((i18n) => {
              this.media.subTitle = i18n;
              this.loadingSubtitle = false;
              this.closeModal();
            });
        }
      } else {
        this.media.subTitle = null;
      }

      if (this.isDescription()) {
        this.loadingDescription = true;
        if (this.media.file && this.media.file.description) {
          this.botService
            .saveI18nLabel(
              this.media.file.description.changeDefaultLabelForLocale(
                this.stateService.currentLocale,
                this.media.file.descriptionLabel.trim()
              )
            )
            .subscribe((_) => {
              this.loadingDescription = false;
              this.closeModal();
            });
        } else {
          this.botService
            .createI18nLabel(
              new CreateI18nLabelRequest(this.category, this.media.file.descriptionLabel.trim(), this.stateService.currentLocale)
            )
            .subscribe((i18n) => {
              this.media.file.description = i18n;
              this.loadingDescription = false;
              this.closeModal();
            });
        }
      } else if (this.media.file) {
        this.media.file.description = null;
      }

      this.media.actions = this.media.actions
        .filter((a) => a.titleLabel && a.titleLabel.trim().length !== 0)
        .map((a) => {
          if (a.title) {
            this.botService
              .saveI18nLabel(a.title.changeDefaultLabelForLocale(this.stateService.currentLocale, a.titleLabel.trim()))
              .subscribe((_) => {});
          } else {
            this.botService
              .createI18nLabel(new CreateI18nLabelRequest(this.category, a.titleLabel.trim(), this.stateService.currentLocale))
              .subscribe((i18n) => (a.title = i18n));
          }
          return a;
        });
    }
    this.closeModal();
  }

  private closeModal() {
    if (!this.loading && !this.loadingSubtitle && !this.loadingDescription) {
      this.dialogRef.close({
        media: this.media
      });
    }
  }

  remove() {
    this.dialogRef.close({ removeMedia: true });
  }

  cancel() {
    this.dialogRef.close();
  }

  removeAction(action: MediaAction) {
    this.media.actions.splice(this.media.actions.indexOf(action), 1);
  }

  addAction() {
    const mediaAction = new MediaAction(null, null);
    this.media.actions.push(mediaAction);
  }

  downward(action: MediaAction) {
    const actions = this.media.actions;
    const i = actions.indexOf(action);
    actions[i] = actions[i + 1];
    actions[i + 1] = action;
    this.media.actions = actions.slice();
  }

  canDownward(action: MediaAction): boolean {
    const actions = this.media.actions;
    return actions.length > 1 && actions[actions.length - 1] !== action;
  }

  upward(action: MediaAction) {
    const actions = this.media.actions;
    const i = actions.indexOf(action);
    actions[i] = actions[i - 1];
    actions[i - 1] = action;
    this.media.actions = actions.slice();
  }

  canUpward(action: MediaAction): boolean {
    const actions = this.media.actions;
    return actions.length > 1 && actions[0] !== action;
  }
}
