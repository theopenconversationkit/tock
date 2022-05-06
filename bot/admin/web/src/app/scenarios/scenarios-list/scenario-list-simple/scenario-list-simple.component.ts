import { Component, EventEmitter, Input, Output } from '@angular/core';

import { Scenario } from '../../models';

@Component({
  selector: 'tock-scenario-list-simple',
  templateUrl: './scenario-list-simple.component.html',
  styleUrls: ['./scenario-list-simple.component.scss']
})
export class ScenarioListSimpleComponent {
  @Input() scenarios!: Scenario[];

  @Output() handleEdit = new EventEmitter<Scenario>();
  @Output() handleDelete = new EventEmitter<Scenario>();

  orderBy = 'name';
  orderByReverse = false;
  setOrderBy(criteria) {
    if (criteria == this.orderBy) {
      this.orderByReverse = !this.orderByReverse;
    } else {
      this.orderBy = criteria;
      this.orderByReverse = false;
    }
  }

  edit(scenario: Scenario): void {
    this.handleEdit.emit(scenario);
  }

  delete(scenario: Scenario): void {
    this.handleDelete.emit(scenario);
  }
}
