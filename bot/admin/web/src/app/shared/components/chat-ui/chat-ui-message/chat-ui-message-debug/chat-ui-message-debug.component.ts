import { Component, Input, OnInit } from '@angular/core';
import { NbDialogService } from '@nebular/theme';
import { Debug } from '../../../../model/dialog-data';
import { DebugViewerComponent } from '../../../debug-viewer/debug-viewer.component';

@Component({
  selector: 'tock-chat-ui-message-debug',
  templateUrl: './chat-ui-message-debug.component.html',
  styleUrls: ['./chat-ui-message-debug.component.scss']
})
export class ChatUiMessageDebugComponent implements OnInit {
  @Input() message: Debug;
  constructor(private nbDialogService: NbDialogService) {}

  ngOnInit(): void {
    console.log(this.message);
  }

  showDebug() {
    this.nbDialogService.open(DebugViewerComponent, {
      context: {
        debug: this.message.data
      }
    });
  }
}
