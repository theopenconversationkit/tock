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

import { Component, OnDestroy } from '@angular/core';
import { StateService } from '../../core-nlp/state.service';
import { ApplicationService } from '../../core-nlp/applications.service';
import { NamespaceConfiguration, NamespaceSharingConfiguration, UserNamespace } from '../../model/application';
import { AuthService } from '../../core-nlp/auth/auth.service';
import { NbDialogService, NbToastrService } from '@nebular/theme';
import { UserRole } from '../../model/auth';
import { ApplicationConfig } from '../application.config';
import { CreateNamespaceComponent } from './create-namespace/create-namespace.component';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'tock-namespaces',
  templateUrl: 'namespaces.component.html',
  styleUrls: ['namespaces.component.scss']
})
export class NamespacesComponent implements OnDestroy {
  destroy = new Subject();

  managedNamespace: string;
  managedUsers: UserNamespace[];
  namespaceConfiguration: NamespaceConfiguration;
  importedNamespaces: string[];
  sharableNamespaceConfigurations: NamespaceConfiguration[] = [];

  newLogin: string;
  newOwner: boolean;

  c;
  constructor(
    private toastrService: NbToastrService,
    public state: StateService,
    private applicationService: ApplicationService,
    private authService: AuthService,
    private applicationConfig: ApplicationConfig,
    private nbDialogService: NbDialogService
  ) {}

  selectNamespace(namespace: string): void {
    this.applicationService.selectNamespace(namespace).subscribe((_) =>
      this.authService.loadUser().subscribe((_) => {
        this.applicationService.resetConfiguration();
      })
    );
  }

  isAdmin(): boolean {
    return this.state.hasRole(UserRole.admin);
  }

  canCreateNamespace(): boolean {
    return this.isAdmin() && this.applicationConfig.canCreateNamespace();
  }

  createNamespace(): void {
    const modal = this.nbDialogService.open(CreateNamespaceComponent);
    const validate = modal.componentRef.instance.validate.pipe(takeUntil(this.destroy)).subscribe((result) => {
      this.applicationService.createNamespace(result.name.trim()).subscribe((b) => {
        this.applicationService.resetConfiguration();
      });
      this.closeEdition();
      modal.close();
    });
  }

  closeEdition(): void {
    this.managedNamespace = null;
    this.managedUsers = null;
    this.namespaceConfiguration = null;
    this.importedNamespaces = null;
  }

  manageUsers(namespace: string): void {
    if (this.managedNamespace && this.managedUsers) {
      this.closeEdition();
      return;
    }

    this.applicationService.getUsersForNamespace(namespace).subscribe((users) => {
      this.closeEdition();
      this.managedUsers = users;
      this.managedNamespace = namespace;
    });
  }

  deleteUserNamespace(userNamespace: UserNamespace): void {
    this.applicationService.deleteNamespace(userNamespace).subscribe((_) => this.manageUsers(userNamespace.namespace));
  }

  addUserNamespace(): void {
    if (!this.newLogin || this.newLogin.trim().length === 0) {
      this.toastrService.show('Please enter a non empty login');
    } else {
      this.applicationService
        .saveNamespace(new UserNamespace(this.managedNamespace, this.newLogin, this.newOwner, false))
        .subscribe((_) => this.manageUsers(this.managedNamespace));
    }
  }

  manageSharingSettings(namespace: string): void {
    if (this.managedNamespace && this.namespaceConfiguration) {
      this.closeEdition();
      return;
    }

    this.applicationService.getNamespaceConfiguration(namespace).subscribe((config) => {
      this.closeEdition();
      this.namespaceConfiguration =
        config ?? new NamespaceConfiguration(namespace, new NamespaceSharingConfiguration(false, false), new Map());
      this.managedNamespace = namespace;
      this.importedNamespaces = Array.from(this.namespaceConfiguration.namespaceImportConfiguration.keys());
      this.applicationService.getSharableNamespaceConfiguration().subscribe((configs) => {
        this.sharableNamespaceConfigurations = configs.filter((n) => n.namespace !== namespace);
      });
    });
  }

  saveNamespaceConfiguration(): void {
    this.namespaceConfiguration.namespaceImportConfiguration = new Map(
      this.sharableNamespaceConfigurations.map((c) => [c.namespace, c.defaultSharingConfiguration])
    );
    this.applicationService.saveNamespaceConfiguration(this.namespaceConfiguration).subscribe((_) => this.closeEdition());
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
