export interface storyCollectorItem {
  id: number;
  parentIds?: number[];
  text: string;
  from: string;
  botAnswerType?: string;
}
