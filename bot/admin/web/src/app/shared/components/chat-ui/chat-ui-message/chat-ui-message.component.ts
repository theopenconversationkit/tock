import { Component, EventEmitter, HostBinding, Input, Output } from '@angular/core';
import { DomSanitizer, SafeStyle } from '@angular/platform-browser';
import { BotMessage } from '../../../model/dialog-data';

@Component({
  selector: 'tock-chat-ui-message',
  templateUrl: './chat-ui-message.component.html',
  styleUrls: ['./chat-ui-message.component.scss']
})
export class ChatUiMessageComponent {
  @Input() message: BotMessage;
  @Input() replay: boolean = false;
  @Input() sender: string;
  @Input() date: Date;

  @Input()
  set avatar(value: string) {
    this.avatarStyle = value ? this.domSanitizer.bypassSecurityTrustStyle(`url(${value})`) : null;
  }

  avatarStyle: SafeStyle;

  @HostBinding('class.not-reply')
  get notReply() {
    return !this.reply;
  }

  @Input()
  @HostBinding('class.reply')
  get reply(): boolean {
    return this._reply;
  }
  set reply(value: boolean) {
    this._reply = value;
  }
  protected _reply: boolean = false;

  @Output() sendMessage: EventEmitter<BotMessage> = new EventEmitter();

  constructor(protected domSanitizer: DomSanitizer) {}

  replyMessage(message: BotMessage) {
    this.sendMessage.emit(message);
  }
}
