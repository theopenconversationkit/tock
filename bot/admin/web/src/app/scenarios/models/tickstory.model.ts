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
  actions: ScenarioActionDefinition[];
  stateMachine: MachineState;
  intentsContexts: intentsContext[];
}

export interface intentsContext {
  intentName: string;
  associations: { actionName: string; contextNames: string[] }[];
}