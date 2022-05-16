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

import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NbToastrService } from '@nebular/theme';
import { Observable, Observer, Subject, Subscription } from 'rxjs';

import { ConfirmDialogComponent } from '../../shared-nlp/confirm-dialog/confirm-dialog.component';
import { DialogService } from '../../core-nlp/dialog.service';
import { Filter, Scenario, ViewMode } from '../models';
import { ScenarioService } from '../services/scenario.service';
import { StateService } from 'src/app/core-nlp/state.service';
import { first, takeUntil } from 'rxjs/operators';

@Component({
  selector: 'scenarios-list',
  templateUrl: './scenarios-list.component.html',
  styleUrls: ['./scenarios-list.component.scss']
})
export class ScenariosListComponent implements OnInit, OnDestroy {
  destroy = new Subject();
  scenarios: Scenario[] = [];
  filteredScenarios: Scenario[] = [];
  scenarioEdit?: Scenario;

  currentViewMode: ViewMode = ViewMode.LIST;
  viewMode: typeof ViewMode = ViewMode;

  isSidePanelOpen: boolean = false;

  loading = {
    delete: false,
    edit: false,
    list: false
  };

  private currentFilters: Filter = { search: '', tags: [] };

  constructor(
    private dialogService: DialogService,
    private scenarioService: ScenarioService,
    private toastrService: NbToastrService,
    private router: Router,
    protected state: StateService
  ) {}

  ngOnInit() {
    this.loading.list = true;

    this.subscribeToScenarios();

    this.state.configurationChange.pipe(takeUntil(this.destroy)).subscribe((_) => {
      this.subscribeToScenarios(true);
    });
  }

  scenariosSubscription: Subscription;
  subscribeToScenarios(forceRelaod = false) {
    if (this.scenariosSubscription) this.scenariosSubscription.unsubscribe();

    this.scenariosSubscription = this.scenarioService
      .getScenarios(forceRelaod)
      .pipe(takeUntil(this.destroy))
      .subscribe((data: Scenario[]) => {
        this.loading.list = false;
        this.scenarios = [...data];
        this.filterScenarios(this.currentFilters);
      });
  }

  ngOnDestroy(): void {
    this.destroy.next();
    this.destroy.complete();
  }

  add(): void {
    this.scenarioEdit = {
      id: null,
      category: '',
      description: '',
      name: '',
      tags: []
    } as Scenario;
    this.isSidePanelOpen = true;
  }

  edit(scenario: Scenario): void {
    this.scenarioEdit = scenario;
    this.isSidePanelOpen = true;
  }

  delete(scenario: Scenario): void {
    const deleteAction = 'delete';
    const dialogRef = this.dialogService.openDialog(ConfirmDialogComponent, {
      context: {
        title: `Delete scenario "${scenario.name}"`,
        subtitle: 'Are you sure you want to delete the scenario ?',
        action: deleteAction
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result === deleteAction) {
        this.deleteScenario(scenario.id);
      }
    });
  }

  closeSidePanel(): void {
    this.isSidePanelOpen = false;
    this.scenarioEdit = undefined;
  }

  deleteScenario(id: number): void {
    this.loading.delete = true;

    this.scenarioService
      .deleteScenario(id)
      .pipe(first())
      .subscribe({
        next: () => {
          this.toastrService.success(`Scenario successfully deleted`, 'Success', {
            duration: 5000,
            status: 'success'
          });
          this.loading.delete = false;
        },
        error: () => {
          this.toastrService.danger(`Failed to delete scenario`, 'Error', {
            duration: 5000,
            status: 'danger'
          });
          this.loading.delete = false;
        }
      });
  }

  saveScenario(result) {
    this.loading.edit = true;

    if (!result.scenario.id) {
      this.scenarioService
        .postScenario(result.scenario)
        .pipe(first())
        .subscribe({
          next: (newScenario) => {
            this.loading.edit = false;
            this.toastrService.success(`Scenario successfully created`, 'Success', {
              duration: 5000,
              status: 'success'
            });
            if (result.redirect) {
              this.router.navigateByUrl(`/scenarios/${newScenario.id}`);
            } else {
              this.closeSidePanel();
            }
          },
          error: () => {
            this.toastrService.danger(`Failed to create scenario`, 'Error', {
              duration: 5000,
              status: 'danger'
            });
            this.loading.edit = false;
          }
        });
    } else {
      this.scenarioService
        .putScenario(result.scenario.id, result.scenario)
        .pipe(first())
        .subscribe({
          next: (newScenario) => {
            this.loading.edit = false;
            this.toastrService.success(`Scenario successfully updated`, 'Success', {
              duration: 5000,
              status: 'success'
            });
            if (result.redirect) {
              this.router.navigateByUrl(`/scenarios/${newScenario.id}`);
            } else {
              this.closeSidePanel();
            }
          },
          error: () => {
            this.toastrService.danger(`Failed to update scenario`, 'Error', {
              duration: 5000,
              status: 'danger'
            });
            this.loading.edit = false;
          }
        });
    }
  }

  switchViewMode(): void {
    this.currentViewMode =
      this.currentViewMode === ViewMode.LIST ? this.viewMode.TREE : this.viewMode.LIST;
  }

  filterScenarios(filters: Filter): void {
    const { search, tags } = filters;
    this.currentFilters = filters;

    this.filteredScenarios = this.scenarios.filter((scenario: Scenario) => {
      if (
        search &&
        !(
          scenario.name.toUpperCase().includes(search.toUpperCase()) ||
          scenario.description.toUpperCase().includes(search.toUpperCase())
        )
      ) {
        return;
      }

      if (tags?.length && !scenario.tags.some((tag) => tags.includes(tag))) {
        return;
      }

      return scenario;
    });
  }
}
