import { Component, EventEmitter, Input, Output } from '@angular/core';

import { OrderBy } from '../../../shared/utils';
import { ScenarioGroupExtended, ScenarioVersion, SCENARIO_STATE } from '../../models';
import { StateService } from '../../../core-nlp/state.service';
import { ScenarioService } from '../../services/scenario.service';
import { ConfirmDialogComponent } from '../../../shared-nlp/confirm-dialog/confirm-dialog.component';
import { DialogService } from '../../../core-nlp/dialog.service';

@Component({
  selector: 'tock-scenario-list-simple',
  templateUrl: './scenario-list-simple.component.html',
  styleUrls: ['./scenario-list-simple.component.scss']
})
export class ScenarioListSimpleComponent {
  @Input() scenariosGroups!: ScenarioGroupExtended[];
  @Input() selectedScenarioGroup?: ScenarioGroupExtended;

  @Output() onEditScenarioGroup = new EventEmitter<ScenarioGroupExtended>();
  @Output() onDeleteScenarioGroup = new EventEmitter<ScenarioGroupExtended>();
  @Output() onDuplicateScenarioVersion = new EventEmitter<{ scenarioGroup: ScenarioGroupExtended; scenarioVersion: ScenarioVersion }>();
  @Output() onDeleteScenarioVersion = new EventEmitter<{ scenarioGroup: ScenarioGroupExtended; scenarioVersion: ScenarioVersion }>();
  @Output() onToggleScenarioGroup = new EventEmitter<ScenarioGroupExtended>();
  @Output() onOrderBy = new EventEmitter<OrderBy>();

  SCENARIO_STATE = SCENARIO_STATE;
  orderBy = 'name';
  orderByReverse = false;

  constructor(protected state: StateService, private dialogService: DialogService, private scenarioService: ScenarioService) {}

  setOrderBy(criteria: string): void {
    if (criteria == this.orderBy) {
      this.orderByReverse = !this.orderByReverse;
    } else {
      this.orderBy = criteria;
      this.orderByReverse = false;
    }

    this.onOrderBy.emit({ criteria: this.orderBy, reverse: this.orderByReverse });
  }

  scenarioGroupHasState(scenarioGroup: ScenarioGroupExtended, state: SCENARIO_STATE): boolean {
    return scenarioGroup.versions.find((scn) => scn.state === state) ? true : false;
  }

  scenarioGroupCollapsedChangeDebouncerFlag = false;
  onScenarioGroupCollapsedChange(scenarioGroup, event) {
    // debounce mechanism to workaround the nb-accordion-item collapsedChange emitter that always triggers twice in a row with incomprehensible values. The first call is ignored but a flag is set to allow the side effect to occur if a subsequent call is made less than 100ms later.
    if (!this.scenarioGroupCollapsedChangeDebouncerFlag) {
      this.scenarioGroupCollapsedChangeDebouncerFlag = true;
      setTimeout(() => {
        this.scenarioGroupCollapsedChangeDebouncerFlag = false;
      }, 100);
      return;
    }

    this.scenarioService.patchScenarioGroupState({ id: scenarioGroup.id, _expanded: !scenarioGroup._expanded });
  }

  design(event: PointerEvent, scenarioGroup: ScenarioGroupExtended): void {
    event.stopPropagation();
    this.scenarioService.redirectToDesigner(scenarioGroup);
  }

  editScenarioGroup(event: PointerEvent, scenarioGroup: ScenarioGroupExtended): void {
    event.stopPropagation();
    this.onEditScenarioGroup.emit(scenarioGroup);
  }

  deleteScenarioGroup(event: PointerEvent, scenarioGroup: ScenarioGroupExtended): void {
    event.stopPropagation();
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
        this.onDeleteScenarioGroup.emit(scenarioGroup);
      }
    });
  }

  deleteScenarioVersion(event: PointerEvent, scenarioGroup: ScenarioGroupExtended, scenarioVersion: ScenarioVersion): void {
    event.stopPropagation();
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
        this.onDeleteScenarioVersion.emit({ scenarioGroup: scenarioGroup, scenarioVersion: scenarioVersion });
      }
    });
  }

  duplicate(scenarioGroup: ScenarioGroupExtended, scenarioVersion: ScenarioVersion): void {
    this.onDuplicateScenarioVersion.emit({ scenarioGroup, scenarioVersion });
  }

  download(scenarioGroup: ScenarioGroupExtended, scenarioVersion: ScenarioVersion): void {
    this.scenarioService.loadScenariosAndDownload(this.scenariosGroups, [{ id: scenarioGroup.id, versions: [scenarioVersion.id] }]);
  }

  toggleTickEnabled(scenarioGroup: ScenarioGroupExtended): void {
    if (scenarioGroup._hasCurrentVersion) {
      let action = 'Enable';
      if (scenarioGroup.enabled) {
        action = 'Disable';
      }

      const dialogRef = this.dialogService.openDialog(ConfirmDialogComponent, {
        context: {
          title: `${action} tick story`,
          subtitle: `Are you sure you want to ${action.toLowerCase()} the tick story associated with this scenario ?`,
          action: action
        }
      });
      dialogRef.onClose.subscribe((result: string) => {
        if (result === action.toLowerCase()) {
          this.onToggleScenarioGroup.emit(scenarioGroup);
        }
      });
    }
  }
}
