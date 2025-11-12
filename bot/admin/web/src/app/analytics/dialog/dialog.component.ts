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

import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, take, takeUntil } from 'rxjs';
import { AnalyticsService } from '../analytics.service';
import { StateService } from '../../core-nlp/state.service';
import { ActionReport, DialogReport } from '../../shared/model/dialog-data';
import { ApplicationService } from '../../core-nlp/applications.service';
import { AuthService } from '../../core-nlp/auth/auth.service';
import { SettingsService } from '../../core-nlp/settings.service';
import { copyToClipboard } from '../../shared/utils';
import {BotSharedService} from "../../shared/bot-shared.service";

@Component({
  selector: 'tock-dialog',
  templateUrl: './dialog.component.html',
  styleUrl: './dialog.component.scss'
})
export class DialogComponent implements OnInit, OnDestroy {
  destroy = new Subject();

  dialog: DialogReport;

  accessDenied: boolean = false;

  targetFragment: string;

  constructor(
    private route: ActivatedRoute,
    private analytics: AnalyticsService,
    private state: StateService,
    private applicationService: ApplicationService,
    public auth: AuthService,
    public settings: SettingsService,
    private botSharedService: BotSharedService,
    private router: Router
  ) {}

  ngOnInit(): void {
    setTimeout(() => this.initialize(), 500);
  }

  initialize(): void {
    this.route.fragment.subscribe((fragment: string) => {
      if (fragment) {
        this.targetFragment = fragment;
      }
    });

    this.route.params.pipe(take(1)).subscribe((routeParams) => {
      // User does not have rights to access this namespace
      if (!this.state.namespaces.find((ns) => ns.namespace === routeParams.namespace)) {
        this.accessDenied = true;

        return;
      }

      // The current application does not belong to the requested namespace, so we move to the target namespace.
      if (routeParams.namespace !== this.state.currentApplication.namespace) {
        this.applicationService
          .selectNamespace(routeParams.namespace)
          .pipe(take(1))
          .subscribe((_) => {
            this.auth.loadUser().subscribe((_) => {
              this.state.currentApplicationEmitter.pipe(take(1)).subscribe((arg) => {
                this.initialize();
              });
              this.applicationService.resetConfiguration();
            });
          });

        return;
      }

      // The current application is not the requested one, we move to the targeted application.
      if (routeParams.applicationId !== this.state.currentApplication._id) {
        const targetApp = this.state.applications.find((app) => app._id === routeParams.applicationId);
        if (targetApp) {
          this.state.currentApplicationEmitter.pipe(take(1)).subscribe((arg) => {
            this.initialize();
          });
          this.state.changeApplicationWithName(targetApp.name);
        } else {
          this.accessDenied = true;
        }

        return;
      }

      this.botSharedService
        .getDialogWithNlpStats(this.state.currentApplication._id, routeParams.dialogId)
        .pipe(take(1))
        .subscribe((dialog) => {
          if (dialog?.actions?.length) {
            this.dialog = dialog;

            if (this.targetFragment) {
              setTimeout(() => {
                const target = document.querySelector(`#action-anchor-${this.targetFragment}`);
                if (target) {
                  target.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
              }, 300);
            }
          } else {
            this.accessDenied = true;
          }
        });

      // We monitor changes in the current application to redirect to the dialogs page in case of change.
      this.state.currentApplicationEmitter.pipe(takeUntil(this.destroy)).subscribe((arg) => {
        if (
          routeParams.namespace !== this.state.currentApplication.namespace ||
          routeParams.applicationId !== this.state.currentApplication._id
        ) {
          this.jumpToDialogs();
        }
      });
    });
  }

  getTargetedAction(): ActionReport {
    if (!this.targetFragment || !this.dialog) return;
    return this.dialog.actions.find((action) => {
      return action.id === this.targetFragment;
    });
  }

  jumpToDialogs(addAnchorRef: boolean = false): void {
    const extras = addAnchorRef && this.dialog?.id ? { state: { dialogId: this.dialog.id } } : undefined;
    this.router.navigateByUrl('/analytics/dialogs', extras);
  }

  copyUrl(): void {
    copyToClipboard(window.location.href);
  }

  ngOnDestroy(): void {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
