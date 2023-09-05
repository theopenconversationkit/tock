import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { BotMessage, Choice } from '../../../../model/dialog-data';

@Component({
  selector: 'tock-chat-ui-message-choice',
  templateUrl: './chat-ui-message-choice.component.html',
  styleUrls: ['./chat-ui-message-choice.component.scss']
})
export class ChatUiMessageChoiceComponent {
  @Input() choice: Choice;

  @Input() replay: boolean;

  @Input() reply: boolean;

  @Output()
  replyMessage: EventEmitter<BotMessage> = new EventEmitter();

  title(): String {
    return this.choice.parameters.get('_title');
  }

  url(): String {
    return this.choice.parameters.get('_url');
  }

  click() {
    if (this.url()) {
      this.redirect();
      //TODO emit redirect event
    } else {
      this.replyMessage.emit(new Choice(0, this.choice.intentName, this.choice.parameters));
    }
  }

  redirect() {
    window.open(this.url() as string, '_blank');
  }

  parameters(): string {
    if (this.choice.parameters.size === 0) {
      return '';
    }
    let r = '';
    const separator = ' & ';
    if (this.title() && !this.url()) {
      r += 'intent = ' + this.choice.intentName + separator;
    }
    this.choice.parameters.forEach((v, k) => {
      if (k !== '_title') r += k + ' = ' + v + separator;
    });
    if (r.endsWith(separator)) {
      r = r.substring(0, r.length - separator.length);
    }
    return r.trim();
  }
}
