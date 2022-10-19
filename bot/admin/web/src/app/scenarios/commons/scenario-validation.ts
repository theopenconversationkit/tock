import { IntegrityCheckResult, ScenarioVersion, SCENARIO_ITEM_FROM_BOT, SCENARIO_ITEM_FROM_CLIENT, SCENARIO_MODE } from '../models';

import {
  getAllSmNonGroupStatesNames,
  getAllSmTransitionNames,
  getScenarioActionDefinitions,
  getScenarioIntentDefinitions,
  getSmStateById,
  getSmTransitionByName
} from '../commons/utils';

export const SCENARIO_STEPS_ERRORS = {
  one_intervention_at_least: () => 'The scenario must contain at least one customer intervention and one bot response',
  interventions_text_must_be_filled: () => 'The texts of the customer and bot interventions must be filled in',
  client_intervention_should_have_intent: (txt: string) =>
    `An intent must be defined for each client intervention. The "${txt}" client intervention does not have an intent defined.`,
  bot_intervention_should_have_action: (txt: string) =>
    `An action must be defined for each bot intervention. The "${txt}" bot intervention does not have an action defined.`,
  input_context_should_exist_as_output: (txt: string) =>
    `For each context declared as input to an action, there must be at least one other action or one intent producing this same context as output. The context "${txt}" was not found as an output of any other action or intent.`,
  output_context_should_exist_as_input: (txt: string) =>
    `For each context declared as output to an action, there must be at least one other action requiring the same context as input. The context "${txt}" was not found as an input of any other action.`,
  intent_output_context_should_exist_as_action_input: (txt: string) =>
    `For each context declared as output to an intent, there must be at least one action requiring the same context as input. The context "${txt}" was not found as an input of any action.`,
  intents_should_have_at_least_one_sentence: (txt: string) =>
    `All intents must have at least one sentence. The intent "${txt}" have no sentence defined.`,
  statemachine_should_be_defined: () => 'A valid state machine must be defined',
  intents_should_be_transitions: (txt: string) =>
    `For each defined intent there must be a transition with the same name in the state machine. No transition found for the intent "${txt}".`,
  transitions_should_be_intents: (txt: string) =>
    `For each transition in the state machine there must be a defined intent with the same name. No intent found for the transition "${txt}".`,
  actions_should_be_states: (txt: string) =>
    `For each defined action there must be a state with the same name in the state machine. No state found for the action "${txt}".`,
  states_should_be_actions: (txt: string) =>
    `For each state in the state machine there must be a defined action with the same name. No action found for the state "${txt}".`
};

export function isStepValid(scenario: ScenarioVersion, step: SCENARIO_MODE): IntegrityCheckResult {
  const scenarioItems = scenario.data!.scenarioItems;

  if (step === SCENARIO_MODE.casting) {
    if (scenarioItems.length < 2)
      return {
        valid: false,
        reason: SCENARIO_STEPS_ERRORS.one_intervention_at_least()
      };
    for (let index = 0; index < scenarioItems.length; index++) {
      const item = scenarioItems[index];
      if (item.text.trim().length < 1)
        return {
          valid: false,
          reason: SCENARIO_STEPS_ERRORS.interventions_text_must_be_filled()
        };
    }
  }

  if (step === SCENARIO_MODE.production) {
    for (let index = 0; index < scenarioItems.length; index++) {
      const item = scenarioItems[index];
      if (item.from === SCENARIO_ITEM_FROM_CLIENT && !item.intentDefinition) {
        return {
          valid: false,
          reason: SCENARIO_STEPS_ERRORS.client_intervention_should_have_intent(item.text)
        };
      }
      if (item.from === SCENARIO_ITEM_FROM_BOT && !item.tickActionDefinition) {
        return {
          valid: false,
          reason: SCENARIO_STEPS_ERRORS.bot_intervention_should_have_action(item.text)
        };
      }
    }

    const StoryValidity = checkScenarioItemsIntegrity(scenario);
    if (!StoryValidity.valid) return StoryValidity;
  }

  if (step === SCENARIO_MODE.publishing) {
    if (!scenario.data!.stateMachine) {
      return {
        valid: false,
        reason: SCENARIO_STEPS_ERRORS.statemachine_should_be_defined()
      };
    }
    const SmValidity = checkStateMachineIntegrity(scenario);
    if (!SmValidity.valid) return SmValidity;
  }

  return { valid: true };
}

function checkScenarioItemsIntegrity(scenario: ScenarioVersion): IntegrityCheckResult {
  const actionsDefinitions = getScenarioActionDefinitions(scenario);
  const intentDefinitions = getScenarioIntentDefinitions(scenario);

  for (let index = 0; index < actionsDefinitions.length; index++) {
    const actionDef = actionsDefinitions[index];
    // For each context declared as input to an action, there must be at least one action or one intent producing this same context as output from another action
    for (let inCtxIndex = 0; inCtxIndex < actionDef.inputContextNames!.length; inCtxIndex++) {
      const inputContext = actionDef.inputContextNames![inCtxIndex];
      const actionsOutputContext = actionsDefinitions.find((actDef) => {
        return actDef !== actionDef && actDef.outputContextNames!.includes(inputContext);
      });
      const intentsOutputContext = intentDefinitions.find((intDef) => {
        return intDef.outputContextNames?.includes(inputContext);
      });
      if (!actionsOutputContext && !intentsOutputContext) {
        return {
          valid: false,
          reason: SCENARIO_STEPS_ERRORS.input_context_should_exist_as_output(inputContext)
        };
      }
    }

    // For each context declared at the output of an action, there must be at least one action requiring this same context at the input of another action
    for (let outCtxIndex = 0; outCtxIndex < actionDef.outputContextNames!.length; outCtxIndex++) {
      const outputContext = actionDef.outputContextNames![outCtxIndex];
      const inputContext = actionsDefinitions.find((actDef) => {
        return actDef !== actionDef && actDef.inputContextNames!.includes(outputContext);
      });
      if (!inputContext) {
        return {
          valid: false,
          reason: SCENARIO_STEPS_ERRORS.output_context_should_exist_as_input(outputContext)
        };
      }
    }
  }

  for (let index = 0; index < intentDefinitions.length; index++) {
    const intentDef = intentDefinitions[index];

    // For each context declared as the output of an intent, there must be at least one action requiring this same context as input
    for (let outCtxIndex = 0; outCtxIndex < intentDef.outputContextNames?.length; outCtxIndex++) {
      const outputContext = intentDef.outputContextNames![outCtxIndex];
      const inputContext = actionsDefinitions.find((actDef) => {
        return actDef.inputContextNames!.includes(outputContext);
      });
      if (!inputContext) {
        return {
          valid: false,
          reason: SCENARIO_STEPS_ERRORS.intent_output_context_should_exist_as_action_input(outputContext)
        };
      }
    }

    // intents must have at least one sentence
    if (!intentDef.sentences?.length && !intentDef._sentences?.length) {
      return {
        valid: false,
        reason: SCENARIO_STEPS_ERRORS.intents_should_have_at_least_one_sentence(intentDef.name)
      };
    }
  }

  return { valid: true };
}

function checkStateMachineIntegrity(scenario: ScenarioVersion): IntegrityCheckResult {
  const stateMachine = scenario.data!.stateMachine;

  const intentDefinitions = getScenarioIntentDefinitions(scenario);
  // For each intention (primary and secondary) declared in the TickStory we must find a transition with the same name in the state machine
  for (let index = 0; index < intentDefinitions.length; index++) {
    const intentDef = intentDefinitions[index];
    const transition = getSmTransitionByName(intentDef.name, scenario.data!.stateMachine!);
    if (!transition) {
      return {
        valid: false,
        reason: SCENARIO_STEPS_ERRORS.intents_should_be_transitions(intentDef.name)
      };
    }
  }

  const transitionsNames = getAllSmTransitionNames(scenario.data!.stateMachine!);
  // For each transition in the state machine we must find an intention of the same name in the TickStory
  for (let index = 0; index < transitionsNames.length; index++) {
    const transName = transitionsNames[index];
    if (!intentDefinitions.find((intDef) => intDef.name === transName)) {
      return {
        valid: false,
        reason: SCENARIO_STEPS_ERRORS.transitions_should_be_intents(transName)
      };
    }
  }

  const actionsDefinitions = getScenarioActionDefinitions(scenario);
  // For each action declared in the TickStory we must find a state with the same name in the state machine
  for (let index = 0; index < actionsDefinitions.length; index++) {
    const actionDef = actionsDefinitions[index];
    const state = getSmStateById(actionDef.name, scenario.data!.stateMachine!);
    if (!state) {
      return {
        valid: false,
        reason: SCENARIO_STEPS_ERRORS.actions_should_be_states(actionDef.name)
      };
    }
  }

  const nonGroupStatesNames = getAllSmNonGroupStatesNames(scenario.data!.stateMachine!);
  // For each state of the state machine that is not a grouping state, an action with the same name must be found in the TickStory
  for (let index = 0; index < nonGroupStatesNames.length; index++) {
    const stateName = nonGroupStatesNames[index];
    if (!actionsDefinitions.find((actDef) => actDef.name === stateName)) {
      return {
        valid: false,
        reason: SCENARIO_STEPS_ERRORS.states_should_be_actions(stateName)
      };
    }
  }

  return { valid: true };
}
