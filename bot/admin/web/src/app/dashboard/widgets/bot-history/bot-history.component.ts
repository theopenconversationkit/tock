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
import { Component, Input, OnChanges } from '@angular/core';

import { BOT_HISTORY_EVENT_ICONS, BotHistoryEvent, BotHistoryEventType, WidgetState } from '../../models/dashboard.model';

interface HistoryGroup {
  /** Year label, used as a sticky separator while scrolling back in time. */
  year: number;
  events: BotHistoryEvent[];
}

interface TypeFilter {
  type: BotHistoryEventType;
  icon: string;
  count: number;
  selected: boolean;
}

@Component({
  selector: 'tock-bot-history',
  templateUrl: './bot-history.component.html',
  styleUrls: ['./bot-history.component.scss'],
  standalone: false
})
export class BotHistoryComponent implements OnChanges {
  /** Most recent first. */
  @Input() events: BotHistoryEvent[] = [];
  @Input() state: WidgetState = WidgetState.loading;

  WidgetState = WidgetState;

  filters: TypeFilter[] = [];
  groups: HistoryGroup[] = [];
  visibleCount: number = 0;

  ngOnChanges(): void {
    this.buildFilters();
    this.applyFilters();
  }

  private buildFilters(): void {
    const counts = new Map<BotHistoryEventType, number>();
    (this.events ?? []).forEach((event) => counts.set(event.type, (counts.get(event.type) ?? 0) + 1));

    // Keep any selection the user already made when the data refreshes.
    const selection = new Map(this.filters.map((filter) => [filter.type, filter.selected]));

    this.filters = [...counts.entries()]
      .sort(([, a], [, b]) => b - a)
      .map(([type, count]) => ({
        type,
        icon: BOT_HISTORY_EVENT_ICONS[type],
        count,
        selected: selection.get(type) ?? true
      }));
  }

  private applyFilters(): void {
    const active = new Set(this.filters.filter((filter) => filter.selected).map((filter) => filter.type));
    const visible = (this.events ?? []).filter((event) => active.has(event.type));

    this.visibleCount = visible.length;
    this.groups = visible.reduce<HistoryGroup[]>((groups, event) => {
      const year = new Date(event.date).getFullYear();
      const last = groups[groups.length - 1];

      if (last?.year === year) {
        last.events.push(event);
      } else {
        groups.push({ year, events: [event] });
      }

      return groups;
    }, []);
  }

  toggleFilter(filter: TypeFilter): void {
    filter.selected = !filter.selected;

    // Turning everything off would leave an empty timeline with no way back.
    if (!this.filters.some((item) => item.selected)) {
      filter.selected = true;
      return;
    }

    this.applyFilters();
  }

  iconOf(type: BotHistoryEventType): string {
    return BOT_HISTORY_EVENT_ICONS[type];
  }
}
