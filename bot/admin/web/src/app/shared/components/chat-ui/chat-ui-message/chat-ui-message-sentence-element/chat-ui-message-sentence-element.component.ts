import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { BotMessage, SentenceElement } from '../../../../model/dialog-data';
import linkifyHtml from 'linkify-html';
@Component({
  selector: 'tock-chat-ui-message-sentence-element',
  templateUrl: './chat-ui-message-sentence-element.component.html',
  styleUrls: ['./chat-ui-message-sentence-element.component.scss']
})
export class ChatUiMessageSentenceElementComponent {
  @Input() element: SentenceElement;

  @Input() replay: boolean;

  @Input() reply: boolean = false;

  @Output() sendMessage: EventEmitter<BotMessage> = new EventEmitter();

  linkifyHtml(str) {
    return linkifyHtml(str, { target: '_blank' });
  }

  replyMessage(message: BotMessage) {
    this.sendMessage.emit(message);
  }
}
