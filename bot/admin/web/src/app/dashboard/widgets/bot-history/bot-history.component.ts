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
import { Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';

import {
  BOT_HISTORY_EVENT_ICONS,
  BOT_HISTORY_SNAPSHOT_KINDS,
  BotHistoryEvent,
  BotHistoryEventType,
  INDEX_SESSION_FACET,
  INDEX_SESSION_FACET_ICON,
  INDEX_SESSION_SNAPSHOT_FIELD,
  WidgetState
} from '../../models/dashboard.model';

interface HistoryGroup {
  /** Year label, used as a sticky separator while scrolling back in time. */
  year: number;
  events: BotHistoryEvent[];
}

/**
 * A facet is either a real event type or the derived corpus facet. The corpus facet is
 * computed from the snapshot, never emitted by the backend: an index session change is
 * a plain RAG settings save, but it deserves to be spotted and filtered on its own.
 */
type HistoryFacet = BotHistoryEventType | typeof INDEX_SESSION_FACET;

interface TypeFilter {
  facet: HistoryFacet;
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

  @Output() onInspect = new EventEmitter<BotHistoryEvent>();

  WidgetState = WidgetState;
  readonly indexSessionFacet = INDEX_SESSION_FACET;

  filters: TypeFilter[] = [];
  groups: HistoryGroup[] = [];
  visibleCount: number = 0;

  ngOnChanges(): void {
    this.buildFilters();
    this.applyFilters();
  }

  /** True when this event's snapshot shows the index session moved. */
  isCorpusChange(event: BotHistoryEvent): boolean {
    const snapshot = event.snapshot;
    if (!snapshot?.previous) return false;

    return snapshot.previous[INDEX_SESSION_SNAPSHOT_FIELD] !== snapshot.current[INDEX_SESSION_SNAPSHOT_FIELD];
  }

  /** Facets an event belongs to: its own type, plus the corpus facet when relevant. */
  private facetsOf(event: BotHistoryEvent): HistoryFacet[] {
    return this.isCorpusChange(event) ? [event.type, INDEX_SESSION_FACET] : [event.type];
  }

  /** Translation key for the label, derived from the type; corpus wording when relevant. */
  labelKey(event: BotHistoryEvent): string {
    return this.isCorpusChange(event)
      ? `dashboard.history.event.${INDEX_SESSION_FACET}.label`
      : `dashboard.history.event.${event.type}.label`;
  }

  detailKey(event: BotHistoryEvent): string {
    return this.isCorpusChange(event)
      ? `dashboard.history.event.${INDEX_SESSION_FACET}.detail`
      : `dashboard.history.event.${event.type}.detail`;
  }

  /** Only events carrying interpolation values render a detail line. */
  hasDetail(event: BotHistoryEvent): boolean {
    return !!event.params && Object.keys(event.params).length > 0;
  }

  iconOf(event: BotHistoryEvent): string {
    return this.isCorpusChange(event) ? INDEX_SESSION_FACET_ICON : BOT_HISTORY_EVENT_ICONS[event.type];
  }

  /** Only config events carry a snapshot; those are the clickable ones. */
  hasSnapshot(event: BotHistoryEvent): boolean {
    return !!event.snapshot && !!BOT_HISTORY_SNAPSHOT_KINDS[event.type];
  }

  inspect(event: BotHistoryEvent): void {
    if (this.hasSnapshot(event)) {
      this.onInspect.emit(event);
    }
  }

  facetLabelKey(facet: HistoryFacet): string {
    return `dashboard.history.type.${facet}`;
  }

  private buildFilters(): void {
    const counts = new Map<HistoryFacet, number>();
    (this.events ?? []).forEach((event) => this.facetsOf(event).forEach((facet) => counts.set(facet, (counts.get(facet) ?? 0) + 1)));

    // Keep any selection the user already made when the data refreshes.
    const selection = new Map(this.filters.map((filter) => [filter.facet, filter.selected]));

    this.filters = [...counts.entries()]
      .sort(([, a], [, b]) => b - a)
      .map(([facet, count]) => ({
        facet,
        icon: facet === INDEX_SESSION_FACET ? INDEX_SESSION_FACET_ICON : BOT_HISTORY_EVENT_ICONS[facet as BotHistoryEventType],
        count,
        selected: selection.get(facet) ?? true
      }));
  }

  private applyFilters(): void {
    const active = new Set(this.filters.filter((filter) => filter.selected).map((filter) => filter.facet));
    const visible = (this.events ?? []).filter((event) => this.facetsOf(event).some((facet) => active.has(facet)));

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

  /**
   * Click on a facet isolates it — the common case is "show me only this". Clicking the
   * already-isolated facet clears the filter and shows everything again. Ctrl/Cmd-click
   * adds or removes a facet from the current selection for the rarer multi-facet view.
   */
  selectFilter(filter: TypeFilter, event: MouseEvent): void {
    const additive = event.ctrlKey || event.metaKey;

    if (additive) {
      const next = !filter.selected;
      if (!next && this.filters.filter((item) => item.selected).length === 1) {
        return;
      }
      filter.selected = next;
    } else {
      const isSoleSelection = filter.selected && this.filters.every((item) => item.selected === (item === filter));
      if (isSoleSelection) {
        this.filters.forEach((item) => (item.selected = true));
      } else {
        this.filters.forEach((item) => (item.selected = item === filter));
      }
    }

    this.applyFilters();
  }

  /** Whether every facet is currently shown (no active isolation). */
  get allSelected(): boolean {
    return this.filters.every((item) => item.selected);
  }

  showAll(): void {
    this.filters.forEach((item) => (item.selected = true));
    this.applyFilters();
  }
}
