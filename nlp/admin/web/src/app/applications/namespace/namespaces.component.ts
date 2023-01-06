/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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

import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { StateService } from '../../core-nlp/state.service';
import { ApplicationService } from '../../core-nlp/applications.service';
import { NamespaceConfiguration, NamespaceSharingConfiguration, UserNamespace } from '../../model/application';
import { AuthService } from '../../core-nlp/auth/auth.service';
import { NbToastrService } from '@nebular/theme';
import { UserRole } from '../../model/auth';
import { ApplicationConfig } from '../application.config';

@Component({
  selector: 'tock-namespaces',
  templateUrl: 'namespaces.component.html',
  styleUrls: ['namespaces.component.css']
})
export class NamespacesComponent implements OnInit {
  namespaces: UserNamespace[];

  managedNamespace: string;
  managedUsers: UserNamespace[];
  namespaceConfiguration: NamespaceConfiguration;
  importedNamespaces: string[];
  sharableNamespaceConfigurations: NamespaceConfiguration[] = [];

  newLogin: string;
  newOwner: boolean;

  create: boolean;
  newNamespace: string = '';
  //in order to focus
  @ViewChild('createNamespace') createNamespaceElement: ElementRef;

  constructor(
    private toastrService: NbToastrService,
    public state: StateService,
    private applicationService: ApplicationService,
    private authService: AuthService,
    private applicationConfig: ApplicationConfig
  ) {}

  ngOnInit() {
    this.applicationService.getNamespaces().subscribe((n) => (this.namespaces = n));
  }

  selectNamespace(namespace: string) {
    this.applicationService
      .selectNamespace(namespace)
      .subscribe((_) => this.authService.loadUser().subscribe((_) => this.applicationService.resetConfiguration));
  }

  canCreateNamespace(): boolean {
    return this.state.hasRole(UserRole.admin) && this.applicationConfig.canCreateNamespace();
  }

  displayCreate() {
    this.create = true;
    setTimeout((_) => this.createNamespaceElement.nativeElement.focus());
  }

  createNew() {
    const n = this.newNamespace.trim();
    if (n.length === 0) {
      this.toastrService.danger('Namespace may not be empty!');
    } else {
      this.applicationService.createNamespace(n).subscribe((b) => {
        this.create = false;
        this.newNamespace = '';
        this.ngOnInit();
      });
    }
  }

  closeManageUsers() {
    this.managedNamespace = null;
    this.managedUsers = null;
  }

  manageUsers(namespace: string) {
    this.applicationService.getUsersForNamespace(namespace).subscribe((users) => {
      this.closeSharingSettings();
      this.managedUsers = users;
      this.managedNamespace = namespace;
    });
  }

  deleteUserNamespace(userNamespace: UserNamespace) {
    this.applicationService.deleteNamespace(userNamespace).subscribe((_) => this.manageUsers(userNamespace.namespace));
  }

  addUserNamespace() {
    if (!this.newLogin || this.newLogin.trim().length === 0) {
      this.toastrService.show('Please enter a non empty login');
    } else {
      this.applicationService
        .saveNamespace(new UserNamespace(this.managedNamespace, this.newLogin, this.newOwner, false))
        .subscribe((_) => this.manageUsers(this.managedNamespace));
    }
  }

  closeSharingSettings() {
    this.managedNamespace = null;
    this.namespaceConfiguration = null;
    this.importedNamespaces = null;
  }

  manageSharingSettings(namespace: string) {
    this.applicationService.getNamespaceConfiguration(namespace).subscribe((config) => {
      this.closeManageUsers();
      this.namespaceConfiguration =
        config ?? new NamespaceConfiguration(namespace, new NamespaceSharingConfiguration(false, false), new Map());
      this.managedNamespace = namespace;
      this.importedNamespaces = Array.from(this.namespaceConfiguration.namespaceImportConfiguration.keys());
      this.applicationService.getSharableNamespaceConfiguration().subscribe((configs) => {
        this.sharableNamespaceConfigurations = configs.filter((n) => n.namespace !== namespace);
      });
    });
  }

  saveNamespaceConfiguration() {
    this.namespaceConfiguration.namespaceImportConfiguration = new Map(
      this.sharableNamespaceConfigurations.map((c) => [c.namespace, c.defaultSharingConfiguration])
    );
    this.applicationService.saveNamespaceConfiguration(this.namespaceConfiguration).subscribe((_) => this.closeSharingSettings());
  }
}
