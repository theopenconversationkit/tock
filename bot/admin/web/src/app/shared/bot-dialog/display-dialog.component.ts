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

import { Component, Inject, Input, OnInit } from '@angular/core';
import { DialogReport } from '../model/dialog-data';
import { UserRole } from '../../model/auth';
import { StateService } from '../../core-nlp/state.service';
import { AnalyticsService } from '../../analytics/analytics.service';
import { Router } from '@angular/router';
import { APP_BASE_HREF } from '@angular/common';

@Component({
  selector: 'tock-display-dialog',
  templateUrl: './display-dialog.component.html',
  styleUrls: ['./display-dialog.component.css']
})
export class DisplayDialogComponent implements OnInit {
  @Input()
  dialog: DialogReport;
  @Input()
  userPicture: string;

  constructor(
    private state: StateService,
    private analyticsService: AnalyticsService,
    @Inject(APP_BASE_HREF) public baseHref: string
  ) {}

  ngOnInit() {}

  canReveal(): boolean {
    return this.dialog.obfuscated && this.state.hasRole(UserRole.admin);
  }

  reveal() {
    this.analyticsService
      .dialog(this.state.currentApplication._id, this.dialog.id)
      .subscribe((d) => (this.dialog = d));
  }
}
