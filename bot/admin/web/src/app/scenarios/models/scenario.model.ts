export const SCENARIO_MODE_WRITING = 'writing';
export type scenario_mode_writing = typeof SCENARIO_MODE_WRITING;
export const SCENARIO_MODE_PRODUCTION = 'production';
export type scenario_mode_production = typeof SCENARIO_MODE_PRODUCTION;
export type scenarioMode = scenario_mode_writing | scenario_mode_production;

export interface Scenario {
  id: number | null;
  name: string;
  category?: string;
  tags?: Array<string>;
  creationDate: Date;
  updateDate?: Date;
  description?: string;
  data?: scenarioItem[];
  mode: scenarioMode;
}

export interface intentDefinition {
  label: string;
  name: string;
  category?: string;
  description?: string;
  intentId?: string;
}
export interface scenarioItem {
  id: number;
  parentIds?: number[];
  text: string;
  from: string;
  final?: boolean;
  intentDefinition?: intentDefinition;
  _sentences?: string[];
}

export interface Filter {
  search: string;
  tags: Array<string>;
}
