import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { NbTreeGridDataSource, NbTreeGridDataSourceBuilder } from '@nebular/theme';

import { Scenario } from '../../models';

@Component({
  selector: 'tock-scenario-tree',
  templateUrl: './scenario-tree.component.html',
  styleUrls: ['./scenario-tree.component.scss']
})
export class ScenarioTreeComponent implements OnChanges {
  @Input() scenarios!: Scenario[];

  @Output() onEdit = new EventEmitter<Scenario>();
  @Output() onDelete = new EventEmitter<Scenario>();

  actionsColumn = 'actions';
  categoryColumn = 'category';
  tagsColumn = 'tags';
  defaultColumns = ['name', 'description'];
  allColumns = [this.categoryColumn, ...this.defaultColumns, this.tagsColumn, this.actionsColumn];

  dataSource: NbTreeGridDataSource<any>;

  constructor(private dataSourceBuilder: NbTreeGridDataSourceBuilder<any>) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.scenarios.currentValue) {
      this.dataSource = this.dataSourceBuilder.create(
        this.buildTreeNodeByCategory(changes.scenarios.currentValue)
      );
    }
  }

  edit(scenario: Scenario): void {
    this.onEdit.emit(scenario);
  }

  delete(scenario: Scenario): void {
    this.onDelete.emit(scenario);
  }

  private buildTreeNodeByCategory(scenarios: Array<Scenario>): Array<any> {
    const scenariosByCatagory = new Map();
    const defaultCategory = 'default';

    scenarios.forEach((s) => {
      let category = scenariosByCatagory.get(s.category || defaultCategory);

      if (!category) {
        category = [];
        scenariosByCatagory.set(s.category || defaultCategory, category);
      }

      category.push(s);
    });

    scenariosByCatagory.forEach((t) => {
      t = t.sort((a: Scenario, b: Scenario) => {
        if (a.name.toUpperCase() < b.name.toUpperCase()) return -1;
        else if (a.name.toUpperCase() > b.name.toUpperCase()) return 1;
        else return 0;
      });
    });

    return Array.from(scenariosByCatagory, ([key, value]) => ({
      data: {
        category: key,
        expandable: true
      },
      children: value.map((v: Scenario) => {
        return {
          data: v
        };
      })
    })).sort((a, b) => {
      if (a.data.category.toUpperCase() < b.data.category.toUpperCase()) return -1;
      else if (a.data.category.toUpperCase() > b.data.category.toUpperCase()) return 1;
      else return 0;
    });
  }
}
