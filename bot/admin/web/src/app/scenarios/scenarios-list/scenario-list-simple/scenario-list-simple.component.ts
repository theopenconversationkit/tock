import { saveAs } from 'file-saver';
import { Component, EventEmitter, Input, Output } from '@angular/core';

import { OrderBy } from '../../../shared/utils';
import { Scenario } from '../../models';
import { StateService } from '../../../core-nlp/state.service';
import { normalizedSnakeCase } from '../../commons/utils';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'tock-scenario-list-simple',
  templateUrl: './scenario-list-simple.component.html',
  styleUrls: ['./scenario-list-simple.component.scss']
})
export class ScenarioListSimpleComponent {
  @Input() scenarios!: Scenario[];
  @Input() selectedScenario?: Scenario;

  @Output() onEdit = new EventEmitter<Scenario>();
  @Output() onDelete = new EventEmitter<Scenario>();
  @Output() onOrderBy = new EventEmitter<OrderBy>();

  orderBy = 'name';
  orderByReverse = false;

  constructor(protected state: StateService, private datePipe: DatePipe) {}

  setOrderBy(criteria: string): void {
    if (criteria == this.orderBy) {
      this.orderByReverse = !this.orderByReverse;
    } else {
      this.orderBy = criteria;
      this.orderByReverse = false;
    }

    this.onOrderBy.emit({ criteria: this.orderBy, reverse: this.orderByReverse });
  }

  edit(event: MouseEvent, scenario: Scenario): void {
    event.stopPropagation();
    this.onEdit.emit(scenario);
  }

  delete(event: MouseEvent, scenario: Scenario): void {
    event.stopPropagation();
    this.onDelete.emit(scenario);
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

  versions = [
    { date: '05/11/2022', freezed: true, current: false, description: "Correction d'une entité" },
    { date: '09/11/2022', freezed: true, current: false, description: 'Nouveau process métier' },
    { date: '18/11/2022', freezed: true, current: true, description: "Faute d'orthographe" },
    { date: '28/12/2022', freezed: false, current: false, description: 'Evolutions' }
  ];
}
