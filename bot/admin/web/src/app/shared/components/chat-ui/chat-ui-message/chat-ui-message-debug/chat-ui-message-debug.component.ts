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

import { Component, Input, OnInit } from '@angular/core';
import { NbDialogService } from '@nebular/theme';
import { Debug } from '../../../../model/dialog-data';
import { DebugViewerDialogComponent } from '../../../debug-viewer-dialog/debug-viewer-dialog.component';

@Component({
  selector: 'tock-chat-ui-message-debug',
  templateUrl: './chat-ui-message-debug.component.html',
  styleUrls: ['./chat-ui-message-debug.component.scss']
})
export class ChatUiMessageDebugComponent {
  @Input() message: Debug;
  constructor(private nbDialogService: NbDialogService) {}

  showDebug() {
    this.nbDialogService.open(DebugViewerDialogComponent, {
      context: {
        debug: this.message.data
      }
    });
  }
}
