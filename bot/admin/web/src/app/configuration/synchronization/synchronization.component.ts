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

import { Component, OnInit } from '@angular/core';
import { ApplicationService } from '../../core-nlp/applications.service';
import { Application, UserNamespace } from '../../model/application';
import { StateService } from '../../core-nlp/state.service';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { DialogService } from '../../core-nlp/dialog.service';
import { NbToastrService } from '@nebular/theme';
import { ActivatedRoute, Router } from '@angular/router';
import { CopyContext, SynchronizationConfiguration } from '../../core/model/synchronizationConfiguration';
import { ChoiceDialogComponent } from '../../shared/components';

@Component({
  selector: 'tock-bot-synchronization',
  templateUrl: './synchronization.component.html',
  styleUrls: ['./synchronization.component.css']
})
export class SynchronizationComponent implements OnInit {
  sourceNamespace: UserNamespace;
  targetNamespace: UserNamespace;
  sourceApplications: Application[];
  targetApplications: Application[];
  sourceApplication: Application;
  targetApplication: Application;
  shouldSynchronizeInboxMessages: boolean = false;

  constructor(
    private applicationService: ApplicationService,
    public state: StateService,
    private botConfigurationService: BotConfigurationService,
    private dialog: DialogService,
    private toastrService: NbToastrService,
    private route: ActivatedRoute,
    private router: Router
  ) {}
  ngOnInit(): void {
    this.sourceApplications = this.state.applications;
  }

  selectSourceNamespace(namespace: UserNamespace) {
    this.sourceNamespace = namespace;
    this.applicationService.getApplicationsByNamespace(namespace.namespace).subscribe((apps) => {
      this.sourceApplications = apps;
      this.sourceApplication = undefined;
    });
  }

  selectTargetNamespace(namespace: UserNamespace) {
    this.targetNamespace = namespace;
    this.applicationService.getApplicationsByNamespace(namespace.namespace).subscribe((apps) => {
      this.targetApplications = apps;
      this.targetApplication = undefined;
    });
  }

  copyConfiguration() {
    const inboxMessagesCopySubtitle = this.shouldSynchronizeInboxMessages
      ? 'Inbox messages will be synchronized.'
      : 'Inbox messages will NOT be synchronized by default. If you want to, just check the "Copy inbox messages" checkbox on the duplication form.';

    const action = 'overwrite';

    let dialogRef = this.dialog.openDialog(ChoiceDialogComponent, {
      context: {
        title: 'Overwrite configuration?',
        subtitle: `During synchronization, configuration will be copied from the source application to the target application (answers, stories, training).

Please note : ${inboxMessagesCopySubtitle}

The synchronization of both applications will be permanent, and there will be no way to reverse it. Do you want to continue?
`,
        actions: [
          { actionName: 'cancel', buttonStatus: 'basic', ghost: true },
          { actionName: action, buttonStatus: 'danger' }
        ],
        modalStatus: 'danger'
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result === action) {
        let conf = new SynchronizationConfiguration(
          new CopyContext(this.sourceNamespace.namespace, this.sourceApplication.name, this.sourceApplication._id),
          new CopyContext(this.targetNamespace.namespace, this.targetApplication.name, this.targetApplication._id),
          this.shouldSynchronizeInboxMessages
        );
        this.botConfigurationService.synchronize(conf).subscribe((result) => {
          if (result) {
            this.toastrService.show(`Configuration has been copied`, 'Overwrite configuration', {
              duration: 2000,
              status: 'success'
            });
            this.state.resetConfiguration();
          } else {
            this.toastrService.show(`Copy configuration failed`, 'Error', {
              duration: 5000,
              status: 'danger'
            });
          }
          this.redirect();
        });
      }
    });
  }

  private redirect() {
    let redirect = '../../';
    this.router.navigate([redirect], { relativeTo: this.route });
  }
}
