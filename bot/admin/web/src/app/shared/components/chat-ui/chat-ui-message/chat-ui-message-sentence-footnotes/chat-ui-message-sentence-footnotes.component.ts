import { Component, Input } from '@angular/core';
import linkifyHtml from 'linkify-html';
import { SentenceWithFootnotes } from '../../../../model/dialog-data';

@Component({
  selector: 'tock-chat-ui-message-sentence-footnotes',
  templateUrl: './chat-ui-message-sentence-footnotes.component.html',
  styleUrls: ['./chat-ui-message-sentence-footnotes.component.scss']
})
export class ChatUiMessageSentenceFootnotesComponent {
  @Input() sentence: SentenceWithFootnotes;

  @Input() reply: boolean = false;

  linkifyHtml(str) {
    return linkifyHtml(str, { target: '_blank' });
  }

  isClamped(el): boolean {
    return el.offsetHeight < el.scrollHeight;
  }
}
