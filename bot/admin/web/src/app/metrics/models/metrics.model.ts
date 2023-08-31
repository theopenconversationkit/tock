export interface MetricResult {
  count: number;
  row: {
    trackedStoryId?: string;
    indicatorName?: string;
    indicatorValueName?: string;
    type?: string;
  };
}
