import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { NbToastrService } from '@nebular/theme';
import { Subject, Subscription } from 'rxjs';
import { first, take, takeUntil } from 'rxjs/operators';

import { ConfirmDialogComponent } from '../../shared-nlp/confirm-dialog/confirm-dialog.component';
import { DialogService } from '../../core-nlp/dialog.service';
import { Filter, Scenario, ViewMode } from '../models';
import { ScenarioService } from '../services/scenario.service';
import { StateService } from '../../core-nlp/state.service';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { ScenarioEditComponent } from '../scenario-edit/scenario-edit.component';
import { Pagination } from '../../shared/pagination/pagination.component';
import { OrderBy, orderBy } from '../../shared/utils';

@Component({
  selector: 'scenarios-list',
  templateUrl: './scenarios-list.component.html',
  styleUrls: ['./scenarios-list.component.scss']
})
export class ScenariosListComponent implements OnInit, OnDestroy {
  @ViewChild('scenarioEditComponent') scenarioEditComponent: ScenarioEditComponent;

  configurations: BotApplicationConfiguration[];
  destroy$ = new Subject();
  scenarios: Scenario[] = [];
  filteredScenarios: Scenario[] = [];
  paginatedScenarios: Scenario[] = [];
  scenarioEdit?: Scenario;
  categoriesCache: string[] = [];
  tagsCache: string[] = [];

  currentViewMode: ViewMode = ViewMode.LIST;
  viewMode: typeof ViewMode = ViewMode;

  isSidePanelOpen: boolean = false;

  loading = {
    delete: false,
    edit: false,
    list: false
  };

  pagination: Pagination = {
    start: 0,
    end: 0,
    size: 10,
    total: 0
  };

  private currentFilters: Filter = { search: '', tags: [] };
  private currentOrderByCriteria: OrderBy = {
    criteria: 'name',
    reverse: false,
    secondField: 'name'
  };

  constructor(
    private botConfigurationService: BotConfigurationService,
    private dialogService: DialogService,
    private scenarioService: ScenarioService,
    private toastrService: NbToastrService,
    private router: Router,
    protected stateService: StateService
  ) {}

  ngOnInit() {
    this.botConfigurationService.configurations
      .pipe(takeUntil(this.destroy$))
      .subscribe((confs) => {
        this.configurations = confs;
        this.closeSidePanel();
      });

    this.loading.list = true;

    this.subscribeToScenarios();

    this.stateService.configurationChange.pipe(takeUntil(this.destroy$)).subscribe((_) => {
      this.subscribeToScenarios(true);
    });
  }

  scenariosSubscription: Subscription;
  subscribeToScenarios(forceReload = false) {
    if (this.scenariosSubscription) this.scenariosSubscription.unsubscribe();

    this.scenariosSubscription = this.scenarioService
      .getScenarios(forceReload)
      .pipe(takeUntil(this.destroy$))
      .subscribe((data: Scenario[]) => {
        this.loading.list = false;
        this.scenarios = [...data];
        this.pagination.total = data.length;
        this.filterScenarios(this.currentFilters, false);
        this.orderBy(this.currentOrderByCriteria);
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  addOrEditScenario(scenario?: Scenario): void {
    if (this.scenarioEditComponent) {
      this.scenarioEditComponent
        .close()
        .pipe(take(1))
        .subscribe((res) => {
          if (res != 'cancel') {
            setTimeout(() => {
              this.addOrEditScenario(scenario);
            }, 200);
          }
        });
    } else {
      if (scenario) this.edit(scenario);
      else this.add();
    }
  }

  add(): void {
    this.scenarioEdit = {
      id: null,
      category: '',
      description: '',
      name: '',
      tags: [],
      state: 'draft',
      applicationId: this.stateService.currentApplication._id
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
        if (scenario.id === this.scenarioEdit?.id) {
          this.closeSidePanel();
        }
        this.deleteScenario(scenario.id);
      }
    });
  }

  closeSidePanel(): void {
    this.isSidePanelOpen = false;
    this.scenarioEdit = undefined;
  }

  deleteScenario(id: string): void {
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
            this.loading.edit = false;
          }
        });
    }
  }

  switchViewMode(): void {
    this.currentViewMode =
      this.currentViewMode === ViewMode.LIST ? this.viewMode.TREE : this.viewMode.LIST;
  }

  filterScenarios(filters: Filter, resetPaginationStart: boolean = true): void {
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

    this.resetPaginationAfterFiltering(resetPaginationStart);

    this.orderBy(this.currentOrderByCriteria);
  }

  resetPaginationAfterFiltering(resetPaginationStart: boolean): void {
    if (resetPaginationStart) {
      this.pagination.start = 0;
    }
    this.pagination.total = this.filteredScenarios.length;

    const pageEnd = this.pagination.start + this.pagination.size;
    this.pagination.end = pageEnd < this.pagination.total ? pageEnd : this.pagination.total;

    if (this.pagination.start === this.pagination.end) {
      const pageStart = this.pagination.start - this.pagination.size;
      this.pagination.start = pageStart < 0 ? 0 : pageStart;
    }
  }

  paginateScenario(scenarios: Scenario[]): void {
    if (!this.pagination.end) {
      const pageEnd = this.pagination.end + this.pagination.size;
      this.pagination.end = scenarios.length > pageEnd ? pageEnd : scenarios.length;
    }

    this.paginatedScenarios = this.filteredScenarios.slice(
      this.pagination.start,
      this.pagination.end
    );
  }

  paginationChange(): void {
    this.paginateScenario(this.scenarios);
  }

  orderBy(event: OrderBy): void {
    this.currentOrderByCriteria = {
      ...this.currentOrderByCriteria,
      criteria: event.criteria,
      reverse: event.reverse
    };

    this.filteredScenarios = orderBy<Scenario>(
      this.filteredScenarios,
      this.currentOrderByCriteria.criteria,
      this.currentOrderByCriteria.reverse,
      this.currentOrderByCriteria.secondField
    );

    this.paginateScenario(this.filteredScenarios);
  }
}
