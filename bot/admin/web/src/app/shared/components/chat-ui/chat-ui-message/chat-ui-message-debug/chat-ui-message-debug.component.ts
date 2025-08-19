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

import { Component, Input } from '@angular/core';
import { NbDialogService } from '@nebular/theme';
import { Debug, RagAnswerStatus, RagAnswerStatusLabels } from '../../../../model/dialog-data';
import { DebugViewerDialogComponent } from '../../../debug-viewer-dialog/debug-viewer-dialog.component';
import { getContrastYIQ, getInterpolatedColor } from '../../../../utils';

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

  getStatusClassName(): string {
    const status = this.message.data.answer?.status;
    if (!status) {
      return '';
    }

    switch (status.toLowerCase()) {
      case RagAnswerStatus.FOUND_IN_CONTEXT:
        return 'status-success';
      case RagAnswerStatus.NOT_FOUND_IN_CONTEXT:
        return 'status-warning';
      case RagAnswerStatus.SMALL_TALK:
        return 'status-info';
      case RagAnswerStatus.OUT_OF_SCOPE:
        return 'status-danger';
      default:
        return '';
    }
  }

  getStatusLabel(): string {
    const status = this.message.data.answer?.status;
    if (status) {
      return RagAnswerStatusLabels[status.toLowerCase()] || status.replace(/_/g, ' ').replace(/^(.)|\s+(.)/g, (c) => c.toUpperCase());
    }
    return '';
  }

  getConfidenceBgColor(): { bg: string; fg: string } {
    const score = this.message.data.answer?.confidence_score;
    if (score != null) {
      const bg = getInterpolatedColor(score);
      const fg = getContrastYIQ(bg);
      return { bg, fg };
    }
    return { bg: '', fg: '' };
  }
}
