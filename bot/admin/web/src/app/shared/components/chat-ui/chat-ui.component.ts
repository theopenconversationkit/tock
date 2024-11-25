import { Component, ElementRef, Input, ViewChild } from '@angular/core';

@Component({
  selector: 'tock-chat-ui',
  templateUrl: './chat-ui.component.html',
  styleUrls: ['./chat-ui.component.scss']
})
export class ChatUiComponent {
  @Input() height?: string;
  @Input() maxHeight?: string;
  @Input() padding?: string;

  @ViewChild('scrollable') private scrollable: ElementRef;

  scrollToBottom() {
    this.scrollable.nativeElement.scrollTop = this.scrollable.nativeElement.scrollHeight;
  }
}
