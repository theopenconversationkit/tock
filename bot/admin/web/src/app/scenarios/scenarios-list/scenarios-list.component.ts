import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { NbDialogRef, NbToastrService } from '@nebular/theme';
import { Subject } from 'rxjs';
import { first, take, takeUntil } from 'rxjs/operators';

import { ConfirmDialogComponent } from '../../shared-nlp/confirm-dialog/confirm-dialog.component';
import { DialogService } from '../../core-nlp/dialog.service';
import { Filter, ScenarioGroup, ScenarioVersion, SCENARIO_STATE } from '../models';
import { ScenarioService } from '../services/scenario.service';
import { StateService } from '../../core-nlp/state.service';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { BotConfigurationService } from '../../core/bot-configuration.service';

import { ScenarioEditComponent } from './scenario-edit/scenario-edit.component';

import { OrderBy, orderBy } from '../../shared/utils';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ScenarioImportComponent } from './scenario-import/scenario-import.component';
import { ScenarioExportComponent } from './scenario-export/scenario-export.component';

import { deepCopy } from '../commons/utils';
import { Pagination } from '../../shared/components';

@Component({
  selector: 'scenarios-list',
  templateUrl: './scenarios-list.component.html',
  styleUrls: ['./scenarios-list.component.scss']
})
export class ScenariosListComponent implements OnInit, OnDestroy {
  @ViewChild('scenarioImportComponent') scenarioImportComponent: ScenarioImportComponent;
  @ViewChild('scenarioExportComponent') scenarioExportComponent: ScenarioExportComponent;
  @ViewChild('scenarioEditComponent') scenarioEditComponent: ScenarioEditComponent;
  @ViewChild('duplicationModal') duplicationModal: TemplateRef<any>;

  configurations: BotApplicationConfiguration[];
  destroy$ = new Subject();
  scenariosGroups: ScenarioGroup[] = [];
  filteredScenariosGroups: ScenarioGroup[] = [];
  paginatedScenariosGroups: ScenarioGroup[] = [];
  scenarioGroupEdit?: ScenarioGroup;
  categoriesCache: string[] = [];
  tagsCache: string[] = [];

  isSidePanelOpen = {
    edit: false,
    export: false,
    import: false
  };

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
    this.botConfigurationService.configurations.pipe(takeUntil(this.destroy$)).subscribe((confs) => {
      this.configurations = confs;
      this.closeSidePanel();
    });

    this.loading.list = true;

    this.subscribeToScenariosGroups();

    this.stateService.configurationChange.pipe(takeUntil(this.destroy$)).subscribe((_) => {
      this.subscribeToScenariosGroups(true);
    });
  }

  subscribeToScenariosGroups(forceReload = false): void {
    this.scenarioService
      .getScenariosGroups(forceReload)
      .pipe(takeUntil(this.destroy$))
      .subscribe((data: ScenarioGroup[]) => {
        this.loading.list = false;
        this.scenariosGroups = [...data];
        this.pagination.total = data.length;
        this.filterScenariosGroups(this.currentFilters, false);
        this.orderBy(this.currentOrderByCriteria);
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  addOrEditScenarioGroup(scenarioGroup?: ScenarioGroup): void {
    if (this.scenarioExportComponent) {
      this.closeSidePanel('export');
      setTimeout(() => {
        this.addOrEditScenarioGroup(scenarioGroup);
      }, 200);
    } else if (this.scenarioEditComponent || this.scenarioImportComponent) {
      this.closeSidePanelCheck(this.scenarioEditComponent || this.scenarioImportComponent, () => {
        setTimeout(() => {
          this.addOrEditScenarioGroup(scenarioGroup);
        }, 200);
      });
    } else {
      if (scenarioGroup) this.editScenarioGroup(scenarioGroup);
      else this.add();
    }
  }

  add(): void {
    this.scenarioGroupEdit = {
      id: null,
      category: '',
      description: '',
      name: '',
      tags: []
    } as ScenarioGroup;
    this.isSidePanelOpen.edit = true;
  }

  editScenarioGroup(scenarioGroup: ScenarioGroup): void {
    this.scenarioGroupEdit = scenarioGroup;
    this.isSidePanelOpen.edit = true;
  }

  duplicationForm: FormGroup = new FormGroup({
    comment: new FormControl('', [Validators.required])
  });

  get comment(): FormControl {
    return this.duplicationForm.get('comment') as FormControl;
  }

  get canSaveDuplication(): boolean {
    return this.isDuplicationSubmitted ? this.duplicationForm.valid : this.duplicationForm.dirty;
  }

  duplicationDialogRef: NbDialogRef<TemplateRef<any>>;
  duplicationScenarioMemo: { scenarioGroup: ScenarioGroup; scenarioVersion: ScenarioVersion };
  askDuplicationScenarioVersion({ scenarioGroup, scenarioVersion }): void {
    this.duplicationScenarioMemo = { scenarioGroup, scenarioVersion };
    this.duplicationDialogRef = this.dialogService.openDialog(this.duplicationModal);
  }

  duplicationCancel(): void {
    this.isDuplicationSubmitted = false;
    this.duplicationForm.reset();
    this.duplicationDialogRef.close();
    this.duplicationScenarioMemo = undefined;
  }

  isDuplicationSubmitted: boolean = false;
  duplicationSubmit(): void {
    this.isDuplicationSubmitted = true;
    if (this.canSaveDuplication) {
      this.duplicateScenarioVersion();
    }
  }

  duplicateScenarioVersion(): void {
    this.scenarioService
      .getScenarioVersion(this.duplicationScenarioMemo.scenarioGroup.id, this.duplicationScenarioMemo.scenarioVersion.id)
      .pipe(take(1))
      .subscribe((version) => {
        const copy = deepCopy(version);
        delete copy.id;
        delete copy.creationDate;
        delete copy.updateDate;
        copy.state = SCENARIO_STATE.draft;
        copy.comment = this.duplicationForm.value.comment;

        this.scenarioService.postScenarioVersion(this.duplicationScenarioMemo.scenarioGroup.id, copy).subscribe((res) => {
          this.duplicationCancel();
        });
      });
  }

  askDeleteScenarioGroup(scenarioGroup: ScenarioGroup): void {
    const deleteAction = 'delete';
    const dialogRef = this.dialogService.openDialog(ConfirmDialogComponent, {
      context: {
        title: `Delete scenario group "${scenarioGroup.name}"`,
        subtitle:
          'Are you sure you want to delete this scenario group and its scenario versions and, if applicable, the corresponding TickStory?',
        action: deleteAction
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result === deleteAction) {
        this.deleteScenarioGroup(scenarioGroup);
      }
    });
  }

  deleteScenarioGroup(scenarioGroup: ScenarioGroup): void {
    this.loading.delete = true;

    this.scenarioService
      .deleteScenarioGroup(scenarioGroup.id)
      .pipe(first())
      .subscribe({
        next: () => {
          this.toastrService.success(`Scenario group successfully deleted`, 'Success', {
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

  askDeleteScenarioVersion({ scenarioGroup, scenarioVersion }): void {
    const deleteAction = 'delete';
    const dialogRef = this.dialogService.openDialog(ConfirmDialogComponent, {
      context: {
        title: 'Delete scenario version',
        subtitle:
          scenarioVersion.state === SCENARIO_STATE.current
            ? 'Are you sure you want to delete the scenario version and, if applicable, the corresponding TickStory?'
            : 'Are you sure you want to delete the scenario version ?',
        action: deleteAction
      }
    });
    dialogRef.onClose.subscribe((result) => {
      if (result === deleteAction) {
        if (scenarioVersion.id === this.scenarioGroupEdit?.id) {
          this.closeSidePanel('edit');
        }
        this.deleteScenarioVersion(scenarioGroup.id, scenarioVersion.id);
      }
    });
  }

  closeSidePanel(panel?: 'edit' | 'export' | 'import'): void {
    if (!panel) {
      this.isSidePanelOpen.edit = false;
      this.isSidePanelOpen.export = false;
      this.isSidePanelOpen.import = false;
      this.scenarioGroupEdit = undefined;
    } else {
      switch (panel) {
        case 'edit':
          this.isSidePanelOpen.edit = false;
          this.scenarioGroupEdit = undefined;
          break;
        case 'export':
          this.isSidePanelOpen.export = false;
          break;
        case 'import':
          this.isSidePanelOpen.import = false;
          break;
      }
    }
  }

  deleteScenarioVersion(scenarioGroupId: string, scenarioVersionId: string): void {
    this.loading.delete = true;

    this.scenarioService
      .deleteScenarioVersion(scenarioGroupId, scenarioVersionId)
      .pipe(first())
      .subscribe({
        next: () => {
          this.toastrService.success(`Scenario version successfully deleted`, 'Success', {
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

  saveScenarioGroup(result: { scenarioGroup: ScenarioGroup; redirect: boolean }): void {
    this.loading.edit = true;

    if (!result.scenarioGroup.id) {
      this.scenarioService
        .postScenarioGroup(result.scenarioGroup)
        .pipe(first())
        .subscribe({
          next: (newScenarioGroup) => {
            this.loading.edit = false;
            this.toastrService.success(`Scenario group successfully created`, 'Success', {
              duration: 5000,
              status: 'success'
            });
            if (result.redirect) {
              let versionId: string = newScenarioGroup.versions[0].id;
              this.router.navigateByUrl(`/scenarios/${newScenarioGroup.id}/${versionId}`);
            } else {
              this.closeSidePanel('edit');
            }
          },
          error: () => {
            this.loading.edit = false;
          }
        });
    } else {
      this.scenarioService
        .updateScenarioGroup(result.scenarioGroup)
        .pipe(first())
        .subscribe({
          next: (newScenarioGroup) => {
            this.loading.edit = false;
            this.toastrService.success(`Scenario group successfully updated`, 'Success', {
              duration: 5000,
              status: 'success'
            });
            if (result.redirect) {
              this.router.navigateByUrl(`/scenarios/${newScenarioGroup.id}`);
            } else {
              this.closeSidePanel('edit');
            }
          },
          error: () => {
            this.loading.edit = false;
          }
        });
    }
  }

  filterScenariosGroups(filters: Filter, resetPaginationStart: boolean = true): void {
    const { search, tags } = filters;
    this.currentFilters = filters;

    this.filteredScenariosGroups = this.scenariosGroups.filter((scenarioGroup: ScenarioGroup) => {
      if (
        search &&
        !(
          scenarioGroup.name.toUpperCase().includes(search.toUpperCase()) ||
          scenarioGroup.description.toUpperCase().includes(search.toUpperCase())
        )
      ) {
        return;
      }

      if (tags?.length && !scenarioGroup.tags.some((tag) => tags.includes(tag))) {
        return;
      }

      return scenarioGroup;
    });

    this.resetPaginationAfterFiltering(resetPaginationStart);

    this.orderBy(this.currentOrderByCriteria);
  }

  resetPaginationAfterFiltering(resetPaginationStart: boolean): void {
    if (resetPaginationStart) {
      this.pagination.start = 0;
    }
    this.pagination.total = this.filteredScenariosGroups.length;

    const pageEnd = this.pagination.start + this.pagination.size;
    this.pagination.end = pageEnd < this.pagination.total ? pageEnd : this.pagination.total;

    if (this.pagination.start === this.pagination.end) {
      const pageStart = this.pagination.start - this.pagination.size;
      this.pagination.start = pageStart < 0 ? 0 : pageStart;
    }
  }

  paginateScenariosGroups(scenariosGroups: ScenarioGroup[]): void {
    if (!this.pagination.end) {
      const pageEnd = this.pagination.end + this.pagination.size;
      this.pagination.end = scenariosGroups.length > pageEnd ? pageEnd : scenariosGroups.length;
    }

    this.paginatedScenariosGroups = this.filteredScenariosGroups.slice(this.pagination.start, this.pagination.end);
  }

  paginationChange(): void {
    this.paginateScenariosGroups(this.scenariosGroups);
  }

  orderBy(event: OrderBy): void {
    this.currentOrderByCriteria = {
      ...this.currentOrderByCriteria,
      criteria: event.criteria,
      reverse: event.reverse
    };

    this.filteredScenariosGroups = orderBy<ScenarioGroup>(
      this.filteredScenariosGroups,
      this.currentOrderByCriteria.criteria,
      this.currentOrderByCriteria.reverse,
      this.currentOrderByCriteria.secondField
    );

    this.paginateScenariosGroups(this.filteredScenariosGroups);
  }

  openExportScenario(): void {
    if (this.scenarioEditComponent || this.scenarioImportComponent) {
      this.closeSidePanelCheck(this.scenarioEditComponent || this.scenarioImportComponent, () => {
        this.isSidePanelOpen.export = true;
      });
    } else {
      this.isSidePanelOpen.export = true;
    }
  }

  openImportScenario(): void {
    if (this.scenarioEditComponent) {
      this.closeSidePanelCheck(this.scenarioEditComponent, () => {
        this.isSidePanelOpen.import = true;
      });
    } else if (this.scenarioExportComponent) {
      this.closeSidePanel('export');
      this.isSidePanelOpen.import = true;
    } else {
      this.isSidePanelOpen.import = true;
    }
  }

  private closeSidePanelCheck(component: ScenarioEditComponent | ScenarioImportComponent, cb: Function): void {
    component
      .close()
      .pipe(take(1))
      .subscribe((res) => {
        if (res != 'cancel' && res != undefined) {
          cb();
        }
      });
  }
}
