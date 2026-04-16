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
import { Footnote, SentenceWithFootnotes } from '../../../../model/dialog-data';
import { sanitizeURLSync } from 'url-sanitizer';
import { ResilientDatePipe } from '../../../../pipes/resilient-date.pipe';
import { NbDialogService } from '@nebular/theme';
import { JsonViewerDialogComponent } from '../../../json-viewer-dialog/json-viewer-dialog.component';

@Component({
  selector: 'tock-chat-ui-message-sentence-footnotes',
  templateUrl: './chat-ui-message-sentence-footnotes.component.html',
  styleUrls: ['./chat-ui-message-sentence-footnotes.component.scss']
})
export class ChatUiMessageSentenceFootnotesComponent {
  @Input() sentence: SentenceWithFootnotes;

  @Input() reply: boolean = false;

  @Input() formatting: boolean = true;

  constructor(private resilientDatePipe: ResilientDatePipe, private nbDialogService: NbDialogService) {}

  isClamped(el): boolean {
    return el.offsetHeight < el.scrollHeight;
  }

  sanitizeUrl(url: string): string | null {
    return sanitizeURLSync(url);
  }

  getFootnoteTooltip(footnote: Footnote): string {
    const tooltip = [];
    if (footnote.score) tooltip.push(`Compressor score : ${footnote.score.toFixed(2)}`);
    if (footnote.metadata?.index_datetime) {
      const dateFormat = 'y/MM/dd HH:mm';
      const ingestionDate = this.resilientDatePipe.transform(footnote.metadata?.index_datetime, dateFormat);
      tooltip.push(`Ingestion date : ${ingestionDate}`);
    }

    if (tooltip.length === 0) {
      if (footnote.title) {
        tooltip.push(`Title : ${footnote.title}`);
      } else {
        return 'No additional information';
      }
    }

    return tooltip.join(' | ');
  }

  displayFootnoteDetails(footnote: Footnote): void {
    this.nbDialogService.open(JsonViewerDialogComponent, {
      context: {
        title: 'Footnote details',
        data: footnote,
        customOrder: ['title', 'url', 'score', 'metadata', 'content']
      }
    });
  }
}
