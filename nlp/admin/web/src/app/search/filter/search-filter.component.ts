/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
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
import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';
import { Observable, of } from 'rxjs';

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
export class SearchFilterComponent implements OnInit, AfterViewInit {
  @Input()
  name: String;
  @Input()
  groups: Group[];
  @Input()
  noFilter: FilterOption;
  @Input()
  unknownFilter: FilterOption;
  @Input()
  activeFirst: boolean = false;
  @Input()
  cleanupIfSelected: boolean = false;

  @Output()
  filterChange: EventEmitter<string> = new EventEmitter<string>();

  @Input()
  selectedValue: any;
  filteredGroups$: Observable<Group[]>;
  private cachedValue: string;
  private skipBlur: boolean;

  @ViewChild('autoBlur') autoBlurElement: ElementRef;

  ngOnInit() {
    this.filteredGroups$ = of(this.groups);
    this.cachedValue = this.selectedValue;
  }

  ngAfterViewInit() {
    //force blur
    setTimeout(() => {
      this.skipBlur = true;
      this.autoBlurElement.nativeElement.blur();
    });
  }

  private filterChildren(children: FilterOption[], filterValue: string) {
    return children.filter((optionValue) => optionValue.label.toLowerCase().includes(filterValue));
  }

  private findChildren(children: FilterOption[], value: string): FilterOption[] {
    return children.filter((optionValue) => optionValue.label.toLowerCase() === value);
  }

  filter(value: string): Group[] {
    const filterValue = value.toLowerCase();
    return this.groups
      .map((group) => {
        return {
          name: group.name,
          children: this.filterChildren(group.children, filterValue)
        };
      })
      .filter((group) => group.children.length);
  }

  private find(value: string): Group[] {
    const normalizedValue = value.toLowerCase();
    return this.groups
      .map((group) => {
        return {
          name: group.name,
          children: this.findChildren(group.children, normalizedValue)
        };
      })
      .filter((group) => group.children.length > 0);
  }

  trackByFn(index, item) {
    return item.name;
  }

  displayOption = (option: string | FilterOption) => {
    if (option instanceof FilterOption) {
      return option.label;
    }
    return option;
  };

  onSelectedChange(selected: FilterOption) {
    if (selected === this.noFilter) {
      this.filteredGroups$ = of(this.filter(''));
    } else {
      this.filteredGroups$ = of(this.filter(selected.toString()));
    }
    if (selected) {
      this.cachedValue = selected.label ?? selected.toString();
      this.filterChange.emit(selected.value);
    }
  }

  onModelChange(value: string) {
    const inputValue = value.toString().trim();
    this.filteredGroups$ = of(this.filter(inputValue));
    if (inputValue === '' && this.noFilter) {
      this.filterChange.emit(this.noFilter.value);
    }
  }

  onBlur() {
    if (!this.skipBlur) {
      // Reset initial value when loosing focus
      setTimeout(() => this.resetInitialValue(), 500);
    }
    this.skipBlur = false;
  }

  onClick() {
    if (this.cleanupIfSelected) {
      setTimeout((_) => {
        this.cachedValue = this.selectedValue;
        this.selectedValue = '';
      });
    }
  }

  onDoubleClick() {
    this.cachedValue = this.selectedValue;
    this.selectedValue = '';
    setTimeout((_) => {
      const e = this.autoBlurElement.nativeElement;
      e.click();
      e.focus();
    });
  }

  private resetInitialValue() {
    const selected = this.selectedValue ? this.selectedValue : '';
    if (!(selected instanceof FilterOption)) {
      if (selected.trim().length !== 0) {
        const selectedGroup = this.find(this.selectedValue);
        if (selectedGroup.length !== 0) {
          this.onSelectedChange(selectedGroup[0].children[0]);
          return;
        }
      }
      if (this.cachedValue) {
        this.autoBlurElement.nativeElement.disabled = true;
        this.selectedValue = this.cachedValue;
        setTimeout(() => {
          this.autoBlurElement.nativeElement.disabled = false;
          this.cachedValue = this.selectedValue;
        }, 0);
      }
    }
  }
}
