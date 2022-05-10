export interface Scenario {
  id: number | null;
  name: string;
  category?: string;
  tags?: Array<string>;
  dateCreation: Date;
  dateModification?: Date;
  description?: string;
  data?: scenarioItem[];
}

export interface scenarioItem {
  id: number;
  parentIds?: number[];
  text: string;
  from: string;
  final?: boolean;
}

export interface Filter {
  search: string;
  tags: Array<string>;
}
