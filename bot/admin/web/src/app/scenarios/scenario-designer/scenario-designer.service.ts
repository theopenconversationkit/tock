import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { tap } from 'rxjs/operators';
import {
  getAllSmTransitionNames,
  getScenarioActionDefinitions,
  getScenarioIntentDefinitions,
  getSmStateById,
  getSmTransitionByName,
  getSmStateParentById,
  stringifiedCleanScenario
} from '../commons/utils';
import {
  Scenario,
  SCENARIO_ITEM_FROM_BOT,
  SCENARIO_ITEM_FROM_CLIENT,
  SCENARIO_MODE
} from '../models';
import { ScenarioService } from '../services/scenario.service';

@Injectable({
  providedIn: 'root'
})
export class ScenarioDesignerService {
  constructor(private scenarioService: ScenarioService) {}

  public scenarioDesignerCommunication = new Subject<any>();

  saveScenario(scenarioId: number, scenario: Scenario): Observable<Scenario> {
    const cleanScenario = JSON.parse(stringifiedCleanScenario(scenario));
    return this.scenarioService
      .putScenario(scenarioId, cleanScenario)
      .pipe(tap((data) => this.updateScenarioBackup(data)));
  }

  updateScenarioBackup(data) {
    this.scenarioDesignerCommunication.next({
      type: 'updateScenarioBackup',
      data: data
    });
  }

  isStepValid(scenario: Scenario, step: SCENARIO_MODE): { valid: boolean; reason?: string } {
    const scenarioItems = scenario.data.scenarioItems;

    if (step === SCENARIO_MODE.casting) {
      if (scenarioItems.length < 2)
        return {
          valid: false,
          reason:
            'The scenario must contain at least one customer intervention and one bot response'
        };
      for (let index = 0; index < scenarioItems.length; index++) {
        const item = scenarioItems[index];
        if (item.text.trim().length < 1)
          return {
            valid: false,
            reason: 'The texts of the customer and bot interventions must be filled in'
          };
      }
    }

    if (step === SCENARIO_MODE.production) {
      for (let index = 0; index < scenarioItems.length; index++) {
        const item = scenarioItems[index];
        if (item.from === SCENARIO_ITEM_FROM_CLIENT && !item.intentDefinition)
          return {
            valid: false,
            reason: 'An intent must be defined for each client intervention'
          };
        if (item.from === SCENARIO_ITEM_FROM_BOT && !item.tickActionDefinition)
          return {
            valid: false,
            reason: 'An action must be defined for each bot intervention'
          };
      }
    }

    if (step === SCENARIO_MODE.publishing) {
      if (!scenario.data.stateMachine) {
        return {
          valid: false,
          reason: 'A valid state machine must be defined'
        };
      }
      const SmValidity = this.checkStateMachineIntegrity(scenario);
      if (!SmValidity.valid) return SmValidity;
    }

    return { valid: true };
  }

  checkStateMachineIntegrity(scenario: Scenario): { valid: boolean; reason?: string } {
    const stateMachine = scenario.data.stateMachine;

    // Pour chaque intention (primaire et secondaire) déclarée dans la TickStory on doit trouver une transition portant le même nom dans la state machine
    const intentDefinitions = getScenarioIntentDefinitions(scenario);
    for (let index = 0; index < intentDefinitions.length; index++) {
      const intentDef = intentDefinitions[index];
      const transition = getSmTransitionByName(intentDef.name, scenario.data.stateMachine);
      if (!transition) {
        return {
          valid: false,
          reason: `For each defined intent there must be a transition with the same name in the state machine. No transition found for the intent "${intentDef.name}"`
        };
      }
    }

    // Pour chaque transition dans la state machine on doit trouver une intention du même nom dans la TickStory
    const transitionsNames = getAllSmTransitionNames(scenario.data.stateMachine);
    for (let index = 0; index < transitionsNames.length; index++) {
      const transName = transitionsNames[index];
      if (!intentDefinitions.find((intDef) => intDef.name === transName)) {
        return {
          valid: false,
          reason: `For each transition in the state machine there must be a defined intent with the same name. No intent found for the transition ${transName}`
        };
      }
    }

    // Pour chaque action déclarée dans la TickStory on doit trouver un état portant le même nom dans la state machine
    // const actionsDefinitions = getScenarioActionDefinitions(scenario);
    // for (let index = 0; index < actionsDefinitions.length; index++) {
    //   const actionDef = actionsDefinitions[index];
    //   const state = getSmStateById(actionDef.answerId, scenario.data.stateMachine);
    //   console.log(actionDef, state);
    // }

    // Pour chaque état de la state machine qui n'est pas un état de regroupement on doit trouver une action du même nom dans la TickStory

    // Pour chaque action déclarant exécuter du code métier on doit trouver une classe portant le nom du handler déclaré dans l'action

    // Pour chaque contexte déclaré en entrée d'une action, il doit exister au moins une action produisant ce même contexte en sortie d'une autre action

    // Pour chaque contexte déclaré en sortie d'une action, il doit exister au moins une action nécessitant ce même contexte en entrée d'une autre action

    // Il ne peut pas y a voir 2 fois la même transition avec le même état d'origine

    return { valid: true };
  }
}
