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

import { Component, Input, OnInit } from '@angular/core';
import { NbDialogRef } from '@nebular/theme';
import { MetricResult, StorySummary } from '../../models';
import { ragStoryId, unknownIntentName } from '../metrics-board.component';

enum SortingCriteria {
  name,
  count
}

@Component({
  selector: 'tock-stories-hits',
  templateUrl: './stories-hits.component.html',
  styleUrls: ['./stories-hits.component.scss']
})
export class StoriesHitsComponent implements OnInit {
  @Input() stories: StorySummary[];
  @Input() storiesMetrics: MetricResult[];
  SortingCriteria = SortingCriteria;
  constructor(public dialogRef: NbDialogRef<StoriesHitsComponent>) {}

  ngOnInit(): void {
    this.processMetrics();
  }

  processedStoriesMetrics: { name: string; count: number; unknownStory?: boolean; deletedStories?: boolean; ragStory?: boolean }[];

  private processMetrics(): void {
    this.processedStoriesMetrics = [];

    let deletedStoriesNumber = 0;
    let deletedStoriesCount = 0;

    this.storiesMetrics.forEach((metric) => {
      const story = this.getStorySummaryById(metric.row.trackedStoryId);
      if (story) {
        this.processedStoriesMetrics.push({
          name: story.name,
          count: metric.count,
          unknownStory: story.intent.name === unknownIntentName
        });
      } else {
        if (metric.row.trackedStoryId === ragStoryId) {
          this.processedStoriesMetrics.push({
            name: 'RAG',
            count: metric.count,
            ragStory: true
          });
        } else {
          deletedStoriesNumber++;
          deletedStoriesCount += metric.count;
        }
      }
    });

    if (deletedStoriesCount > 0) {
      let deletedName = 'Deleted story';
      if (deletedStoriesNumber > 1) {
        deletedName = `Deleted stories (${deletedStoriesNumber})`;
      }
      this.processedStoriesMetrics.push({
        name: deletedName,
        count: deletedStoriesCount,
        deletedStories: true
      });
    }

    this.sortMetrics();
  }

  getStoriesHitsSum(): number {
    return this.processedStoriesMetrics.reduce((acc, current) => acc + current.count, 0);
  }

  private getStorySummaryById(id: string): StorySummary {
    return this.stories.find((story) => story._id === id);
  }

  sortCriteria: SortingCriteria = SortingCriteria.count;
  sortDirection: boolean = true;

  sortBy(criteria: SortingCriteria): void {
    if (this.sortCriteria === criteria) {
      this.sortDirection = !this.sortDirection;
    } else {
      this.sortCriteria = criteria;
    }
    this.sortMetrics();
  }

  sortMetrics(): void {
    if (this.sortCriteria === SortingCriteria.count) {
      this.processedStoriesMetrics.sort((a, b) => {
        if (this.sortDirection) return b.count - a.count;
        else return a.count - b.count;
      });
    }

    if (this.sortCriteria === SortingCriteria.name) {
      this.processedStoriesMetrics.sort((a, b) => {
        if (this.sortDirection) return a.name.localeCompare(b.name);
        else return b.name.localeCompare(a.name);
      });
    }
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
