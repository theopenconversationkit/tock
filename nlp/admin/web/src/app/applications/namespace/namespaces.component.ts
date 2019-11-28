/*
 * Copyright (C) 2017/2019 e-voyageurs technologies
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

import {Component, OnInit} from "@angular/core";
import {MatSnackBar} from "@angular/material/snack-bar";
import {StateService} from "../../core-nlp/state.service";
import {ApplicationService} from "../../core-nlp/applications.service";
import {UserNamespace} from "../../model/application";
import {AuthService} from "../../core-nlp/auth/auth.service";

@Component({
  selector: 'tock-namespaces',
  templateUrl: 'namespaces.component.html',
  styleUrls: ['namespaces.component.css']
})
export class NamespacesComponent implements OnInit {

  namespaces: UserNamespace[];

  managedNamespace: string;
  managedUsers: UserNamespace[];

  newLogin: string;
  newOwner: boolean;

  constructor(private snackBar: MatSnackBar,
              public state: StateService,
              private applicationService: ApplicationService,
              private authService: AuthService) {
  }

  ngOnInit() {
    this.applicationService.getNamespaces().subscribe(n => this.namespaces = n);
  }

  selectNamespace(namespace: string) {
    this.applicationService.selectNamespace(namespace).subscribe(_ =>
      this.authService.loadUser().subscribe(_ => this.applicationService.resetConfiguration)
    );
  }

  closeManageUsers() {
    this.managedNamespace = null;
    this.managedUsers = null;
  }

  manageUsers(namespace: string) {
    this.applicationService.getUsersForNamespace(namespace).subscribe(users => {
      this.managedUsers = users;
      this.managedNamespace = namespace;
    });
  }

  deleteUserNamespace(userNamespace: UserNamespace) {
    this.applicationService.deleteNamespace(userNamespace).subscribe(_ =>
      this.manageUsers(userNamespace.namespace)
    )
  }

  addUserNamespace() {
    if (!this.newLogin || this.newLogin.trim().length === 0) {
      this.snackBar.open("Please enter a non empty login")
    } else {
      this.applicationService.saveNamespace(new UserNamespace(this.managedNamespace, this.newLogin, this.newOwner, false)).subscribe(_ =>
        this.manageUsers(this.managedNamespace)
      );
    }
  }
}
