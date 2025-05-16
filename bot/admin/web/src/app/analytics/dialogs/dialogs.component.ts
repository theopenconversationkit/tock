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

import { AfterViewInit, Component, ViewChild } from '@angular/core';
import { DialogsListComponent } from './dialogs-list/dialogs-list.component';

@Component({
  selector: 'tock-dialogs',
  templateUrl: './dialogs.component.html',
  styleUrls: ['./dialogs.component.css']
})
export class DialogsComponent implements AfterViewInit {
  @ViewChild('dialogsList') dialogsList: DialogsListComponent;

  count: string = '';

  ngAfterViewInit() {
    this.dialogsList.totalDialogsCount.subscribe((count) => {
      this.count = count;
    });
  }

  refresh() {
    this.dialogsList.refresh();
  }
}
