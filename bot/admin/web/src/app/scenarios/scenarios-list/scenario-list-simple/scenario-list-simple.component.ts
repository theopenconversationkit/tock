import { Component, EventEmitter, Input, Output } from '@angular/core';

import { Scenario } from '../../models';

@Component({
  selector: 'tock-scenario-list-simple',
  templateUrl: './scenario-list-simple.component.html',
  styleUrls: ['./scenario-list-simple.component.scss']
})
export class ScenarioListSimpleComponent {
  @Input() scenarios!: Scenario[];

  @Output() onEdit = new EventEmitter<Scenario>();
  @Output() onDelete = new EventEmitter<Scenario>();

  orderBy = 'name';
  orderByReverse = false;

  setOrderBy(criteria: string): void {
    if (criteria == this.orderBy) {
      this.orderByReverse = !this.orderByReverse;
    } else {
      this.orderBy = criteria;
      this.orderByReverse = false;
    }
  }

  edit(event: MouseEvent, scenario: Scenario): void {
    event.stopPropagation();
    this.onEdit.emit(scenario);
  }

  delete(event: MouseEvent, scenario: Scenario): void {
    event.stopPropagation();
    this.onDelete.emit(scenario);
  }

  versions = [
    { date: '05/11/2022', freezed: true, current: false, description: "Correction d'une entité" },
    { date: '09/11/2022', freezed: true, current: false, description: 'Nouveau process métier' },
    { date: '18/11/2022', freezed: true, current: true, description: "Faute d'orthographe" },
    { date: '28/12/2022', freezed: false, current: false, description: 'Evolutions' }
  ];
}
