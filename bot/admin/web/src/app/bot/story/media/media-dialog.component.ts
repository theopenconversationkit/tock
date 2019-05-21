/*
 * Copyright (C) 2017/2019 VSCT
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
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {MediaAction, MediaCard} from "../../model/story";
import {CreateI18nLabelRequest} from "../../model/i18n";
import {BotService} from "../../bot-service";
import {StateService} from "../../../core-nlp/state.service";

@Component({
  selector: 'tock-media-dialog',
  templateUrl: './media-dialog.component.html',
  styleUrls: ['./media-dialog.component.css']
})
export class MediaDialogComponent {

  media: MediaCard;
  create: boolean;
  category: string;

  @ViewChild('titleElement') titleElement: ElementRef;

  constructor(
    public dialogRef: MatDialogRef<MediaDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private state: StateService,
    private bot: BotService) {
    this.category = this.data.category ? this.data.category : "build";
    this.create = this.data.media === null;
    this.media = this.data.media ? this.data.media : new MediaCard([]);
    if (this.media.title) {
      this.media.titleLabel = this.media.title.defaultLocalizedLabel().label;
    }
    if (this.media.subTitle) {
      this.media.subTitleLabel = this.media.subTitle.defaultLocalizedLabel().label;
    }
    if (!this.media.imageUrl) {
      this.media.imageUrl = "";
    }
    this.media.actions.forEach(a => a.titleLabel = a.title.defaultLocalizedLabel().label);

    setTimeout(() => this.titleElement.nativeElement.focus(), 500);
  }

  save() {
    if (this.media.titleLabel && this.media.titleLabel.trim().length !== 0) {
      if (this.media.title) {
        this.media.title.defaultLocalizedLabel().label = this.media.titleLabel.trim();
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

    if (this.media.subTitleLabel && this.media.subTitleLabel.trim().length !== 0) {
      if (this.media.subTitle) {
        this.media.subTitle.defaultLocalizedLabel().label = this.media.subTitleLabel.trim();
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

    if (!this.media.imageUrl || this.media.imageUrl.trim().length === 0) {
      this.media.imageUrl = null;
    }

    this.media.actions = this.media.actions
      .filter(a => a.titleLabel && a.titleLabel.trim().length !== 0)
      .map(a => {
        if (a.title) {
          a.title.defaultLocalizedLabel().label = a.titleLabel.trim();
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
