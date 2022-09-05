import { Component, EventEmitter, Input, OnDestroy, Output } from '@angular/core';

import { OrderBy } from '../../../shared/utils';
import { Saga, Scenario, SCENARIO_STATE } from '../../models';
import { StateService } from '../../../core-nlp/state.service';
import { exportJsonDump, normalizedSnakeCase } from '../../commons/utils';
import { DatePipe } from '@angular/common';
import { Subject } from 'rxjs';
import { Router } from '@angular/router';
@Component({
  selector: 'tock-scenario-list-simple',
  templateUrl: './scenario-list-simple.component.html',
  styleUrls: ['./scenario-list-simple.component.scss']
})
export class ScenarioListSimpleComponent implements OnDestroy {
  destroy$ = new Subject();

  // @Input() scenarios!: Scenario[];
  @Input() sagas!: Saga[];
  @Input() selectedScenario?: Scenario;

  @Output() onEdit = new EventEmitter<Saga>();
  @Output() onDeleteSaga = new EventEmitter<Saga>();
  @Output() onDuplicate = new EventEmitter<Scenario>();
  @Output() onDelete = new EventEmitter<Scenario>();
  @Output() onOrderBy = new EventEmitter<OrderBy>();

  SCENARIO_STATE = SCENARIO_STATE;
  orderBy = 'name';
  orderByReverse = false;

  constructor(protected state: StateService, private datePipe: DatePipe, private router: Router) {}

  setOrderBy(criteria: string): void {
    if (criteria == this.orderBy) {
      this.orderByReverse = !this.orderByReverse;
    } else {
      this.orderBy = criteria;
      this.orderByReverse = false;
    }

    this.onOrderBy.emit({ criteria: this.orderBy, reverse: this.orderByReverse });
  }

  sagaHasDraft(saga: Saga) {
    return saga.scenarios.find((scn) => scn.state === SCENARIO_STATE.draft);
  }

  design(event: MouseEvent, saga: Saga) {
    event.stopPropagation();
    let scenarioToOpen;
    const drafts = saga.scenarios.filter((scn) => scn.state === SCENARIO_STATE.draft);
    if (drafts.length) {
      scenarioToOpen = drafts.sort((a, b) => {
        return new Date(b.createDate).getTime() - new Date(a.createDate).getTime();
      })[0];
    } else {
      const current = saga.scenarios.filter((scn) => scn.state === SCENARIO_STATE.current);
      if (current) {
        scenarioToOpen = current[0];
      } else {
        scenarioToOpen = saga.scenarios[saga.scenarios.length - 1];
      }
    }
    this.router.navigateByUrl('/scenarios/' + scenarioToOpen.id);
  }

  edit(event: MouseEvent, saga: Saga): void {
    event.stopPropagation();
    this.onEdit.emit(saga);
  }

  deleteSaga(event: MouseEvent, saga: Saga) {
    event.stopPropagation();
    this.onDeleteSaga.emit(saga);
  }

  delete(event: MouseEvent, scenario: Scenario): void {
    event.stopPropagation();
    this.onDelete.emit(scenario);
  }

  duplicate(scenario: Scenario) {
    this.onDuplicate.emit(scenario);
  }

  download(scenario: Scenario) {
    const fileName = [
      this.state.currentApplication.name,
      'SCENARIO',
      normalizedSnakeCase(scenario.name),
      scenario.state,
      this.datePipe.transform(scenario.createDate, 'yyyy-MM-dd')
    ].join('_');

    exportJsonDump(scenario, fileName);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
