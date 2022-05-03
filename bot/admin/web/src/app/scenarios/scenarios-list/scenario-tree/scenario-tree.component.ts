import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NbTreeGridDataSource, NbTreeGridDataSourceBuilder } from '@nebular/theme';

import { Scenario } from '../../models';
import { ScenarioService } from '../../services/scenario.service';

@Component({
  selector: 'tock-scenario-tree',
  templateUrl: './scenario-tree.component.html',
  styleUrls: ['./scenario-tree.component.scss']
})
export class ScenarioTreeComponent implements OnInit {
  @Input() scenarios!: Scenario[];

  @Output() handleEdit = new EventEmitter<Scenario>();
  @Output() handleDelete = new EventEmitter<Scenario>();

  actionsColumn = 'actions';
  categoryColumn = 'category';
  defaultColumns = ['name', 'description'];
  allColumns = [this.categoryColumn, ...this.defaultColumns, this.actionsColumn];

  dataSource: NbTreeGridDataSource<any>;

  constructor(
    private dataSourceBuilder: NbTreeGridDataSourceBuilder<any>,
    private scenarioService: ScenarioService
  ) {}

  ngOnInit(): void {
    this.dataSource = this.dataSourceBuilder.create(
      this.scenarioService.buildTreeNodeByCategory(this.scenarios)
    );
  }

  edit(scenario: Scenario): void {
    this.handleEdit.emit(scenario);
  }

  delete(scenario: Scenario): void {
    this.handleDelete.emit(scenario);
  }
}
