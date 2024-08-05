import { Component, Input, OnInit } from '@angular/core';
import { NbDialogService } from '@nebular/theme';
import { Debug } from '../../../../model/dialog-data';
import { DebugViewerDialogComponent } from '../../../debug-viewer-dialog/debug-viewer-dialog.component';

@Component({
  selector: 'tock-chat-ui-message-debug',
  templateUrl: './chat-ui-message-debug.component.html',
  styleUrls: ['./chat-ui-message-debug.component.scss']
})
export class ChatUiMessageDebugComponent {
  @Input() message: Debug;
  constructor(private nbDialogService: NbDialogService) {}

  showDebug() {
    this.nbDialogService.open(DebugViewerDialogComponent, {
      context: {
        debug: this.message.data
      }
    });
  }
}
