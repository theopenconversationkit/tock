import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { NbDialogRef, NbDialogService, NbToastrService } from '@nebular/theme';
import { Subject } from 'rxjs';
import { first, take, takeUntil } from 'rxjs/operators';

import { Filter, ScenarioGroup, ScenarioGroupExtended, ScenarioGroupUpdate, ScenarioVersion, SCENARIO_STATE } from '../models';
import { ScenarioService } from '../services';
import { StateService } from '../../core-nlp/state.service';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { ScenarioEditComponent, ScenarioEditOnSave } from './scenario-edit/scenario-edit.component';
import { OrderBy, orderBy } from '../../shared/utils';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ScenarioImportComponent } from './scenario-import/scenario-import.component';
import { ScenarioExportComponent } from './scenario-export/scenario-export.component';
import { deepCopy } from '../../shared/utils';
import { Pagination } from '../../shared/components';
import { ScenariosSettingsComponent } from './scenarios-settings/scenarios-settings.component';
import { I18nLabel, I18nLabels } from '../../bot/model/i18n';

@Component({
  selector: 'tock-scenarios-list',
  templateUrl: './scenarios-list.component.html',
  styleUrls: ['./scenarios-list.component.scss']
})
export class ScenariosListComponent implements OnInit, OnDestroy {
  @ViewChild('scenarioImportComponent') scenarioImportComponent: ScenarioImportComponent;
  @ViewChild('scenarioExportComponent') scenarioExportComponent: ScenarioExportComponent;
  @ViewChild('scenarioEditComponent') scenarioEditComponent: ScenarioEditComponent;
  @ViewChild('scenarioSettingsComponent') scenarioSettingsComponent: ScenariosSettingsComponent;
  @ViewChild('duplicationModal') duplicationModal: TemplateRef<any>;

  configurations: BotApplicationConfiguration[];
  destroy$ = new Subject();
  scenariosGroups: ScenarioGroup[] = [];
  filteredScenariosGroups: ScenarioGroup[] = [];
  paginatedScenariosGroups: ScenarioGroup[] = [];
  scenarioGroupEdit?: ScenarioGroup;
  categoriesCache: string[] = [];
  tagsCache: string[] = [];
  i18nLabels: I18nLabels;

  isSidePanelOpen = {
    edit: false,
    export: false,
    import: false,
    settings: false
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

  private currentFilters: Filter = { search: '', tags: [], enabled: null };
  private currentOrderByCriteria: OrderBy = {
    criteria: 'name',
    reverse: false,
    secondField: 'name'
  };

  constructor(
    private botConfigurationService: BotConfigurationService,
    private nbDialogService: NbDialogService,
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

    this.loadScenariosGroups();
  }

  loadScenariosGroups(): void {
    this.loading.list = true;

    this.scenarioService
      .getScenariosGroups()
      .pipe(takeUntil(this.destroy$))
      .subscribe((scenariosGroups: ScenarioGroupExtended[]) => {
        this.loading.list = false;
        this.initController(scenariosGroups);
      });
  }

  private initController(scenariosGroups: ScenarioGroupExtended[]): void {
    this.scenariosGroups = [...scenariosGroups];
    this.pagination.total = scenariosGroups.length;
    this.filterScenariosGroups(this.currentFilters, false);
    this.orderBy(this.currentOrderByCriteria);
  }

  ngOnDestroy(): void {
    this.destroy$.next(true);
    this.destroy$.complete();
  }

  addOrEditScenarioGroup(scenarioGroup?: ScenarioGroup): void {
    if (this.scenarioExportComponent) {
      this.closeSidePanel('export');
      setTimeout(() => {
        this.addOrEditScenarioGroup(scenarioGroup);
      }, 200);
    } else if (this.scenarioEditComponent || this.scenarioImportComponent || this.scenarioSettingsComponent) {
      this.closeSidePanelCheck(this.scenarioEditComponent || this.scenarioImportComponent || this.scenarioSettingsComponent, () => {
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
      tags: [],
      unknownAnswerId: ''
    } as ScenarioGroup;
    this.isSidePanelOpen.edit = true;
  }

  editScenarioGroup(scenarioGroup: ScenarioGroup): void {
    this.scenarioGroupEdit = scenarioGroup;
    this.isSidePanelOpen.edit = true;
  }

  duplicationForm: FormGroup = new FormGroup({
    comment: new FormControl<string>('', [Validators.required])
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
    this.duplicationDialogRef = this.nbDialogService.open(this.duplicationModal);
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

        this.scenarioService.postScenarioVersion(this.duplicationScenarioMemo.scenarioGroup.id, copy).subscribe((_res) => {
          this.toastrService.success('Scenario version successfully duplicated', 'Duplicate', { duration: 5000 });
          this.duplicationCancel();
        });
      });
  }

  deleteScenarioGroup(scenarioGroup: ScenarioGroupExtended): void {
    scenarioGroup._loading = true;

    this.scenarioService
      .deleteScenarioGroup(scenarioGroup.id)
      .pipe(first())
      .subscribe({
        next: () => {
          this.toastrService.success(`Scenario group successfully deleted`, 'Delete', { duration: 5000 });
          scenarioGroup._loading = false;
        },
        error: () => {
          scenarioGroup._loading = false;
        }
      });
  }

  closeSidePanel(panel?: 'edit' | 'export' | 'import' | 'settings'): void {
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
      case 'settings':
        this.isSidePanelOpen.settings = false;
        break;
      default:
        for (let panel in this.isSidePanelOpen) {
          this.isSidePanelOpen[panel] = false;
        }
        this.scenarioGroupEdit = undefined;
        break;
    }
  }

  deleteScenarioVersion({ scenarioGroup, scenarioVersion }): void {
    this.loading.delete = true;

    this.scenarioService
      .deleteScenarioVersion(scenarioGroup.id, scenarioVersion.id)
      .pipe(first())
      .subscribe({
        next: () => {
          this.toastrService.success(`Scenario version successfully deleted`, 'Delete', { duration: 5000 });
          this.loading.delete = false;
        },
        error: () => {
          this.loading.delete = false;
        }
      });
  }

  saveScenarioGroupUnknownAnswers({ scenarioGroup, unknownAnswers, redirect, i18nLabel }: ScenarioEditOnSave): void {
    this.loading.edit = true;

    if (scenarioGroup.unknownAnswerId && i18nLabel) {
      this.scenarioService
        .patchAnswer(i18nLabel, unknownAnswers)
        .pipe(take(1))
        .subscribe({
          next: (_) => {
            this.saveScenarioGroup(scenarioGroup, redirect);
          },
          error: () => {
            this.loading.edit = false;
            this.toastrService.danger('The update of the unknown answers did not proceed correctly', 'Error');
          }
        });
    } else {
      this.scenarioService
        .saveAnswers(unknownAnswers)
        .pipe(take(1))
        .subscribe({
          next: (i18nLabel: I18nLabel) => {
            scenarioGroup.unknownAnswerId = i18nLabel._id;
            this.saveScenarioGroup(scenarioGroup, redirect);
          },
          error: () => {
            this.loading.edit = false;
            this.toastrService.danger('The addition of the unknown answers did not proceed correctly', 'Error');
          }
        });
    }
  }

  saveScenarioGroup(scenarioGroup: ScenarioGroup, redirect: boolean): void {
    if (!scenarioGroup.id) {
      this.scenarioService
        .postScenarioGroup(scenarioGroup)
        .pipe(first())
        .subscribe({
          next: (newScenarioGroup) => {
            this.loading.edit = false;
            this.toastrService.success(`Scenario group successfully created`, 'Creation', { duration: 5000 });

            if (redirect) {
              const versionId: string = newScenarioGroup.versions[0].id;
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
        .updateScenarioGroup(scenarioGroup)
        .pipe(first())
        .subscribe({
          next: (newScenarioGroup) => {
            this.loading.edit = false;
            this.toastrService.success(`Scenario group successfully updated`, 'Update', { duration: 5000 });
            if (redirect) {
              this.scenarioService.redirectToDesigner(newScenarioGroup);
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
    const { search, tags, enabled } = filters;
    this.currentFilters = filters;

    this.filteredScenariosGroups = this.scenariosGroups.filter((scenarioGroup: ScenarioGroupExtended) => {
      if (
        search &&
        !(
          scenarioGroup.name.toUpperCase().includes(search.toUpperCase()) ||
          scenarioGroup.description.toUpperCase().includes(search.toUpperCase())
        )
      )
        return;

      if (tags?.length && !scenarioGroup.tags.some((tag) => tags.includes(tag))) return;

      if (enabled !== null && !(scenarioGroup.enabled === enabled)) return;

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
    if (this.scenarioEditComponent || this.scenarioImportComponent || this.scenarioSettingsComponent) {
      this.closeSidePanelCheck(this.scenarioEditComponent || this.scenarioImportComponent || this.scenarioSettingsComponent, () => {
        this.isSidePanelOpen.export = true;
      });
    } else {
      this.isSidePanelOpen.export = true;
    }
  }

  openImportScenario(): void {
    if (this.scenarioEditComponent || this.scenarioSettingsComponent) {
      this.closeSidePanelCheck(this.scenarioEditComponent || this.scenarioSettingsComponent, () => {
        this.isSidePanelOpen.import = true;
      });
    } else if (this.scenarioExportComponent) {
      this.closeSidePanel('export');
      this.isSidePanelOpen.import = true;
    } else {
      this.isSidePanelOpen.import = true;
    }
  }

  openSettings(): void {
    if (this.scenarioEditComponent || this.scenarioImportComponent) {
      this.closeSidePanelCheck(this.scenarioEditComponent || this.scenarioImportComponent, () => {
        this.isSidePanelOpen.settings = true;
      });
    } else if (this.scenarioExportComponent) {
      this.closeSidePanel('export');
      this.isSidePanelOpen.settings = true;
    } else {
      this.isSidePanelOpen.settings = true;
    }
  }

  toggleTickScenarioGroup(scenarioGroup: ScenarioGroupExtended): void {
    const partialScenarioGroup: ScenarioGroupUpdate = {
      id: scenarioGroup.id,
      name: scenarioGroup.name,
      category: scenarioGroup.category,
      description: scenarioGroup.description,
      enabled: !scenarioGroup.enabled,
      tags: scenarioGroup.tags,
      unknownAnswerId: scenarioGroup.unknownAnswerId
    };

    scenarioGroup._loading = true;

    this.scenarioService
      .updateScenarioGroup(partialScenarioGroup)
      .pipe(first())
      .subscribe({
        next: (updatedScenarioGroup) => {
          scenarioGroup._loading = false;
          this.toastrService.success(
            `The tick story associated with the scenario group "${updatedScenarioGroup.name}" has been correctly ${
              updatedScenarioGroup.enabled ? 'enabled' : 'disabled'
            }`,
            `${updatedScenarioGroup.enabled ? 'Enable' : 'Disable'}`,
            { duration: 5000 }
          );
        },
        error: () => {
          scenarioGroup._loading = false;
        }
      });
  }

  private closeSidePanelCheck(component: ScenarioEditComponent | ScenarioImportComponent | ScenariosSettingsComponent, cb: Function): void {
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
