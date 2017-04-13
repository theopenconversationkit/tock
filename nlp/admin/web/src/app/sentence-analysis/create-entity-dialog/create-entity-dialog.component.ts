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

import {Component, OnInit} from "@angular/core";
import {MdDialogRef} from "@angular/material";
import {StateService} from "../../core/state.service";
import {Intent} from "../../model/application";
import {entityNameFromQualifiedName, EntityType, qualifiedNameWithoutRole} from "../../model/nlp";

@Component({
  selector: 'tock-create-entity-dialog',
  templateUrl: 'create-entity-dialog.component.html',
  styleUrls: ['create-entity-dialog.component.css']
})
export class CreateEntityDialogComponent implements OnInit {

  intent: Intent;
  entityType: EntityType;
  type: string;
  role: string;

  error: string;

  constructor(public dialogRef: MdDialogRef<CreateEntityDialogComponent>,
    public state: StateService) {

  }

  ngOnInit() {
    this.intent = this.dialogRef._containerInstance.dialogConfig.data.intent;
  }

  onSelect(entityType: EntityType) {
    this.entityType = entityType;
    this.type = qualifiedNameWithoutRole(this.state.user, entityType.name);
    this.role = entityNameFromQualifiedName(entityType.name);
  }

  onTypeKeyDown(event) {
    this.role = event.target.value + event.key;
  }

  onTypeChange() {
    this.role = this.type;
  }

  save() {
    this.error = undefined;
    let name = this.type;
    if (!name || name.length === 0) {
      if (this.entityType) {
        name = this.entityType.name;
      } else {
        this.error = "Please select or create an entity";
        return;
      }
    } else if (name.indexOf(':') === -1) {
      name = `${this.state.user.organization}:${name.trim().toLowerCase()}`;
    }
    let role = this.role;
    if (!role || role.length === 0) {
      role = entityNameFromQualifiedName(name);
    } else {
      role = role.trim().toLowerCase();
    }

    if (this.intent.containsEntityRole(role)) {
      this.error = "Entity role already exists for this intent";
    } else {
      this.dialogRef.close({name: name, role: role});
    }
  }

}
