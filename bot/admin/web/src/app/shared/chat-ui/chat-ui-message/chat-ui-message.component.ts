import { Component, HostBinding, Input } from '@angular/core';
import { DomSanitizer, SafeStyle } from '@angular/platform-browser';

@Component({
  selector: 'tock-chat-ui-message',
  templateUrl: './chat-ui-message.component.html',
  styleUrls: ['./chat-ui-message.component.scss']
})
export class ChatUiMessageComponent {
  @Input() message: string;
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

  constructor(protected domSanitizer: DomSanitizer) {}
}
