import { Component, Input } from '@angular/core';
import { Attachment } from 'src/app/shared/model/dialog-data';
import { RestService } from '../../../../../core-nlp/rest/rest.service';
import { sanitizeURLSync } from 'url-sanitizer';
@Component({
  selector: 'tock-chat-ui-message-attachment',
  templateUrl: './chat-ui-message-attachment.component.html',
  styleUrls: ['./chat-ui-message-attachment.component.scss']
})
export class ChatUiMessageAttachmentComponent {
  @Input() attachment: Attachment;

  constructor(public rest: RestService) {}

  localUrl() {
    const file = this.attachment.url.split('/').pop();

    return sanitizeURLSync(this.rest.url + '/file/' + file);
  }
}
