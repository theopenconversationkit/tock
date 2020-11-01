/*
 * Copyright (C) 2017/2020 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Observable, of} from 'rxjs';

export class FilterOption {
  value: any;
  label: string;

  constructor(value: any, label: string) {
    this.value = value;
    this.label = label;
  }

  toString = () => this.label;
}

export class Group {
  name: string;
  children: FilterOption[];

  constructor(name: string, children: FilterOption[]) {
    this.name = name;
    this.children = children;
  }
}

@Component({
  selector: 'tock-search-filter',
  templateUrl: './search-filter.component.html',
  styleUrls: ['./search-filter.component.css']
})
export class SearchFilterComponent implements OnInit {

  @Input()
  name: String;
  @Input()
  groups: Group[];
  @Input()
  noFilter: FilterOption;
  @Input()
  unknownFilter: FilterOption;

  @Output()
  filterChange: EventEmitter<string> = new EventEmitter<string>();

  selectedValue: string;
  filteredGroups$: Observable<Group[]>;


  ngOnInit() {
    this.filteredGroups$ = of(this.groups);
  }

  private filterChildren(children: FilterOption[], filterValue: string) {
    return children.filter(optionValue => optionValue.label.toLowerCase().includes(filterValue));
  }

  filter(value: string): Group[] {
    const filterValue = value.toLowerCase();
    return this.groups
      .map(group => {
        return {
          name: group.name,
          children: this.filterChildren(group.children, filterValue),
        };
      })
      .filter(group => group.children.length);
  }

  trackByFn(index, item) {
    return item.name;
  }

  displayOption = (option: string | FilterOption) => {
    if (option instanceof FilterOption) {
      return option.label;
    }
    return option;
  }

  onSelectedChange(selected: FilterOption) {
    if (selected === this.noFilter) {
      this.filteredGroups$ = of(this.filter(''));
    } else {
      this.filteredGroups$ = of(this.filter(selected.toString()));
    }
    if (selected) {
      this.filterChange.emit(selected.value);
    }
  }

  onModelChange(value: string) {
    const inputValue = value.toString().trim();
    this.filteredGroups$ = of(this.filter(inputValue));
    if (inputValue === '') {
      this.filterChange.emit(this.noFilter.value);
    }
  }
}
