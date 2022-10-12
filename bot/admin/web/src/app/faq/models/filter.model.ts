export interface FaqFilter {
  enabled: boolean;
  search: string;
  tags: Array<string>;
  sort;
}

export interface FaqTrainingFilter {
  search: string;
  showUnknown: boolean;
}
