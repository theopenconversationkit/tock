/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Component, OnInit } from '@angular/core';
import {MdDialogRef} from "@angular/material";
import {NlpService} from "../../nlp-tabs/nlp.service";

@Component({
  selector: 'tock-create-entity-dialog',
  templateUrl: 'create-entity-dialog.component.html',
  styleUrls: ['create-entity-dialog.component.css']
})
export class CreateEntityDialogComponent implements OnInit {

  type:string;
  role:string;

  constructor(public dialogRef: MdDialogRef<CreateEntityDialogComponent>) {

  }

  ngOnInit() {
  }

  save() {
    this.dialogRef.close({type:this.type, role:this.role});
  }

}
