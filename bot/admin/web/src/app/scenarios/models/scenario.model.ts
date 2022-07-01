import { Sentence } from '../../model/nlp';

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
  data?: ScenarioData;
  mode: scenarioMode;
}
export interface ScenarioData {
  scenarioItems: scenarioItem[];
  contexts?: TickContext[];
}

export type EntityTypeName = string;
export type EntityRole = string;
export type TickContextName = string;
export interface TickContext {
  name: TickContextName;
  entity?: [EntityTypeName, EntityRole];
  type: 'string';
}
export interface intentDefinition {
  label: string;
  name: string;
  category?: string;
  description?: string;
  intentId?: string;
  sentences?: string[];
  _sentences?: Sentence[];
}
export interface TickActionDefinition {
  name: string;
  description?: string;
  inputContexts?: TickContextName[];
  outputContexts?: TickContextName[];
  handler?: string;
  answer?: string;
}

export const SCENARIO_ITEM_FROM_CLIENT = 'client';
export type scenario_item_from_client = typeof SCENARIO_ITEM_FROM_CLIENT;
export const SCENARIO_ITEM_FROM_BOT = 'bot';
export type scenario_item_from_bot = typeof SCENARIO_ITEM_FROM_BOT;
export type scenarioItemFrom = scenario_item_from_client | scenario_item_from_bot;

export interface scenarioItem {
  id: number;
  parentIds?: number[];
  text: string;
  from: scenarioItemFrom;
  final?: boolean;

  intentDefinition?: intentDefinition;
  tickActionDefinition?: TickActionDefinition;
}

export interface Filter {
  search: string;
  tags: Array<string>;
}
