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

import { Component, Input } from '@angular/core';
import { Attachment } from '../model/dialog-data';
@Component({
  selector: 'tock-bot-message-attachment',
  template: `<img
      *ngIf="attachment.isImage()"
      [src]="attachment.url"
      width="191"
      height="100"
    />
    <div *ngIf="!attachment.isImage()"><a [href]="attachment.url">(file)</a></div>`
})
export class BotMessageAttachmentComponent {
  @Input()
  attachment: Attachment;
}
