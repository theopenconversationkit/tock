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

import {Component, Inject, OnInit} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {StateService} from "../../core-nlp/state.service";
import {entityNameFromQualifiedName, EntityType, qualifiedNameWithoutRole} from "../../model/nlp";
import {EntityProvider} from "../highlight/highlight.component";

@Component({
  selector: 'tock-create-entity-dialog',
  templateUrl: 'create-entity-dialog.component.html',
  styleUrls: ['create-entity-dialog.component.css']
})
export class CreateEntityDialogComponent implements OnInit {

  entityProvider: EntityProvider;
  entityType: EntityType;
  type: string;
  role: string;

  roleInitialized: boolean;

  error: string;
  entityTypes: EntityType[];

  constructor(public dialogRef: MatDialogRef<CreateEntityDialogComponent>,
              private state: StateService,
              @Inject(MAT_DIALOG_DATA) private data: any) {
    this.entityProvider = data.entityProvider;
    this.state.entityTypesSortedByName().subscribe(entities => this.entityTypes = entities);
  }

  ngOnInit() {
  }

  onSelect(entityType: EntityType) {
    this.entityType = entityType;
    this.type = qualifiedNameWithoutRole(this.state.user, entityType.name);
    this.role = entityNameFromQualifiedName(entityType.name);
    this.roleInitialized = true;
  }

  onTypeKeyUp(event) {
    if (this.type) {
      this.type = this.type.replace(/[^A-Za-z:_-]*/g, '').toLowerCase().trim();
      if (!this.roleInitialized) {
        this.role = this.type;
      }
    }
  }

  onRoleKeyUp(event) {
    this.roleInitialized = true;
    if (this.role) {
      this.role = this.role.replace(/[^A-Za-z_-]*/g, '').toLowerCase().trim();
    }
  }

  save() {
    this.onTypeKeyUp(null);
    this.onRoleKeyUp(null);
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
      name = `${this.state.user.organization}:${name}`;
    }
    let role = this.role;
    if (!role || role.length === 0) {
      role = entityNameFromQualifiedName(name);
    }
    if (this.entityProvider && this.entityProvider.hasEntityRole(role)) {
      this.error = "Entity role already exists";
    } else {
      this.dialogRef.close({name: name, role: role});
    }
  }

}
