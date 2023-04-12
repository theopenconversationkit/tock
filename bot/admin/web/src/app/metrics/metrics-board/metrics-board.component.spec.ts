import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  NbButtonModule,
  NbCardModule,
  NbDateService,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbTooltipModule,
  NbSelectModule
} from '@nebular/theme';
import { of } from 'rxjs';
import { AnalyticsService } from '../../analytics/analytics.service';
import { UserAnalyticsQueryResult } from '../../analytics/users/users';
import { AnswerConfigurationType } from '../../bot/model/story';
import { RestService } from '../../core-nlp/rest/rest.service';
import { StateService } from '../../core-nlp/state.service';
import { BotConfigurationService } from '../../core/bot-configuration.service';

import { TestSharedModule } from '../../shared/test-shared.module';
import { IndicatorDefinition, MetricResult, StorySummary } from '../models';
import { MetricsBoardComponent, StoriesFilter } from './metrics-board.component';

const indicator1: IndicatorDefinition = {
  name: 'test',
  label: 'test label',
  description: 'test desc',
  dimensions: ['test'],
  values: [{ name: 'oui', label: 'oui label' }]
};
const indicator2: IndicatorDefinition = {
  name: 'otherTest',
  label: 'Other Test',
  description: 'Other Test desc',
  dimensions: ['test', 'Other test dim'],
  values: []
};

const messagesStats: UserAnalyticsQueryResult = {
  dates: [new Date('2023-04-15'), new Date('2023-04-16'), new Date('2023-04-17')],
  usersData: [[0, 0], [5], [12]],
  connectorsType: [],
  intents: []
};

const storiesSummaries: StorySummary[] = [
  {
    _id: '123',
    category: 'testCategory',
    currentType: AnswerConfigurationType.simple,
    metricStory: false,
    name: 'test Story',
    storyId: 'testStory'
  },
  {
    _id: '456',
    category: 'testCategory2',
    currentType: AnswerConfigurationType.simple,
    metricStory: false,
    name: 'test Story 2',
    storyId: 'testStory2'
  }
];

const storiesHits: MetricResult[] = [
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
  }
];

const dimensionMetrics: MetricResult[] = [
  {
    row: {
      type: 'QUESTION_ASKED',
      indicatorName: 'test'
    },
    count: 2
  },
  {
    row: {
      type: 'QUESTION_REPLIED',
      indicatorName: 'test',
      indicatorValueName: 'oui'
    },
    count: 1
  }
];

describe('MetricsBoardComponent', () => {
  let component: MetricsBoardComponent;
  let fixture: ComponentFixture<MetricsBoardComponent>;

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
      declarations: [MetricsBoardComponent],
      providers: [
        {
          provide: NbDateService,
          useValue: {
            getMonthStart: () => '01',
            getMonthEnd: () => '31',
            addDay: (date: Date, days: number) => 'Mon Apr 10 2023 00:00:00 GMT+0200 (heure d’été d’Europe centrale)',
            today: () => 'Mon Apr 17 2023 11:15:09 GMT+0200 (heure d’été d’Europe centrale)'
          }
        },
        {
          provide: StateService,
          useValue: { currentApplication: { name: 'TestApp', namespace: 'TestNamespace' }, currentLocale: 'fr' }
        },
        {
          provide: AnalyticsService,
          useValue: { messagesAnalytics: () => of(messagesStats) }
        },
        {
          provide: BotConfigurationService,
          useValue: { configurations: of([{}]) }
        },
        {
          provide: RestService,
          useValue: {
            get: () => of([indicator1, indicator2]),
            post: (url, payload) => {
              if (url === '/bot/story/search/summary') {
                return of(storiesSummaries);
              }
              if (url === '/bot/TestApp/metrics') {
                if (payload.groupBy[0] === 'TRACKED_STORY_ID') {
                  return of(storiesHits);
                }
                return of(dimensionMetrics);
              }
            }
          }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MetricsBoardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should init stories filters', () => {
    const filters = [
      {
        type: 'currentType',
        value: 0
      } as StoriesFilter,
      {
        type: 'category',
        value: 'testCategory'
      } as StoriesFilter,
      {
        type: 'category',
        value: 'testCategory2'
      } as StoriesFilter
    ];
    expect(component.storiesFilters).toEqual(filters);
    expect(component.selectedStoriesFilters).toEqual(filters);
  });

  it('should list all existing dimensions', () => {
    expect(component.indicatorsDimensions).toEqual(['test', 'Other test dim']);
  });

  it('should init current dimension', () => {
    expect(component.currentDimension).toEqual('test');
  });

  it('should init stories hits chart', () => {
    expect(component.storiesChart.series[0].data).toEqual([
      {
        value: 2,
        name: 'test Story'
      },
      {
        value: 6,
        name: 'test Story 2'
      }
    ]);
  });

  it('should init messages stats chart', () => {
    expect(component.messagesChartOptions.series[0].data).toEqual([0, 5, 12]);
  });

  it('should compute sum of messages', () => {
    expect(component.userMessagesSum).toEqual(17);
  });

  it('should compute sum of answered messages', () => {
    expect(component.answeredMessages).toEqual(8);
  });

  it('should compute sum of unanswered messages', () => {
    expect(component.unAnsweredMessages).toEqual(9);
  });

  it('should compute response rate', () => {
    expect(component.responseRate).toEqual(47.06);
  });

  it('should retrieve indicators by name', () => {
    expect(component['getIndicatorByName']('test')).toEqual({
      name: 'test',
      label: 'test label',
      description: 'test desc',
      dimensions: ['test'],
      values: [
        {
          name: 'oui',
          label: 'oui label'
        }
      ]
    });
    expect(component['getIndicatorLabelByName']('test')).toEqual('test label');
    expect(component['getIndicatorValueLabelByName']('test', 'oui')).toEqual('oui label');
  });

  it('should update current dimension indicators after current dimension change', () => {
    component.dimensionSelected('Other test dim');
    expect(component.currentDimensionIndicators).toEqual([
      {
        name: 'otherTest',
        label: 'Other Test',
        description: 'Other Test desc',
        dimensions: ['test', 'Other test dim'],
        values: []
      }
    ]);
  });

  it('should init dimension indicators chart', () => {
    expect(component.currentDimensionCharts[0].series[0].data).toEqual([
      {
        value: 1,
        name: 'oui label'
      },
      {
        value: 1,
        name: 'No answer given',
        itemStyle: {
          color: '#aaa'
        }
      }
    ]);
  });
});
