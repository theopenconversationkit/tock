import { ParseQuery, Sentence } from '../../model/nlp';

export enum SCENARIO_MODE {
  writing = 'writing',
  casting = 'casting',
  production = 'production',
  publishing = 'publishing'
}

export interface Scenario {
  id: number | null;
  name: string;
  category?: string;
  tags?: Array<string>;
  creationDate: Date;
  updateDate?: Date;
  description?: string;
  data?: ScenarioData;
  mode: SCENARIO_MODE;
  applicationId: string;
  state: string;
}

export interface machineState {
  id: string;
  type?: string;
  initial?: string;
  states?: { [key: string]: machineState };
  on?: { [key: string]: string };
}
export interface ScenarioData {
  scenarioItems: scenarioItem[];
  contexts?: TickContext[];
  stateMachine?: machineState;
}

export type EntityTypeName = string;
export type EntityRole = string;
export type TickContextName = string;
export interface TickContext {
  name: TickContextName;
  entityType?: EntityTypeName;
  entityRole?: EntityRole;
  type: 'string';
}
export interface intentDefinition {
  label: string;
  name: string;
  category?: string;
  description?: string;
  intentId?: string;
  sentences?: TempSentence[];
  _sentences?: Sentence[];
  primary?: boolean;
}
export interface TickActionDefinition {
  name: string;
  description?: string;
  inputContextNames?: TickContextName[];
  outputContextNames?: TickContextName[];
  handler?: string;
  answer?: string;
  answerId?: string;
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
  main?: boolean;

  intentDefinition?: intentDefinition;
  tickActionDefinition?: TickActionDefinition;
}

export interface Filter {
  search: string;
  tags: Array<string>;
}

export interface TempClassification {
  entities: TempEntity[];
}

export interface TempEntity {
  type: string;
  role: string;
  start: number;
  end: number;
  entityColor: string;
}

export class TempSentence extends ParseQuery {
  public classification: TempClassification;

  constructor(
    public namespace: string,
    public applicationName: string,
    public language: string,
    public query: string,
    public checkExistingQuery: boolean,
    public state?: string
  ) {
    super(namespace, applicationName, language, query, checkExistingQuery);
    this.classification = { entities: [] };
  }
}
