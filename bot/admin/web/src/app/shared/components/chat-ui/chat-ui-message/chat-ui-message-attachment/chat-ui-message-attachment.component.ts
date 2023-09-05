import { Component, Input, OnInit } from '@angular/core';
import { Attachment } from 'src/app/shared/model/dialog-data';

@Component({
  selector: 'tock-chat-ui-message-attachment',
  templateUrl: './chat-ui-message-attachment.component.html',
  styleUrls: ['./chat-ui-message-attachment.component.scss']
})
export class ChatUiMessageAttachmentComponent {
  @Input() attachment: Attachment;
}
