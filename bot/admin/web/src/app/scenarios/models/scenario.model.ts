import { ParseQuery, Sentence } from '../../model/nlp';
export interface Scenario {
  id: string | null;
  name: string;
  category?: string;
  tags?: Array<string>;
  createDate: string;
  updateDate?: string;
  description?: string;
  data?: ScenarioData;
  applicationId: string;
  state: SCENARIO_STATE;
}

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
  contexts?: TickContext[];
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

  intentDefinition?: IntentDefinition;
  tickActionDefinition?: TickActionDefinition;
}

export interface IntentDefinition {
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
  answerUpdate?: true;
  final?: boolean;
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
export interface MachineState {
  id: string;
  type?: string;
  initial?: string;
  states?: { [key: string]: MachineState };
  on?: { [key: string]: string };
}

export interface Transition {
  name: string;
  target: string;
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
export interface TempEntity {
  type: string;
  role: string;
  start: number;
  end: number;
  entityColor: string;
  qualifiedRole?: string;
  subEntities: any[];
}
export interface TempClassification {
  entities: TempEntity[];
  intentId?: string;
}

export interface DependencyUpdateJob {
  type: 'creation' | 'update';
  done: boolean;
  data: ScenarioItem;
}

export interface IntegrityCheckResult {
  valid: boolean;
  reason?: string;
}

export interface TickStory {
  name: string;
  botId: string;
  storyId: string;
  description: string;
  sagaId: string;
  mainIntent: string;
  primaryIntents: string[];
  secondaryIntents: string[];
  contexts: TickContext[];
  actions: TickActionDefinition[];
  stateMachine: MachineState;
}
export interface Filter {
  search: string;
  tags: Array<string>;
}
