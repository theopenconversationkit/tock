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
import { NbDialogService } from '@nebular/theme';

import { ApplicationService } from '../../core-nlp/applications.service';
import { StateService } from '../../core-nlp/state.service';
import { UserLog } from '../../model/application';
import { PaginatedQuery } from '../../model/commons';
import { DisplayUserDataComponent } from './display-user-data/display-user-data.component';
import { Pagination } from '../../shared/components';

@Component({
  selector: 'tock-user-logs',
  templateUrl: './user-logs.component.html',
  styleUrls: ['./user-logs.component.scss']
})
export class UserLogsComponent implements OnInit {
  dataSource: UserLog[];
  loading: boolean = false;
  pagination: Pagination = {
    start: 0,
    end: 0,
    size: 10,
    total: 0
  };

  constructor(private state: StateService, private applicationService: ApplicationService, private dialogService: NbDialogService) {}

  ngOnInit(): void {
    this.search();
  }

  appName(appId: string): string {
    if (!appId) {
      return null;
    }
    const r = this.state.applications.find((a) => a._id === appId);
    return r ? r.name : appId;
  }

  displayData(log: UserLog): void {
    this.dialogService.open(DisplayUserDataComponent, {
      context: {
        data: JSON.parse(log.data())
      }
    });
  }

  search(): void {
    this.loading = true;
    const startIndex = this.pagination.start;
    this.applicationService
      .searchUserLogs(
        new PaginatedQuery(
          this.state.currentApplication.namespace,
          this.state.currentApplication.name,
          this.state.currentLocale,
          startIndex,
          this.pagination.size
        )
      )
      .subscribe((r) => {
        this.loading = false;
        this.dataSource = r.logs;

        this.pagination.total = r.total;
        this.pagination.end = Math.min(this.pagination.start + this.pagination.size, this.pagination.total);
      });
  }
}
