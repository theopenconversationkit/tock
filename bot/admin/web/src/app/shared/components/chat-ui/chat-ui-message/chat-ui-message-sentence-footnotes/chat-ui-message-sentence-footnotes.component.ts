import { Component, Input } from '@angular/core';
import { SentenceWithFootnotes } from '../../../../model/dialog-data';
import { sanitizeURLSync } from 'url-sanitizer';

@Component({
  selector: 'tock-chat-ui-message-sentence-footnotes',
  templateUrl: './chat-ui-message-sentence-footnotes.component.html',
  styleUrls: ['./chat-ui-message-sentence-footnotes.component.scss']
})
export class ChatUiMessageSentenceFootnotesComponent {
  @Input() sentence: SentenceWithFootnotes;

  @Input() reply: boolean = false;

  @Input() formatting: boolean = true;

  isClamped(el): boolean {
    return el.offsetHeight < el.scrollHeight;
  }

  sanitizeUrl(url: string): string | null {
    return sanitizeURLSync(url);
  }
}
