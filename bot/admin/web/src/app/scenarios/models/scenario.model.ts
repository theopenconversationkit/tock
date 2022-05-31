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
  data?: scenarioItem[];
  mode: scenarioMode;
}

export const SCENARIO_ITEM_API_RESPONSETYPE_STRING = 'string';
export type scenario_item_api_responsetype_string = typeof SCENARIO_ITEM_API_RESPONSETYPE_STRING;
export const SCENARIO_ITEM_API_RESPONSETYPE_BOOLEAN = 'boolean';
export type scenario_item_api_responsetype_boolean = typeof SCENARIO_ITEM_API_RESPONSETYPE_BOOLEAN;
export const SCENARIO_ITEM_API_RESPONSETYPE_CODES = 'codes';
export type scenario_item_api_responsetype_codes = typeof SCENARIO_ITEM_API_RESPONSETYPE_CODES;
export type scenarioItemApiResponsetype =
  | scenario_item_api_responsetype_string
  | scenario_item_api_responsetype_boolean
  | scenario_item_api_responsetype_codes;
export interface apiCallDefinition {
  name: string;
  description: string;
  responseType: scenarioItemApiResponsetype;
  responseCodes?: string[];
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

export const SCENARIO_ITEM_FROM_CLIENT = 'client';
export type scenario_item_from_client = typeof SCENARIO_ITEM_FROM_CLIENT;
export const SCENARIO_ITEM_FROM_BOT = 'bot';
export type scenario_item_from_bot = typeof SCENARIO_ITEM_FROM_BOT;
export const SCENARIO_ITEM_FROM_API = 'api';
export type scenario_item_from_api = typeof SCENARIO_ITEM_FROM_API;
export type scenarioItemFrom =
  | scenario_item_from_client
  | scenario_item_from_bot
  | scenario_item_from_api;
export interface scenarioItem {
  id: number;
  parentIds?: number[];
  text: string;
  from: scenarioItemFrom;
  final?: boolean;
  intentDefinition?: intentDefinition;
  apiCallDefinition?: apiCallDefinition;
  apiResponse?: string;
}

export interface Filter {
  search: string;
  tags: Array<string>;
}
