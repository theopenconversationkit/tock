import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { tap } from 'rxjs/operators';
import {
  getAllSmNonGroupStatesNames,
  getAllSmTransitionNames,
  getScenarioActionDefinitions,
  getScenarioIntentDefinitions,
  getSmStateById,
  getSmTransitionByName,
  stringifiedCleanScenario
} from '../commons/utils';
import {
  IntegrityCheckResult,
  Scenario,
  SCENARIO_ITEM_FROM_BOT,
  SCENARIO_ITEM_FROM_CLIENT,
  SCENARIO_MODE
} from '../models';
import { ScenarioService } from '../services/scenario.service';

export const SCENARIO_STEPS_ERRORS = {
  one_intervention_at_least: () =>
    'The scenario must contain at least one customer intervention and one bot response',
  interventions_text_must_be_filled: () =>
    'The texts of the customer and bot interventions must be filled in',
  client_intervention_should_have_intent: (txt) =>
    `An intent must be defined for each client intervention. The "${txt}" client intervention does not have an intent defined.`,
  bot_intervention_should_have_action: (txt) =>
    `An action must be defined for each bot intervention. The "${txt}" bot intervention does not have an action defined.`,
  input_context_should_exist_as_output: (txt) =>
    `For each context declared as input to an action, there must be at least one other action producing the same context as output. The context "${txt}" was not found as an output of any other action.`,
  output_context_should_exist_as_input: (txt) =>
    `For each context declared as output to an action, there must be at least one other action requiring the same context as input. The context "${txt}" was not found as an input of any other action.`,
  statemachine_should_be_defined: () => 'A valid state machine must be defined',
  intents_should_be_transitions: (txt) =>
    `For each defined intent there must be a transition with the same name in the state machine. No transition found for the intent "${txt}".`,
  transitions_should_be_intents: (txt) =>
    `For each transition in the state machine there must be a defined intent with the same name. No intent found for the transition "${txt}".`,
  actions_should_be_states: (txt) =>
    `For each defined action there must be a state with the same name in the state machine. No state found for the action "${txt}".`,
  states_should_be_actions: (txt) =>
    `For each state in the state machine there must be a defined action with the same name. No action found for the state "${txt}".`
};

@Injectable({
  providedIn: 'root'
})
export class ScenarioDesignerService {
  constructor(private scenarioService: ScenarioService, private router: Router) {}

  public scenarioDesignerCommunication = new Subject<any>();

  saveScenario(scenarioId: string, scenario: Scenario): Observable<Scenario> {
    const cleanScenario = JSON.parse(stringifiedCleanScenario(scenario));
    return this.scenarioService
      .putScenario(scenarioId, cleanScenario)
      .pipe(tap((data) => this.updateScenarioBackup(data)));
  }

  exitDesigner(): void {
    this.router.navigateByUrl('/scenarios');
  }

  updateScenarioBackup(data: Scenario): void {
    this.scenarioDesignerCommunication.next({
      type: 'updateScenarioBackup',
      data: data
    });
  }

  isStepValid(
    scenario: Scenario,
    step: SCENARIO_MODE,
    isSwitchAction: boolean = false
  ): IntegrityCheckResult {
    const scenarioItems = scenario.data.scenarioItems;

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
          if (isSwitchAction) {
            console.log(`BUG LOG POUR RODOLPHE : no intentDefinition for the item "${item.text}"`);
            console.log(item);
          }
          return {
            valid: false,
            reason: SCENARIO_STEPS_ERRORS.client_intervention_should_have_intent(item.text)
          };
        }
        if (item.from === SCENARIO_ITEM_FROM_BOT && !item.tickActionDefinition) {
          if (isSwitchAction) {
            console.log(
              `BUG LOG POUR RODOLPHE : no tickActionDefinition for the item "${item.text}"`
            );
            console.log(item);
          }
          return {
            valid: false,
            reason: SCENARIO_STEPS_ERRORS.bot_intervention_should_have_action(item.text)
          };
        }
      }

      const StoryValidity = this.checkStoryIntegrity(scenario);
      if (!StoryValidity.valid) return StoryValidity;
    }

    if (step === SCENARIO_MODE.publishing) {
      if (!scenario.data.stateMachine) {
        return {
          valid: false,
          reason: SCENARIO_STEPS_ERRORS.statemachine_should_be_defined()
        };
      }
      const SmValidity = this.checkStateMachineIntegrity(scenario);
      if (!SmValidity.valid) return SmValidity;
    }

    return { valid: true };
  }

  checkStoryIntegrity(scenario: Scenario): IntegrityCheckResult {
    const actionsDefinitions = getScenarioActionDefinitions(scenario);

    for (let index = 0; index < actionsDefinitions.length; index++) {
      const actionDef = actionsDefinitions[index];

      // Pour chaque contexte déclaré en entrée d'une action, il doit exister au moins une action produisant ce même contexte en sortie d'une autre action
      for (let index = 0; index < actionDef.inputContextNames.length; index++) {
        const inputContext = actionDef.inputContextNames[index];
        let outputContext = actionsDefinitions.find((actDef) => {
          return actDef !== actionDef && actDef.outputContextNames.includes(inputContext);
        });
        if (!outputContext) {
          return {
            valid: false,
            reason: SCENARIO_STEPS_ERRORS.input_context_should_exist_as_output(inputContext)
          };
        }
      }

      // Pour chaque contexte déclaré en sortie d'une action, il doit exister au moins une action nécessitant ce même contexte en entrée d'une autre action
      for (let index = 0; index < actionDef.outputContextNames.length; index++) {
        const outputContext = actionDef.outputContextNames[index];
        let inputContext = actionsDefinitions.find((actDef) => {
          return actDef !== actionDef && actDef.inputContextNames.includes(outputContext);
        });
        if (!inputContext) {
          return {
            valid: false,
            reason: SCENARIO_STEPS_ERRORS.output_context_should_exist_as_input(outputContext)
          };
        }
      }
    }

    return { valid: true };
  }

  checkStateMachineIntegrity(scenario: Scenario): IntegrityCheckResult {
    const stateMachine = scenario.data.stateMachine;

    const intentDefinitions = getScenarioIntentDefinitions(scenario);
    // Pour chaque intention (primaire et secondaire) déclarée dans la TickStory on doit trouver une transition portant le même nom dans la state machine
    for (let index = 0; index < intentDefinitions.length; index++) {
      const intentDef = intentDefinitions[index];
      console.log(intentDef);
      const transition = getSmTransitionByName(intentDef.name, scenario.data.stateMachine);
      if (!transition) {
        return {
          valid: false,
          reason: SCENARIO_STEPS_ERRORS.intents_should_be_transitions(intentDef.name)
        };
      }
    }

    const transitionsNames = getAllSmTransitionNames(scenario.data.stateMachine);
    // Pour chaque transition dans la state machine on doit trouver une intention du même nom dans la TickStory
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
    // Pour chaque action déclarée dans la TickStory on doit trouver un état portant le même nom dans la state machine
    for (let index = 0; index < actionsDefinitions.length; index++) {
      const actionDef = actionsDefinitions[index];
      const state = getSmStateById(actionDef.name, scenario.data.stateMachine);
      if (!state) {
        return {
          valid: false,
          reason: SCENARIO_STEPS_ERRORS.actions_should_be_states(actionDef.name)
        };
      }
    }

    const nonGroupStatesNames = getAllSmNonGroupStatesNames(scenario.data.stateMachine);
    // Pour chaque état de la state machine qui n'est pas un état de regroupement on doit trouver une action du même nom dans la TickStory
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
}
