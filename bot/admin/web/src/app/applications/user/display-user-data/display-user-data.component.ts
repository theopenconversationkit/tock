import { Component, Input, ViewChild } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { JsonEditorComponent, JsonEditorOptions } from 'ang-jsoneditor';

@Component({
  selector: 'tock-display-user-data',
  templateUrl: './display-user-data.component.html',
  styleUrls: ['./display-user-data.component.scss']
})
export class DisplayUserDataComponent {
  public editorOptions: JsonEditorOptions;

  @ViewChild(JsonEditorComponent, { static: true }) editor: JsonEditorComponent;

  @Input() data: string;

  constructor(public dialogRef: NbDialogRef<DisplayUserDataComponent>) {
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
