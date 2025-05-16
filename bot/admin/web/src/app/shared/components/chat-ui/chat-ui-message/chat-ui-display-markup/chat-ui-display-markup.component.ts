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

import { Component, Input, SimpleChanges } from '@angular/core';
import { Sentence, SentenceWithFootnotes } from '../../../../model/dialog-data';

import { Marked } from 'marked';
import DOMPurify from 'dompurify';
import hljs from 'highlight.js';
import { markedHighlight } from 'marked-highlight';
import { katexBlockExtension, katexInlineExtension } from '../../../../utils/markup.utils';
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
export class ChatUiDisplayMarkupComponent {
  @Input() sentence: Sentence | SentenceWithFootnotes;
  @Input() formatting: boolean = true;

  marked = new Marked({
    async: false,
    ...markedHighlight({
      emptyLangClass: 'hljs',
      langPrefix: 'hljs language-',
      highlight(code, lang, info) {
        const language = hljs.getLanguage(lang) ? lang : 'plaintext';
        return hljs.highlight(code, { language }).value;
      }
    }),
    hooks: {
      postprocess: (html) => DOMPurify.sanitize(html)
    },
    extensions: [katexBlockExtension, katexInlineExtension],
    breaks: true,
    gfm: true
  });

  markup: SafeHtml;

  constructor(private sanitizer: DomSanitizer) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes.formatting && changes.formatting.currentValue) {
      this.markup = this.sanitizer.bypassSecurityTrustHtml(this.getMarkup(this.sentence.text));
    }
  }

  getMarkup(str: string): string {
    return this.marked.parse(str) as string;
  }
}
