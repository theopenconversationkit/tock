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

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbDialogRef } from '@nebular/theme';
import { AnswerConfigurationType } from '../../../bot/model/story';
import { MetricResult, StorySummary } from '../../models';
import { StoriesHitsComponent } from './stories-hits.component';

const storiesSummaries: StorySummary[] = [
  {
    _id: '789',
    category: 'testCategory',
    currentType: AnswerConfigurationType.simple,
    metricStory: false,
    name: 'unknown Story',
    storyId: 'unknownStory',
    intent: { name: 'unknown' }
  },
  {
    _id: '123',
    category: 'testCategory',
    currentType: AnswerConfigurationType.simple,
    metricStory: false,
    name: 'test Story',
    storyId: 'testStory',
    intent: { name: 'testIntent' }
  },
  {
    _id: '456',
    category: 'testCategory2',
    currentType: AnswerConfigurationType.simple,
    metricStory: false,
    name: 'test Story 2',
    storyId: 'testStory2',
    intent: { name: 'testIntent2' }
  }
];

const storiesHits: MetricResult[] = [
  {
    row: {
      trackedStoryId: '789'
    },
    count: 1
  },
  {
    row: {
      trackedStoryId: '123'
    },
    count: 2
  },
  {
    row: {
      trackedStoryId: '456'
    },
    count: 6
  },
  {
    row: {
      trackedStoryId: 'deletedStoryId'
    },
    count: 3
  }
];

describe('StoriesHitsComponent', () => {
  let component: StoriesHitsComponent;
  let fixture: ComponentFixture<StoriesHitsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [StoriesHitsComponent],
      providers: [
        {
          provide: NbDialogRef,
          useValue: {}
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(StoriesHitsComponent);
    component = fixture.componentInstance;
    component.stories = storiesSummaries;
    component.storiesMetrics = storiesHits;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should sum stories hits', () => {
    expect(component.getStoriesHitsSum()).toEqual(12);
  });

  it('should process input data', () => {
    expect(component.processedStoriesMetrics).toEqual([
      {
        name: 'test Story 2',
        count: 6,
        unknownStory: false
      },
      {
        name: 'Deleted story',
        count: 3,
        deletedStories: true
      },
      {
        name: 'test Story',
        count: 2,
        unknownStory: false
      },
      {
        name: 'unknown Story',
        count: 1,
        unknownStory: true
      }
    ]);
  });
});
