import { Component, Input, ViewChild } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { JsonEditorComponent, JsonEditorOptions } from 'ang-jsoneditor';

@Component({
  selector: 'tock-display-intent-full-log',
  templateUrl: './display-intents-full-log.component.html',
  styleUrls: ['./display-intents-full-log.component.scss']
})
export class DisplayIntentFullLogComponent {
  public editorOptions: JsonEditorOptions;
  @Input() request: string;
  @Input() response: string;

  @ViewChild(JsonEditorComponent, { static: true }) editor: JsonEditorComponent;

  constructor(public dialogRef: NbDialogRef<DisplayIntentFullLogComponent>) {
    this.editorOptions = new JsonEditorOptions();
    this.editorOptions.modes = ['code', 'view'];
    this.editorOptions.mode = 'view';
    this.editorOptions.expandAll = true;
    this.editorOptions.mainMenuBar = true;
    this.editorOptions.navigationBar = true;
  }

  close() {
    this.dialogRef.close();
  }
}
