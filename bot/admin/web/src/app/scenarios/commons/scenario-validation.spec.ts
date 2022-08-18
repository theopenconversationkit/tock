import {
  IntentDefinition,
  Scenario,
  ScenarioItemFrom,
  SCENARIO_MODE,
  SCENARIO_STATE,
  TickActionDefinition
} from '../models';
import { isStepValid, SCENARIO_STEPS_ERRORS } from './scenario-validation';

const scenario = {
  id: '1111111111111111111111',
  name: 'testing scenario',
  category: 'scenarios',
  tags: ['testing'],
  applicationId: '11111111111111111',
  createDate: '2022-08-17T09:57:14.428Z',
  updateDate: '2022-08-17T09:57:33.053Z',
  description: '',
  data: {
    mode: 'writing' as SCENARIO_MODE,
    scenarioItems: [{ id: 0, from: 'client' as ScenarioItemFrom, text: 'Main intent', main: true }],
    contexts: []
  },
  state: 'draft' as SCENARIO_STATE
};

describe('scenario-validation', () => {
  it('should detect step validity', () => {
    let scenarioCopy: Scenario = JSON.parse(JSON.stringify(scenario));
    const scenarioItems = scenarioCopy.data.scenarioItems;

    let res = isStepValid(scenarioCopy, SCENARIO_MODE.casting);
    let expected: { valid: boolean; reason?: string } = {
      valid: false,
      reason: SCENARIO_STEPS_ERRORS.one_intervention_at_least()
    };
    expect(res).toEqual(expected);

    scenarioItems.push({ id: 1, from: 'bot' as ScenarioItemFrom, text: '' });

    res = isStepValid(scenarioCopy, SCENARIO_MODE.casting);
    expected = {
      valid: false,
      reason: SCENARIO_STEPS_ERRORS.interventions_text_must_be_filled()
    };
    expect(res).toEqual(expected);

    scenarioItems[1].text = 'Bot response1';

    res = isStepValid(scenarioCopy, SCENARIO_MODE.production);
    expected = {
      valid: false,
      reason: SCENARIO_STEPS_ERRORS.client_intervention_should_have_intent('Main intent')
    };
    expect(res).toEqual(expected);

    scenarioItems[0].intentDefinition = { name: 'MainIntent' } as IntentDefinition;

    res = isStepValid(scenarioCopy, SCENARIO_MODE.production);
    expected = {
      valid: false,
      reason: SCENARIO_STEPS_ERRORS.bot_intervention_should_have_action('Bot response1')
    };
    expect(res).toEqual(expected);

    scenarioItems[1].tickActionDefinition = {
      name: 'BOT_RESPONSE1',
      inputContextNames: ['TEST_CONTEXT'],
      outputContextNames: []
    } as TickActionDefinition;

    res = isStepValid(scenarioCopy, SCENARIO_MODE.production);
    expected = {
      valid: false,
      reason: SCENARIO_STEPS_ERRORS.input_context_should_exist_as_output('TEST_CONTEXT')
    };
    expect(res).toEqual(expected);

    scenarioItems.push({
      id: 2,
      from: 'bot' as ScenarioItemFrom,
      text: 'Bot response2',
      tickActionDefinition: {
        name: 'BOT_RESPONSE2',
        inputContextNames: [],
        outputContextNames: ['TEST_CONTEXT', 'TEST_CONTEXT2']
      } as TickActionDefinition
    });

    res = isStepValid(scenarioCopy, SCENARIO_MODE.production);
    expected = {
      valid: false,
      reason: SCENARIO_STEPS_ERRORS.output_context_should_exist_as_input('TEST_CONTEXT2')
    };
    expect(res).toEqual(expected);

    scenarioItems[1].tickActionDefinition.inputContextNames.push('TEST_CONTEXT2');

    res = isStepValid(scenarioCopy, SCENARIO_MODE.publishing);
    expected = {
      valid: false,
      reason: SCENARIO_STEPS_ERRORS.statemachine_should_be_defined()
    };
    expect(res).toEqual(expected);

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
    expect(res).toEqual(expected);

    stateMachine.states.Global['on'] = { MainIntent: 'test', TestTransition1: 'test' };

    res = isStepValid(scenarioCopy, SCENARIO_MODE.publishing);
    expected = {
      valid: false,
      reason: SCENARIO_STEPS_ERRORS.transitions_should_be_intents('TestTransition1')
    };
    expect(res).toEqual(expected);

    delete stateMachine.states.Global['on'].TestTransition1;

    res = isStepValid(scenarioCopy, SCENARIO_MODE.publishing);
    expected = {
      valid: false,
      reason: SCENARIO_STEPS_ERRORS.actions_should_be_states('BOT_RESPONSE1')
    };
    expect(res).toEqual(expected);

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
    expect(res).toEqual(expected);

    delete stateMachine.states.Global.states.TEST;

    res = isStepValid(scenarioCopy, SCENARIO_MODE.publishing);
    expected = {
      valid: true
    };
    expect(res).toEqual(expected);
  });
});
