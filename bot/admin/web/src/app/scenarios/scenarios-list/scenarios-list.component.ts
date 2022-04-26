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

import { Component, OnInit } from '@angular/core';
import { NbTreeGridDataSource, NbTreeGridDataSourceBuilder } from '@nebular/theme';

import { Scenario } from '../models';
import { ScenarioService } from '../services/scenario.service';
@Component({
  selector: 'scenarios-list',
  templateUrl: './scenarios-list.component.html',
  styleUrls: ['./scenarios-list.component.scss']
})
export class ScenariosListComponent implements OnInit {
  actionsColumn = 'actions';
  categoryColumn = 'category';
  defaultColumns = ['name', 'description'];
  allColumns = [this.categoryColumn, ...this.defaultColumns, this.actionsColumn];

  dataSource: NbTreeGridDataSource<any>;

  loading: boolean = false;
  isSidePanelOpen: boolean = false;

  constructor(
    private scenarioService: ScenarioService,
    private dataSourceBuilder: NbTreeGridDataSourceBuilder<Scenario>
  ) {}

  ngOnInit() {
    this.loading = true;
    this.scenarioService.getScenariosTreeGrid().subscribe((data: any) => {
      this.loading = false;
      this.dataSource = this.dataSourceBuilder.create(data);
    });
  }

  add(): void {
    this.isSidePanelOpen = true;
    console.log('add');
  }

  edit(scenario: any): void {
    this.isSidePanelOpen = true;
    console.log('edit', scenario);
  }

  delete(scenario: any): void {
    console.log('delete', scenario);
  }

  closeSidePanel(): void {
    this.isSidePanelOpen = false;
  }
}
