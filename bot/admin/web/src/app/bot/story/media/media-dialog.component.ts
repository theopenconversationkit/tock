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

import {Component, ElementRef, Inject, ViewChild} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {MediaAction, MediaCard, MediaFile} from "../../model/story";
import {CreateI18nLabelRequest} from "../../model/i18n";
import {BotService} from "../../bot-service";
import {StateService} from "../../../core-nlp/state.service";
import {FileItem, FileUploader, ParsedResponseHeaders} from "ng2-file-upload";
import {RestService} from "../../../core-nlp/rest/rest.service";
import {NbToastrService} from '@nebular/theme';

@Component({
  selector: 'tock-media-dialog',
  templateUrl: './media-dialog.component.html',
  styleUrls: ['./media-dialog.component.css']
})
export class MediaDialogComponent {

  media: MediaCard;
  create: boolean;
  category: string;
  fileUpload: string = "upload";
  fileExternalUrl: string;

  uploader: FileUploader;

  @ViewChild('titleElement') titleElement: ElementRef;

  constructor(
    public dialogRef: MatDialogRef<MediaDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public rest: RestService,
    private state: StateService,
    private toastrService: NbToastrService,
    private bot: BotService) {
    this.category = this.data.category ? this.data.category : "build";
    this.create = this.data.media === null;
    this.media = this.data.media ? this.data.media : new MediaCard([], null, null, null, true);
    if (this.media.title) {
      this.media.titleLabel = this.media.title.defaultLocalizedLabelForLocale(this.state.currentLocale).label;
    }
    if (this.media.subTitle) {
      this.media.subTitleLabel = this.media.subTitle.defaultLocalizedLabelForLocale(this.state.currentLocale).label;
    }

    this.media.actions.forEach(a => a.titleLabel = a.title.defaultLocalizedLabelForLocale(this.state.currentLocale).label);

    this.uploader = new FileUploader({removeAfterUpload: true, autoUpload: true});
    this.uploader.onCompleteItem =
      (item: FileItem, response: string, status: number, headers: ParsedResponseHeaders) => {
        this.media.file = MediaFile.fromJSON(JSON.parse(response));
      };
    this.bot.prepareFileDumpUploader(this.uploader);

    setTimeout(() => this.titleElement.nativeElement.focus(), 500);
  }

  private isTitle() {
    return this.media.titleLabel && this.media.titleLabel.trim().length !== 0;
  }

  private isSubtitle() {
    return this.media.subTitleLabel && this.media.subTitleLabel.trim().length !== 0;
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
      this.toastrService.show(`Please add a Title, Subtitle or File.`, "Media Message is not complete", {
        duration: 3000,
        status: "warning"
      });

    } else {
      if (this.isTitle()) {
        if (this.media.title) {
          this.bot.saveI18nLabel(
            this.media.title.changeDefaultLabelForLocale(this.state.currentLocale, this.media.titleLabel.trim())
          ).subscribe(_ => {
          });
        } else {
          this.bot.createI18nLabel(
            new CreateI18nLabelRequest(
              this.category,
              this.media.titleLabel.trim(),
              this.state.currentLocale,
            )
          ).subscribe(i18n => this.media.title = i18n);
        }
      } else {
        this.media.title = null
      }

      if (this.isSubtitle()) {
        if (this.media.subTitle) {
          this.bot.saveI18nLabel(
            this.media.subTitle.changeDefaultLabelForLocale(this.state.currentLocale, this.media.subTitleLabel.trim())
          ).subscribe(_ => {
          });
        } else {
          this.bot.createI18nLabel(
            new CreateI18nLabelRequest(
              this.category,
              this.media.subTitleLabel.trim(),
              this.state.currentLocale,
            )
          ).subscribe(i18n => this.media.subTitle = i18n);
        }
      } else {
        this.media.subTitle = null
      }

      this.media.actions = this.media.actions
        .filter(a => a.titleLabel && a.titleLabel.trim().length !== 0)
        .map(a => {
          if (a.title) {
            this.bot.saveI18nLabel(
              a.title.changeDefaultLabelForLocale(this.state.currentLocale, a.titleLabel.trim())
            ).subscribe(_ => {
            });
          } else {
            this.bot.createI18nLabel(
              new CreateI18nLabelRequest(
                this.category,
                a.titleLabel.trim(),
                this.state.currentLocale,
              )
            ).subscribe(i18n => a.title = i18n);
          }
          return a
        });

      this.dialogRef.close({
        media: this.media
      });
    }
  }

  remove() {
    this.dialogRef.close({removeMedia: true});
  }

  removeAction(action: MediaAction) {
    this.media.actions.splice(this.media.actions.indexOf(action), 1);
  }

  addAction() {
    const mediaAction = new MediaAction(null, null);
    this.media.actions.push(mediaAction);
  }
}
