import { AnswerConfigurationType } from '../../bot/model/story';

export interface StorySummary {
  _id: string;
  category: string;
  currentType: AnswerConfigurationType;
  metricStory: boolean;
  name: string;
  storyId: string;
}
