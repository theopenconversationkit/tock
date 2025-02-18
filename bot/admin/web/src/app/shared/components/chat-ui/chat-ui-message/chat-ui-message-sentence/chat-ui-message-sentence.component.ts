import { Component, EventEmitter, Input, Output } from '@angular/core';
import { BotMessage, Sentence } from '../../../../model/dialog-data';

@Component({
  selector: 'tock-chat-ui-message-sentence',
  templateUrl: './chat-ui-message-sentence.component.html',
  styleUrls: ['./chat-ui-message-sentence.component.scss']
})
export class ChatUiMessageSentenceComponent {
  @Input() sentence: Sentence;

  @Input() replay: boolean;

  @Input() reply: boolean = false;

  @Input() formatting: boolean = true;

  @Output() sendMessage: EventEmitter<BotMessage> = new EventEmitter();

  replyMessage(message: BotMessage) {
    this.sendMessage.emit(message);
  }
}
