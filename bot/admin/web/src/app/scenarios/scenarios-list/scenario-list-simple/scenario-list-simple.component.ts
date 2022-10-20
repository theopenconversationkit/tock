import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Router } from '@angular/router';

import { OrderBy } from '../../../shared/utils';
import { ScenarioGroupExtended, ScenarioVersion, SCENARIO_STATE } from '../../models';
import { StateService } from '../../../core-nlp/state.service';
import { ScenarioService } from '../../services/scenario.service';

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
  @Output() onOrderBy = new EventEmitter<OrderBy>();

  SCENARIO_STATE = SCENARIO_STATE;
  orderBy = 'name';
  orderByReverse = false;

  constructor(protected state: StateService, private router: Router, private scenarioService: ScenarioService) {}

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

  design(event: MouseEvent, scenarioGroup: ScenarioGroupExtended): void {
    event.stopPropagation();
    let scenarioToOpen: ScenarioVersion;
    const drafts = scenarioGroup.versions.filter((scn) => scn.state === SCENARIO_STATE.draft);
    if (drafts.length) {
      scenarioToOpen = drafts.sort((a, b) => {
        return new Date(b.creationDate).getTime() - new Date(a.creationDate).getTime();
      })[0];
    } else {
      const current = scenarioGroup.versions.filter((scn) => scn.state === SCENARIO_STATE.current);
      if (current.length) {
        scenarioToOpen = current[current.length - 1];
      } else {
        scenarioToOpen = scenarioGroup.versions[scenarioGroup.versions.length - 1];
      }
    }
    this.router.navigateByUrl(`/scenarios/${scenarioGroup.id}/${scenarioToOpen.id}`);
  }

  editScenarioGroup(event: MouseEvent, scenarioGroup: ScenarioGroupExtended): void {
    event.stopPropagation();
    this.onEditScenarioGroup.emit(scenarioGroup);
  }

  deleteScenarioGroup(event: MouseEvent, scenarioGroup: ScenarioGroupExtended): void {
    event.stopPropagation();
    this.onDeleteScenarioGroup.emit(scenarioGroup);
  }

  deleteScenarioVersion(event: MouseEvent, scenarioGroup: ScenarioGroupExtended, scenarioVersion: ScenarioVersion): void {
    event.stopPropagation();
    this.onDeleteScenarioVersion.emit({ scenarioGroup: scenarioGroup, scenarioVersion: scenarioVersion });
  }

  duplicate(scenarioGroup: ScenarioGroupExtended, scenarioVersion: ScenarioVersion): void {
    this.onDuplicateScenarioVersion.emit({ scenarioGroup, scenarioVersion });
  }

  download(scenarioGroup: ScenarioGroupExtended, scenarioVersion: ScenarioVersion): void {
    this.scenarioService.loadScenariosAndDownload(this.scenariosGroups, [{ id: scenarioGroup.id, versions: [scenarioVersion.id] }]);
  }
}
