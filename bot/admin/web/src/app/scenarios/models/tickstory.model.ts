import { MachineState, ScenarioActionDefinition, ScenarioContext } from './scenario.model';

export interface TickStory {
  name: string;
  botId: string;
  storyId: string;
  description: string;
  mainIntent: string;
  primaryIntents: string[];
  secondaryIntents: string[];
  contexts: ScenarioContext[];
  triggers: string[];
  actions: ScenarioActionDefinition[];
  stateMachine: MachineState;
  intentsContexts: intentsContext[];
  unknownAnswerConfigs: unknownAnswerConfig[];
}

export interface unknownAnswerConfig {
  exitAction?: string;
  action: string;
  intent: string;
  answerId: string;
  retryNb: number;
}

export interface intentsContext {
  intentName: string;
  associations: { actionName: string; contextNames: string[] }[];
}
