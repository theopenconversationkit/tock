export interface Scenario {
  id: number;
  name: string;
  category: string;
  tags: Array<string>;
  dateCreation: Date;
  dateModification?: Date;
  data?: scenarioItem[];
}

export interface scenarioItem {
  id: number;
  parentIds?: number[];
  text: string;
  from: string;
  final?: boolean;
}
