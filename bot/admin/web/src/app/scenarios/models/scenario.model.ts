import { Sentence } from '../../model/nlp';
import { TempSentence } from './designer.model';

export interface ScenarioGroup {
  id?: string;
  name: string;
  description?: string;
  category?: string;
  tags: string[];
  creationDate?: string;
  updateDate?: string;
  versions: ScenarioVersion[];
}

export type ScenarioGroupExtended = ScenarioGroup & { _expanded?: boolean };

export interface ScenarioVersion {
  id?: string;
  creationDate?: string;
  updateDate?: string;
  data?: ScenarioData;
  state: SCENARIO_STATE;
  comment?: string;
}

export type ScenarioVersionExtended = ScenarioVersion & { _name?: string; _scenarioGroupId?: string };

export enum SCENARIO_STATE {
  draft = 'DRAFT',
  current = 'CURRENT',
  archive = 'ARCHIVE'
}

export enum SCENARIO_MODE {
  writing = 'writing',
  casting = 'casting',
  production = 'production',
  publishing = 'publishing'
}
export interface ScenarioData {
  scenarioItems: ScenarioItem[];
  contexts?: ScenarioContext[];
  stateMachine?: MachineState;
  mode: SCENARIO_MODE;
}

export const SCENARIO_ITEM_FROM_CLIENT = 'client';
export type Scenario_item_from_client = typeof SCENARIO_ITEM_FROM_CLIENT;
export const SCENARIO_ITEM_FROM_BOT = 'bot';
export type Scenario_item_from_bot = typeof SCENARIO_ITEM_FROM_BOT;
export type ScenarioItemFrom = Scenario_item_from_client | Scenario_item_from_bot;

export interface ScenarioItem {
  id: number;
  parentIds?: number[];
  text: string;
  from: ScenarioItemFrom;
  final?: boolean;
  main?: boolean;

  intentDefinition?: ScenarioIntentDefinition;
  tickActionDefinition?: ScenarioActionDefinition;
}

export interface ScenarioIntentDefinition {
  label: string;
  name: string;
  category?: string;
  description?: string;
  intentId?: string;
  sentences?: TempSentence[];
  _sentences?: Sentence[];
  primary?: boolean;
}

export interface ScenarioActionDefinition {
  name: string;
  description?: string;
  inputContextNames?: ScenarioContextName[];
  outputContextNames?: ScenarioContextName[];
  handler?: string;
  answer?: string;
  answerId?: string;
  answerUpdate?: true;
  final?: boolean;
}

export type EntityTypeName = string;
export type EntityRole = string;
export type ScenarioContextName = string;
export interface ScenarioContext {
  name: ScenarioContextName;
  entityType?: EntityTypeName;
  entityRole?: EntityRole;
  type: 'string';
}
export interface MachineState {
  id: string;
  type?: string;
  initial?: string;
  states?: { [key: string]: MachineState };
  on?: { [key: string]: string };
}

export type ExportableScenarioGroup = { id: string; versions: string[] };
