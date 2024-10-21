import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { NbCalendarRange, NbDatepickerDirective, NbDateService, NbDialogService } from '@nebular/theme';
import type { EChartsOption } from 'echarts';
import { forkJoin, Observable, Subject, take, takeUntil } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AnalyticsService } from '../../analytics/analytics.service';
import { DialogFlowRequest } from '../../analytics/flow/flow';
import { UserAnalyticsQueryResult } from '../../analytics/users/users';
import { AnswerConfigurationType } from '../../bot/model/story';
import { RestService } from '../../core-nlp/rest/rest.service';
import { StateService } from '../../core-nlp/state.service';
import { BotConfigurationService } from '../../core/bot-configuration.service';
import { BotApplicationConfiguration } from '../../core/model/configuration';
import { heuristicValueColorDetection } from '../commons/utils';
import { IndicatorDefinition, IndicatorValueDefinition, MetricResult, StorySummary } from '../models';
import { MetricsByStoriesComponent } from './metrics-by-stories/metrics-by-stories.component';
import { StoriesHitsComponent } from './stories-hits/stories-hits.component';
import { toISOStringWithoutOffset } from '../../shared/utils';

export enum TimeRanges {
  day = 1,
  week = 7,
  month = 31,
  quarter = 92
}

enum StoriesFilterType {
  metricsStories = 'metricsStories',
  currentType = 'currentType',
  category = 'category'
}

export type StoriesFilter = { type: StoriesFilterType; value: string | AnswerConfigurationType };

export const unknownIntentName = 'unknown';
export const ragStoryId = 'tock_rag_story';

@Component({
  selector: 'tock-metrics-board',
  templateUrl: './metrics-board.component.html',
  styleUrls: ['./metrics-board.component.scss']
})
export class MetricsBoardComponent implements OnInit, OnDestroy {
  destroy = new Subject();
  loading: boolean = true;
  configurations: BotApplicationConfiguration[];
  range: NbCalendarRange<Date>;
  timeRanges = TimeRanges;
  indicators: IndicatorDefinition[];
  stories: StorySummary[];
  storiesFilterType = StoriesFilterType;

  @ViewChild(NbDatepickerDirective) dateRangeInputDirectiveRef;

  constructor(
    private stateService: StateService,
    private dateService: NbDateService<Date>,
    private analyticsService: AnalyticsService,
    private botConfiguration: BotConfigurationService,
    private rest: RestService,
    private nbDialogService: NbDialogService
  ) {
    this.range = {
      start: this.dateService.addDay(this.dateService.today(), -this.timeRanges.week),
      end: this.dateService.today()
    };
  }

  ngOnInit(): void {
    this.loading = true;
    this.botConfiguration.configurations.pipe(takeUntil(this.destroy)).subscribe((confs) => {
      this.configurations = confs;
      if (confs.length) {
        this.loadIndicatorsAndStories();
      }
    });
  }

  private loadIndicatorsAndStories(): void {
    const loaders = [this.getIndicatorsQuery().pipe(take(1)), this.getStoriesSummaryQuery().pipe(take(1))];

    forkJoin(loaders).subscribe(([indicators, stories]: [IndicatorDefinition[], StorySummary[]]) => {
      this.indicators = indicators;
      this.stories = stories;
      this.initStoriesFilters();
      this.initCurrentDimension();
      this.loadMetrics();
    });
  }

  private getIndicatorsQuery(): Observable<IndicatorDefinition[]> {
    const url = `/bot/${this.stateService.currentApplication.name}/indicators`;
    return this.rest.get(url, (indicators) => indicators);
  }

  private getStoriesSummaryQuery(): Observable<StorySummary[]> {
    const url = `/bot/story/search/summary`;
    const payload = {
      applicationName: this.stateService.currentApplication.name,
      namespace: this.stateService.currentApplication.namespace
    };
    return this.rest.post(url, payload);
  }

  private loadMetrics(): void {
    this.loading = true;

    const loaders = [
      this.getMessagesSearchQuery().pipe(take(1)),
      this.getStoriesHitsQuery().pipe(take(1)),
      this.getCurrentDimensionMetricsQuery().pipe(take(1))
    ];

    forkJoin(loaders).subscribe(([messages, storiesHits, dimensionMetrics]: [UserAnalyticsQueryResult, MetricResult[], MetricResult[]]) => {
      this.initMessagesChart(messages);
      this.storiesMetrics = storiesHits;
      this.initStoriesHitsChart();
      this.initCurrentDimensionMetricsChart(dimensionMetrics);
      this.loading = false;
    });
  }

  private loadCurrentDimensionMetrics(): void {
    this.loading = true;

    this.getCurrentDimensionMetricsQuery()
      .pipe(take(1))
      .subscribe((dimensionMetrics) => {
        this.initCurrentDimensionMetricsChart(dimensionMetrics);
        this.loading = false;
      });
  }

  private getMessagesSearchQuery(): Observable<UserAnalyticsQueryResult> {
    return this.analyticsService.messagesAnalytics(
      new DialogFlowRequest(
        this.stateService.currentApplication.namespace,
        this.stateService.currentApplication.name,
        this.stateService.currentLocale,
        this.stateService.currentApplication.name,
        undefined,
        undefined,
        toISOStringWithoutOffset(this.range.start),
        toISOStringWithoutOffset(this.range.end),
        !environment.production // In dev, we ask for test messages to dispose of some usable content
      )
    );
  }

  private getStoriesHitsQuery(): Observable<MetricResult[]> {
    const query = {
      filter: {
        types: ['STORY_HANDLED'],
        creationDateSince: this.range.start,
        creationDateUntil: this.range.end
      },
      groupBy: ['TRACKED_STORY_ID']
    };
    const url = `/bot/${this.stateService.currentApplication.name}/metrics`;
    return this.rest.post(url, query);
  }

  private messagesStatsData: UserAnalyticsQueryResult;
  messagesChartOptions: EChartsOption;

  private initMessagesChart(rawStats: UserAnalyticsQueryResult): void {
    this.messagesStatsData = rawStats;

    this.messagesChartOptions = {
      tooltip: {
        trigger: 'axis',
        formatter: function (params) {
          const plural = params[0].data > 0 ? 's' : '';
          return `${params[0].name}<br />${params[0].data} message${plural}`;
        }
      },
      xAxis: {
        data: rawStats.dates as any
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          type: 'line',
          smooth: true,
          areaStyle: {},
          data: rawStats.usersData.map((val) => val[0])
        }
      ]
    };
  }

  private storiesMetrics: MetricResult[];
  storiesChart: EChartsOption;

  private initStoriesHitsChart(): void {
    const filteredMetrics = [];
    let deletedStoriesHits = 0;

    this.storiesMetrics.forEach((metric) => {
      const story = this.getStorySummaryById(metric.row.trackedStoryId);

      // the story may be the Rag story or may have been deleted but still appear in the recorded hits
      if (story) {
        if (!story.metricStory || this.selectedStoriesFilters.find((filter) => filter.type === StoriesFilterType.metricsStories)) {
          if (
            this.selectedStoriesFilters.find((filter) => {
              return filter.type === StoriesFilterType.currentType && filter.value === story.currentType;
            }) &&
            this.selectedStoriesFilters.find((filter) => {
              return filter.type === StoriesFilterType.category && filter.value === story.category;
            })
          ) {
            filteredMetrics.push({
              value: metric.count,
              name: this.getStoryNameById(metric.row.trackedStoryId),
              color: story.intent.name === unknownIntentName ? '#aaaaaa' : undefined
            });
          }
        }
      } else {
        if (metric.row.trackedStoryId === ragStoryId) {
          filteredMetrics.push({
            value: metric.count,
            name: 'RAG',
            color: '#00d68f'
          });
        } else {
          deletedStoriesHits += metric.count;
        }
      }
    });

    if (deletedStoriesHits > 0) {
      filteredMetrics.push({
        value: deletedStoriesHits,
        name: 'Deleted Stories',
        color: '#000000'
      });
    }

    filteredMetrics.sort((a, b) => {
      return b.value - a.value;
    });

    let mainMetrics = filteredMetrics;

    const maxDisplayedStories = 30;
    if (mainMetrics.length > maxDisplayedStories) {
      mainMetrics = filteredMetrics.slice(0, maxDisplayedStories);
      const othersCount = filteredMetrics.slice(maxDisplayedStories).reduce((acc, current) => acc + current.value, 0);
      mainMetrics.push({ value: othersCount, name: `Other stories (${filteredMetrics.length - maxDisplayedStories})`, otherStories: true });
    }

    this.storiesChart = {
      tooltip: {
        trigger: 'item',
        formatter: function (params) {
          const plural = params.data.value > 1 ? 's' : '';
          return `Story <strong>${params.data.name}</strong> <br />was triggered <strong>${params.data.value} time${plural}</strong> (${params.percent}%)`;
        }
      },
      calculable: true,
      series: [
        {
          name: 'stories',
          type: 'pie',
          radius: [30, 110],
          roseType: 'area',
          itemStyle: {
            borderRadius: 4
          },

          data: mainMetrics.map((hit) => {
            return {
              value: hit.value,
              name: hit.name,
              itemStyle: { color: hit.color },
              otherStories: hit.otherStories
            };
          })
        }
      ]
    };
  }

  onStoriesChartClick(event) {
    this.nbDialogService.open(StoriesHitsComponent, {
      context: {
        storiesMetrics: this.storiesMetrics,
        stories: this.stories
      }
    });
  }

  currentDimension: string;

  private initCurrentDimension(): void {
    this.currentDimension = this.indicatorsDimensions[0];
  }

  dimensionSelected(dimension: string): void {
    this.currentDimension = dimension;
    this.loadCurrentDimensionMetrics();
  }

  get currentDimensionIndicators(): IndicatorDefinition[] {
    return this.indicators.filter((indicator) => indicator.dimensions.includes(this.currentDimension));
  }

  private getCurrentDimensionMetricsQuery(): Observable<MetricResult[]> {
    const query = {
      filter: {
        indicatorNames: this.currentDimensionIndicators.map((indicator) => indicator.name),
        creationDateSince: this.range.start,
        creationDateUntil: this.range.end
      },
      groupBy: ['TYPE', 'INDICATOR_NAME', 'INDICATOR_VALUE_NAME']
    };
    const url = `/bot/${this.stateService.currentApplication.name}/metrics`;
    return this.rest.post(url, query);
  }

  currentDimensionCharts: EChartsOption[];

  private initCurrentDimensionMetricsChart(dimensionMetrics: MetricResult[]): void {
    this.currentDimensionCharts = [];

    this.currentDimensionIndicators.forEach((indicator) => {
      const entries = [];

      const indicatorLabel = this.getIndicatorLabelByName(indicator.name);

      const indicatorMetrics = dimensionMetrics.filter((dimMetric) => dimMetric.row.indicatorName === indicator.name);

      const indicatorMetricsReplies = indicatorMetrics.filter((indMetric) => indMetric.row.type === 'QUESTION_REPLIED');

      let repliesCount = 0;
      indicatorMetricsReplies.forEach((imr) => {
        let indicatorValue = this.getIndicatorValueByName(imr.row.indicatorName, imr.row.indicatorValueName);
        if (indicatorValue) {
          const valueLabel = this.getIndicatorValueLabelByName(imr.row.indicatorName, imr.row.indicatorValueName);

          entries.push({
            value: imr.count,
            name: valueLabel,
            itemStyle: { color: heuristicValueColorDetection(valueLabel) }
          });
        }
        repliesCount += imr.count;
      });

      let indicatorMetricsQuestion = indicatorMetrics.find((indMetric) => indMetric.row.type === 'QUESTION_ASKED');
      if (indicatorMetricsQuestion) {
        const conversionRate = indicatorMetricsQuestion.count - repliesCount;
        if (conversionRate) {
          entries.push({
            value: conversionRate,
            name: 'No answer given',
            itemStyle: { color: '#aaa' }
          });
        }
      }

      this.currentDimensionCharts.push({
        name: indicatorLabel,
        indicatorName: indicator.name,
        tooltip: {
          trigger: 'item',
          formatter: function (params) {
            return `${params.data.name} :<br /><strong>${params.percent}%</strong> (${params.data.value})`;
          }
        },
        calculable: true,
        series: [
          {
            name: 'dimension metrics',
            type: 'pie',
            radius: [60, 110],
            itemStyle: {
              borderRadius: 4
            },
            data: entries
          }
        ]
      });
    });
  }

  loadIndicatorMetrics(indicatorName: string) {
    this.nbDialogService.open(MetricsByStoriesComponent, {
      context: {
        indicatorName: indicatorName,
        indicatorLabel: this.getIndicatorLabelByName(indicatorName),
        range: this.range,
        indicators: this.indicators,
        stories: this.stories
      }
    });
  }

  private getStorySummaryById(id: string): StorySummary {
    return this.stories.find((story) => story._id === id);
  }

  private getStoryNameById(id: string): string {
    return this.getStorySummaryById(id).name;
  }

  private getIndicatorByName(name: string): IndicatorDefinition {
    return this.indicators.find((indicator) => indicator.name === name);
  }

  private getIndicatorLabelByName(name: string): string {
    return this.getIndicatorByName(name).label;
  }

  private getIndicatorValueByName(indicatorname: string, indicatorValueName: string): IndicatorValueDefinition {
    return this.getIndicatorByName(indicatorname).values.find((value) => value.name === indicatorValueName);
  }

  private getIndicatorValueLabelByName(indicatorname: string, indicatorValueName: string): string {
    return this.getIndicatorValueByName(indicatorname, indicatorValueName).label;
  }

  hasUnknownStory(): boolean {
    if (!this.stories?.length) return false;
    return this.stories.some((story) => {
      return story.intent.name === unknownIntentName;
    });
  }

  get userMessagesSum(): number {
    return this.messagesStatsData?.usersData?.reduce((acc, current) => acc + current[0], 0) || 0;
  }

  get answeredQuestions(): number {
    const unknownStorySummary = this.stories?.find((story) => story.intent.name === unknownIntentName);
    if (unknownStorySummary) {
      return (
        this.storiesMetrics
          ?.filter((story) => story.row.trackedStoryId !== unknownStorySummary._id)
          .reduce((acc, current) => acc + current.count, 0) || 0
      );
    } else {
      return this.storiesMetrics?.reduce((acc, current) => acc + current.count, 0) || 0;
    }
  }

  get notUnderstoodQuestions(): number {
    if (!this.stories?.length || !this.storiesMetrics?.length) return 0;

    const unknownStorySummary = this.stories.find((story) => story.intent.name === unknownIntentName);

    const unknownStoryMetrics = this.storiesMetrics.find((story) => story.row.trackedStoryId === unknownStorySummary._id);
    return unknownStoryMetrics ? unknownStoryMetrics.count : 0;
  }

  get responseRate(): number {
    if (!this.answeredQuestions) return 0;

    return Math.round((100 - (this.notUnderstoodQuestions * 100) / this.answeredQuestions) * 100) / 100;
  }

  get indicatorsDimensions(): string[] {
    return [
      ...new Set(
        <string>[].concat.apply(
          [],
          this.indicators?.map((v: IndicatorDefinition) => v.dimensions)
        )
      )
    ];
  }

  get storiesTypes(): AnswerConfigurationType[] {
    return [...new Set(this.stories?.map((v: StorySummary) => v.currentType))];
  }

  get storiesCategories(): string[] {
    return [...new Set(this.stories?.map((v: StorySummary) => v.category))];
  }

  storiesFilters: StoriesFilter[];
  selectedStoriesFilters: StoriesFilter[];

  private initStoriesFilters(): void {
    const filters = [];
    this.storiesTypes.forEach((typeName) => {
      filters.push({ type: StoriesFilterType.currentType, value: typeName });
    });
    this.storiesCategories.forEach((category) => {
      filters.push({ type: StoriesFilterType.category, value: category });
    });
    this.storiesFilters = filters;
    this.selectedStoriesFilters = filters;
  }

  matchStoriesFilters(a: StoriesFilter, b: StoriesFilter): boolean {
    return a.type === b.type && a.value === b.value;
  }

  storiesFilterSelected(): void {
    this.initStoriesHitsChart();
  }

  setTimeRange(timeRange: TimeRanges): void {
    this.range.start = this.dateService.addDay(this.dateService.today(), -timeRange);
    this.range.end = this.dateService.today();

    this.dateRangeInputDirectiveRef.writeValue(this.range);
    this.loadMetrics();
  }

  datePickerChange(event: NbCalendarRange<Date>): void {
    if (event.end) {
      this.loadMetrics();
    }
  }

  doesStoriesHitsEcxeed(nb: number): boolean {
    return this.storiesMetrics?.length > nb;
  }

  helpModalRef;
  displayHelpModal(modalTemplateRef) {
    this.helpModalRef = this.nbDialogService.open(modalTemplateRef);
  }

  closeHelpModal() {
    this.helpModalRef.close();
  }

  ngOnDestroy() {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
