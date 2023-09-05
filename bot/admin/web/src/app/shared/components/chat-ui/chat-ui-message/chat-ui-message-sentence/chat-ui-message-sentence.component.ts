import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { BotMessage, Sentence } from '../../../../model/dialog-data';
import linkifyHtml from 'linkify-html';

@Component({
  selector: 'tock-chat-ui-message-sentence',
  templateUrl: './chat-ui-message-sentence.component.html',
  styleUrls: ['./chat-ui-message-sentence.component.scss']
})
export class ChatUiMessageSentenceComponent {
  @Input() sentence: Sentence;

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
