import { Component, Input } from '@angular/core';

@Component({
  selector: 'tock-chat-ui',
  templateUrl: './chat-ui.component.html',
  styleUrls: ['./chat-ui.component.scss']
})
export class ChatUiComponent {
  @Input() height: string;
}
