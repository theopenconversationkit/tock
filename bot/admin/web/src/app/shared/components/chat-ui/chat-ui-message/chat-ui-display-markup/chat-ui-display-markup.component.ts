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

import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Sentence, SentenceWithFootnotes } from '../../../../model/dialog-data';

import DOMPurify from 'dompurify';
import { markedParser } from '../../../../utils/markup.utils';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

DOMPurify.addHook('afterSanitizeAttributes', (node) => {
  if (node.tagName === 'A') {
    node.setAttribute('rel', 'noreferrer');
    node.setAttribute('target', '_blank');
  }
});

@Component({
  selector: 'tock-chat-ui-display-markup',
  templateUrl: './chat-ui-display-markup.component.html',
  styleUrl: './chat-ui-display-markup.component.scss'
})
export class ChatUiDisplayMarkupComponent implements OnChanges {
  @Input() sentence: Sentence | SentenceWithFootnotes;
  @Input() formatting: boolean = true;

  markup: SafeHtml;

  constructor(private sanitizer: DomSanitizer) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes.formatting && changes.formatting.currentValue) {
      this.markup = this.sanitizer.bypassSecurityTrustHtml(this.getMarkup(this.sentence.text));
    }
  }

  getMarkup(str: string): string {
    return markedParser.parse(str) as string;
  }
}
