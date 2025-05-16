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
import { Router } from '@angular/router';
import {
  NbButtonModule,
  NbCardModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbTooltipModule,
  NbSelectModule,
  NbDialogService,
  NbToastrService
} from '@nebular/theme';
import { of } from 'rxjs';
import { DialogService } from '../../../core-nlp/dialog.service';
import { StateService } from '../../../core-nlp/state.service';
import { BotConfigurationService } from '../../../core/bot-configuration.service';

import { TestSharedModule } from '../../../shared/test-shared.module';
import { BotService } from '../../bot-service';
import { AnswerConfigurationType, IntentName, StoryDefinitionConfigurationSummary } from '../../model/story';
import { SearchStoryComponent } from './search-story.component';
import { Location } from '@angular/common';

const stories = [
  new StoryDefinitionConfigurationSummary(
    'teststorytwo',
    'new_assistant',
    { name: 'logement' } as IntentName,
    AnswerConfigurationType.simple,
    'faq',
    'Test story two',
    '987654321',
    '',
    new Date('2023-07-11T12:28:31.065Z'),
    undefined
  ),
  new StoryDefinitionConfigurationSummary(
    'teststoryone',
    'new_assistant',
    { name: 'logement' } as IntentName,
    AnswerConfigurationType.simple,
    'faq',
    'Test story one',
    '123456789',
    '',
    new Date('2023-07-12T10:00:00.065Z'),
    undefined
  )
];

describe('SearchStoryComponent', () => {
  let component: SearchStoryComponent;
  let fixture: ComponentFixture<SearchStoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        TestSharedModule,
        NbCardModule,
        NbFormFieldModule,
        NbIconModule,
        NbInputModule,
        NbTooltipModule,
        NbButtonModule,
        NbSelectModule
      ],
      declarations: [SearchStoryComponent],
      providers: [
        {
          provide: NbDialogService,
          useValue: { open: () => {} }
        },
        {
          provide: DialogService,
          useValue: { open: () => {} }
        },
        {
          provide: NbToastrService,
          useValue: { show: () => {} }
        },
        {
          provide: BotConfigurationService,
          useValue: { configurations: of([{}]) }
        },
        {
          provide: Location,
          useValue: {
            getState: () => {
              return { category: 'test' };
            }
          }
        },
        {
          provide: StateService,
          useValue: { currentApplication: { name: 'TestApp', namespace: 'TestNamespace' }, currentLocale: 'fr' }
        },
        {
          provide: BotService,
          useValue: {
            searchStories: () => {
              return of(stories);
            }
          }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchStoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load stories', () => {
    expect(component.stories).toEqual(stories);
  });

  it('should initialize stories categories', () => {
    const storyCategories = [
      {
        category: 'faq',
        stories: [stories[1], stories[0]]
      }
    ];

    expect(component.storyCategories).toEqual(storyCategories);
  });

  it('should initialize filtered stories', () => {
    expect(component.filteredStories).toEqual([stories[1], stories[0]]);
  });
});
