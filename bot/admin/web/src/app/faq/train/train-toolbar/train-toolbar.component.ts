/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

export enum BatchActionName {
  validate = "validate",
  unknown = "unknown",
  delete = "delete"
}


@Component({
  selector: 'tock-train-toolbar',
  templateUrl: './train-toolbar.component.html',
  styleUrls: ['./train-toolbar.component.scss']
})
export class TrainToolbarComponent implements OnInit {

  @Input()
  allChecked: boolean;

  @Input()
  disabled: boolean;

  @Output()
  onToggleSelectAll = new EventEmitter<boolean>();

  @Output()
  onBatchAction = new EventEmitter<BatchActionName>();

  constructor() {
  }

  ngOnInit(): void {
  }

  onSelectAll(value: boolean): void {
    this.onToggleSelectAll.emit(value);
  }

  validateAll(): void {
    this.onBatchAction.emit(BatchActionName.validate);
  }

  unknownAll(): void {
    this.onBatchAction.emit(BatchActionName.unknown);
  }

  deleteAll(): void {
    this.onBatchAction.emit(BatchActionName.delete);
  }

}
