/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { NbCalendarRange, NbDateService, NbPopoverDirective } from '@nebular/theme';
import { StateService } from 'src/app/core-nlp/state.service';

@Component({
  selector: 'tock-date-range-calendar',
  templateUrl: './date-range-calendar.component.html',
  styleUrls: ['./date-range-calendar.component.css']
})
export class DateRangeCalendarComponent implements OnInit {
  range: NbCalendarRange<Date>;
  previousRange: NbCalendarRange<Date>;
  displayCalendar = false;

  @Input()
  rangeInDays = 7;

  @Input()
  disabled = false;

  @Output()
  datesChanged: EventEmitter<[Date, Date]> = new EventEmitter();

  @ViewChild(NbPopoverDirective) popover: NbPopoverDirective;

  constructor(protected dateService: NbDateService<Date>, private state: StateService) {}

  ngOnInit(): void {
    if (this.state.dateRange.start != null && this.state.dateRange.end != null && this.state.dateRange.rangeInDays != null) {
      this.range = {
        start: this.state.dateRange.start,
        end: this.state.dateRange.end
      };
      this.rangeInDays = this.state.dateRange.rangeInDays;
    }
    this.setRangeInDays(this.rangeInDays);
  }

  disabledOrModified(): boolean {
    return this.disabled || (this.displayCalendar && this.previousRange != this.range);
  }

  private open(): void {
    this.previousRange = this.range;
    this.displayCalendar = true;
  }

  getStatus(nbDays): string {
    if (this.rangeInDays == nbDays) {
      return 'primary';
    } else {
      return 'basic';
    }
  }

  setRangeInDays(days: number): void {
    this.rangeInDays = days;
    this.state.dateRange.rangeInDays = days;
    this.resetRange(days);
    this.update();
  }

  private resetRange(days: number): void {
    if (days == 0) {
      this.range = {
        start: this.dateService.today(),
        end: this.dateService.today()
      };
    } else if (days == 1) {
      const yesterday = new Date();
      yesterday.setDate(yesterday.getDate() - 1);
      this.range = {
        start: yesterday,
        end: yesterday
      };
    } else {
      const fromDate = new Date();
      fromDate.setDate(fromDate.getDate() - days + 1);
      this.range = {
        start: fromDate,
        end: this.dateService.today()
      };
    }
    this.normalizeDateTimes();
  }

  private normalizeDateTimes(): void {
    let start = null;
    let end = null;

    if (this.range.start != null) {
      start = new Date(JSON.parse(JSON.stringify(this.range.start))); // clone
      start.setHours(0);
      start.setMinutes(0);
      start.setSeconds(0);
    }
    if (this.range.end != null) {
      end = new Date(JSON.parse(JSON.stringify(this.range.end))); // clone
      end.setHours(23);
      end.setMinutes(59);
      end.setSeconds(59);
    }
    this.range = {
      start: start,
      end: end
    };
  }

  update(): void {
    this.displayCalendar = false;
    if (this.range.start != null) {
      this.normalizeDateTimes();
      this.previousRange = null;
      this.datesChanged.emit([this.range.start, this.range.end]);
      this.popover?.hide();
    }
  }
}
