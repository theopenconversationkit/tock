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

import { DashboardTopic, WidgetState } from '../../models/dashboard.model';

@Component({
  selector: 'tock-topics-answered',
  templateUrl: './topics-answered.component.html',
  styleUrls: ['./topics-answered.component.scss'],
  standalone: false
})
export class TopicsAnsweredComponent implements OnChanges {
  @Input() topics: DashboardTopic[] = [];
  @Input() state: WidgetState = WidgetState.loading;

  WidgetState = WidgetState;
  maxCount: number = 0;

  ngOnChanges(): void {
    this.maxCount = this.topics?.length ? Math.max(...this.topics.map((t) => t.count)) : 0;
  }

  share(topic: DashboardTopic): number {
    return this.maxCount ? (topic.count / this.maxCount) * 100 : 0;
  }
}
