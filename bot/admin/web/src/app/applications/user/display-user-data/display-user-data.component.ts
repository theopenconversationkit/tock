/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
