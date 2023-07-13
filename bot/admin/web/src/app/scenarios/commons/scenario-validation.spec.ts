import {
  ScenarioIntentDefinition,
  ScenarioVersion,
  ScenarioItem,
  ScenarioItemFrom,
  SCENARIO_MODE,
  SCENARIO_STATE,
  ScenarioActionDefinition
} from '../models';
import { isStepValid, SCENARIO_STEPS_ERRORS } from './scenario-validation';

const scenario: ScenarioVersion = {
  id: '1111111111111111111111',
  creationDate: '2022-08-17T09:57:14.428Z',
  updateDate: '2022-08-17T09:57:33.053Z',
  data: {
    mode: 'writing' as SCENARIO_MODE,
    scenarioItems: [{ id: 0, from: 'client' as ScenarioItemFrom, text: 'Main intent', main: true }] as ScenarioItem[],
    contexts: [],
    triggers: []
  },
  state: 'draft' as SCENARIO_STATE
};

describe('scenario-validation', () => {
  it('Should detect step validity', () => {
    let scenarioCopy: ScenarioVersion = JSON.parse(JSON.stringify(scenario));
    const scenarioItems = scenarioCopy.data.scenarioItems;

    let res = isStepValid(scenarioCopy, SCENARIO_MODE.casting);
    let expected: { valid: boolean; reason?: string } = {
      valid: false,
      reason: SCENARIO_STEPS_ERRORS.one_intervention_at_least()
    };
    expect(res).withContext('Should have one client and one bot interventions').toEqual(expected);

    scenarioItems.push({ id: 1, from: 'bot' as ScenarioItemFrom, text: '' });

    res = isStepValid(scenarioCopy, SCENARIO_MODE.casting);
    expected = {
      valid: false,
      reason: SCENARIO_STEPS_ERRORS.interventions_text_must_be_filled()
    };
    expect(res).withContext('Interventions text must be filled').toEqual(expected);

    scenarioItems[1].text = 'Bot response1';

    res = isStepValid(scenarioCopy, SCENARIO_MODE.production);
    expected = {
      valid: false,
      reason: SCENARIO_STEPS_ERRORS.client_intervention_should_have_intent('Main intent')
    };
    expect(res).withContext('Client interventions must have intent defined').toEqual(expected);

    scenarioItems[0].intentDefinition = { name: 'MainIntent' } as ScenarioIntentDefinition;

    res = isStepValid(scenarioCopy, SCENARIO_MODE.production);
    expected = {
      valid: false,
      reason: SCENARIO_STEPS_ERRORS.bot_intervention_should_have_action('Bot response1')
    };
    expect(res).withContext('Bot interventions must have action defined').toEqual(expected);

    scenarioItems[1].actionDefinition = {
      name: 'BOT_RESPONSE1',
      inputContextNames: ['TEST_CONTEXT'],
      outputContextNames: []
    } as ScenarioActionDefinition;

    res = isStepValid(scenarioCopy, SCENARIO_MODE.production);
    expected = {
      valid: false,
      reason: SCENARIO_STEPS_ERRORS.input_context_should_exist_as_output('TEST_CONTEXT')
    };
    expect(res).withContext('Input context must exist as output').toEqual(expected);

    scenarioItems.push({
      id: 2,
      from: 'bot' as ScenarioItemFrom,
      text: 'Bot response2',
      actionDefinition: {
        name: 'BOT_RESPONSE2',
        inputContextNames: [],
        outputContextNames: ['TEST_CONTEXT', 'TEST_CONTEXT2']
      } as ScenarioActionDefinition
    });

    res = isStepValid(scenarioCopy, SCENARIO_MODE.production);
    expected = {
      valid: false,
      reason: SCENARIO_STEPS_ERRORS.output_context_should_exist_as_input('TEST_CONTEXT2')
    };
    expect(res).withContext('Output context must exist as input').toEqual(expected);

    scenarioItems[1].actionDefinition.inputContextNames.push('TEST_CONTEXT2');

    res = isStepValid(scenarioCopy, SCENARIO_MODE.publishing);
    expected = {
      valid: false,
      reason: SCENARIO_STEPS_ERRORS.statemachine_should_be_defined()
    };
    expect(res).withContext('Statemachine should be defined').toEqual(expected);

    scenarioCopy.data.stateMachine = {
      id: 'root',
      type: 'parallel',
      states: {
        Global: {
          id: 'Global',
          states: {}
        }
      },
      initial: 'Global',
      on: {}
    };
    let stateMachine = scenarioCopy.data.stateMachine;

    res = isStepValid(scenarioCopy, SCENARIO_MODE.publishing);
    expected = {
      valid: false,
      reason: SCENARIO_STEPS_ERRORS.intents_should_be_transitions('MainIntent')
    };
    expect(res).withContext('All intents must be referenced in SM as transitions').toEqual(expected);

    stateMachine.states.Global['on'] = { MainIntent: 'test', TestTransition1: 'test' };

    res = isStepValid(scenarioCopy, SCENARIO_MODE.publishing);
    expected = {
      valid: false,
      reason: SCENARIO_STEPS_ERRORS.transitions_should_be_intents_or_triggers('TestTransition1')
    };
    expect(res).withContext('All SM transitions must correspond to intents').toEqual(expected);

    delete stateMachine.states.Global['on'].TestTransition1;

    res = isStepValid(scenarioCopy, SCENARIO_MODE.publishing);
    expected = {
      valid: false,
      reason: SCENARIO_STEPS_ERRORS.actions_should_be_states('BOT_RESPONSE1')
    };
    expect(res).withContext('All actions must exist in SM as states').toEqual(expected);

    stateMachine.states.Global.states = {
      BOT_RESPONSE1: {
        id: 'BOT_RESPONSE1'
      },
      BOT_RESPONSE2: {
        id: 'BOT_RESPONSE2'
      },
      TEST: {
        id: 'TEST'
      }
    };
    stateMachine.states.Global['on'].MainIntent = 'test';

    res = isStepValid(scenarioCopy, SCENARIO_MODE.publishing);
    expected = {
      valid: false,
      reason: SCENARIO_STEPS_ERRORS.states_should_be_actions('TEST')
    };
    expect(res).withContext('All SM states must correspond to actions').toEqual(expected);

    delete stateMachine.states.Global.states.TEST;

    res = isStepValid(scenarioCopy, SCENARIO_MODE.publishing);
    expected = {
      valid: true
    };
    expect(res).withContext('All steps are ok, validation passed').toEqual(expected);
  });
});
