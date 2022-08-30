import { saveAs } from 'file-saver';
import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  Output,
  SimpleChanges
} from '@angular/core';

import { OrderBy } from '../../../shared/utils';
import { Saga, Scenario, SCENARIO_STATE } from '../../models';
import { StateService } from '../../../core-nlp/state.service';
import { normalizedSnakeCase } from '../../commons/utils';
import { DatePipe } from '@angular/common';
import { Subject } from 'rxjs';
import { Router } from '@angular/router';

export type SagaExtended = Saga & { collapsed?: boolean };

@Component({
  selector: 'tock-scenario-list-simple',
  templateUrl: './scenario-list-simple.component.html',
  styleUrls: ['./scenario-list-simple.component.scss']
})
export class ScenarioListSimpleComponent implements OnChanges, OnDestroy {
  destroy$ = new Subject();

  @Input() scenarios!: Scenario[];
  @Input() selectedScenario?: Scenario;

  @Output() onEdit = new EventEmitter<Saga>();
  @Output() onDeleteSaga = new EventEmitter<Saga>();
  @Output() onDuplicate = new EventEmitter<Scenario>();
  @Output() onDelete = new EventEmitter<Scenario>();
  @Output() onOrderBy = new EventEmitter<OrderBy>();

  SCENARIO_STATE = SCENARIO_STATE;
  orderBy = 'name';
  orderByReverse = false;

  sagas: SagaExtended[] = [];

  constructor(protected state: StateService, private datePipe: DatePipe, private router: Router) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.scenarios?.currentValue) {
      // grouping of scenarios by sagas. We don't just empty this.sagas every time because we want to keep their collapsed state
      this.scenarios.forEach((scenario) => {
        let existingSaga = this.sagas.find((saga) => saga.sagaId === scenario.sagaId);
        if (!existingSaga) {
          existingSaga = {
            sagaId: scenario.sagaId,
            name: scenario.name,
            description: scenario.description,
            category: scenario.category,
            tags: scenario.tags,
            scenarios: [scenario]
          };
          this.sagas.push(existingSaga);
        } else {
          if (!existingSaga.scenarios.find((scn) => scn.id === scenario.id)) {
            existingSaga.scenarios.push(scenario);
          }
        }
        // sorting by creation date to display an history list
        existingSaga.scenarios.sort((a, b) => {
          return new Date(a.createDate).getTime() - new Date(b.createDate).getTime();
        });
      });

      // clean deleted scenarios / sagas
      for (let index = this.sagas.length - 1; index >= 0; index--) {
        const saga = this.sagas[index];
        for (let indexScn = saga.scenarios.length - 1; indexScn >= 0; indexScn--) {
          const scenario = saga.scenarios[indexScn];
          if (!this.scenarios.find((scn) => scn.id === scenario.id)) {
            saga.scenarios.splice(indexScn, 1);
          }
        }
        if (!saga.scenarios.length) {
          this.sagas.splice(index, 1);
        }
      }
    }
  }

  collapsedChange(event, saga: SagaExtended) {
    saga.collapsed = event;
  }

  setOrderBy(criteria: string): void {
    if (criteria == this.orderBy) {
      this.orderByReverse = !this.orderByReverse;
    } else {
      this.orderBy = criteria;
      this.orderByReverse = false;
    }

    this.onOrderBy.emit({ criteria: this.orderBy, reverse: this.orderByReverse });
  }

  sagaHasDraft(saga: SagaExtended) {
    return saga.scenarios.find((scn) => scn.state === SCENARIO_STATE.draft);
  }

  design(event: MouseEvent, saga: SagaExtended) {
    event.stopPropagation();
    const drafts = saga.scenarios.filter((scn) => scn.state === SCENARIO_STATE.draft);
    const lastDraft = drafts.sort((a, b) => {
      return new Date(b.createDate).getTime() - new Date(a.createDate).getTime();
    });
    this.router.navigateByUrl('/scenarios/' + lastDraft[0].id);
  }

  edit(event: MouseEvent, saga: SagaExtended): void {
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
    var jsonBlob = new Blob([JSON.stringify(scenario)], {
      type: 'application/json'
    });

    saveAs(
      jsonBlob,
      this.state.currentApplication.name +
        '_' +
        this.state.currentLocale +
        '_scenario_' +
        normalizedSnakeCase(scenario.name) +
        '_' +
        this.datePipe.transform(new Date(), 'yyyy-MM-dd') +
        '.json'
    );
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
