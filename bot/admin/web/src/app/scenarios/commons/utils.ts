import {
  IntentDefinition,
  MachineState,
  Scenario,
  ScenarioItem,
  SCENARIO_ITEM_FROM_BOT,
  SCENARIO_ITEM_FROM_CLIENT,
  TickActionDefinition
} from '../models';

import { saveAs } from 'file-saver';

export function normalize(str: string): string {
  return str.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
}

export function normalizedSnakeCase(str: string): string {
  return normalize(str)
    .replace(/-/g, ' ')
    .replace(/\s+/g, ' ')
    .replace(/[^A-Za-z0-9_\s]*/g, '')
    .trim()
    .replace(/\s+/g, '_');
}

export function normalizedSnakeCaseUpper(str: string): string {
  return normalizedSnakeCase(str).toUpperCase();
}

export function normalizedCamelCase(str: string): string {
  return normalize(str)
    .trim()
    .toLowerCase()
    .replace(/[^a-zA-Z0-9]+(.)/g, (m, chr) => {
      return chr.toUpperCase();
    })
    .replace(/[^A-Za-z0-9]*/g, '');
}

export function stringifiedCleanScenario(scenario: Scenario): string {
  return JSON.stringify(scenario, function (key, value) {
    if (key.indexOf('_') == 0) return undefined;
    return value;
  });
}

export function getContrastYIQ(hexcolor: string): string {
  if (!hexcolor) return '';
  hexcolor = hexcolor.replace('#', '');
  var r = parseInt(hexcolor.substring(0, 2), 16);
  var g = parseInt(hexcolor.substring(2, 4), 16);
  var b = parseInt(hexcolor.substring(4, 6), 16);
  var yiq = (r * 299 + g * 587 + b * 114) / 1000;
  return yiq >= 128 ? 'black' : 'white';
}

export function revertTransformMatrix(el: Element, transformedParent: Element): DOMRect {
  var brect = el.getBoundingClientRect();
  var style = getComputedStyle(transformedParent);
  var transformation = style.transform;
  if (transformation === 'none') return brect;

  const matrix = new DOMMatrix(transformation).inverse();
  const topleft = new DOMPoint(brect.x, brect.y).matrixTransform(matrix);
  const bottomRight = new DOMPoint(brect.x + brect.width, brect.y + brect.height).matrixTransform(
    matrix
  );

  let x = topleft.x;
  let y = topleft.y;
  let w = bottomRight.x - topleft.x;
  let h = bottomRight.y - topleft.y;

  let rect = {
    x: x,
    y: y,
    width: w,
    height: h,
    top: y,
    right: x + w,
    bottom: y + h,
    left: x
  };
  let json = JSON.stringify(rect);
  return {
    ...rect,
    toJSON: function () {
      return json;
    }
  };
}

export function getScenarioIntents(scenario: Scenario): ScenarioItem[] {
  return scenario.data.scenarioItems.filter((item) => item.from === SCENARIO_ITEM_FROM_CLIENT);
}

export function getScenarioIntentDefinitions(scenario: Scenario): IntentDefinition[] {
  return getScenarioIntents(scenario)
    .filter((item) => item.intentDefinition)
    .map((item) => item.intentDefinition);
}

export function getScenarioActions(scenario: Scenario): ScenarioItem[] {
  return scenario.data.scenarioItems.filter((item) => item.from === SCENARIO_ITEM_FROM_BOT);
}

export function getScenarioActionDefinitions(scenario: Scenario): TickActionDefinition[] {
  return getScenarioActions(scenario)
    .filter((item) => item.tickActionDefinition)
    .map((item) => item.tickActionDefinition);
}

export function getSmTransitionByName(name: string, group: MachineState): string {
  let result = null;
  if (group.on) {
    for (let transitionName in group.on) {
      if (transitionName === name) {
        result = group.on[transitionName];
        break;
      }
    }

    if (!result) {
      for (let action in group.states) {
        result = getSmTransitionByName(name, group.states[action]);
        if (result) break;
      }
    }
  }

  return result;
}

export function getAllSmTransitions(group: MachineState, result = []): [string, string][] {
  if (group.on) {
    Object.entries(group.on).forEach((entry) => result.push(entry));
  }

  if (group.states) {
    for (let name in group.states) {
      getAllSmTransitions(group.states[name], result);
    }
  }

  return result;
}

export function getAllSmTransitionNames(group: MachineState): string[] {
  let results = new Set<string>();
  const transitions = getAllSmTransitions(group);
  transitions.forEach((entry) => results.add(entry[0]));

  return [...results];
}

export function getSmTransitionParentsByname(
  transitionName: string,
  group: MachineState,
  result: MachineState[] = []
): MachineState[] {
  if (group.on) {
    for (let transName in group.on) {
      if (transName === transitionName) {
        result.push(group);
      }
    }

    for (let action in group.states) {
      getSmTransitionParentsByname(transitionName, group.states[action], result);
    }
  }

  return result;
}

export function getSmTransitionParentByTarget(
  targetName: string,
  group: MachineState
): { parent: MachineState; intents: string[] } | null {
  let result;
  if (group.on) {
    for (let transitionName in group.on) {
      if (group.on[transitionName] === `#${targetName}`) {
        if (!result) result = { parent: group, intents: [transitionName] };
        else result.intents.push(transitionName);
      }
    }

    if (!result) {
      for (let action in group.states) {
        result = getSmTransitionParentByTarget(targetName, group.states[action]);
        if (result) break;
      }
    }
  }

  return result;
}

export function renameSmStateById(
  currentStateId: string,
  newStateId: string,
  group: MachineState
): void {
  const stateParent = getSmStateParentById(currentStateId, group);
  if (stateParent) {
    if (stateParent.initial === currentStateId) {
      stateParent.initial = newStateId;
    }
    stateParent.states[newStateId] = stateParent.states[currentStateId];
    delete stateParent.states[currentStateId];
  }

  const matchingTransitionsParent = getSmTransitionParentByTarget(currentStateId, group);
  if (matchingTransitionsParent) {
    matchingTransitionsParent.intents.forEach((transName) => {
      matchingTransitionsParent.parent.on[transName] = `#${newStateId}`;
    });
  }

  const state = getSmStateById(currentStateId, group);
  if (state) {
    state.id = newStateId;
  }
}

export function removeSmStateById(stateId: string, group: MachineState): void {
  if (!group) return;

  const targetingTransitionParent = getSmTransitionParentByTarget(stateId, group);
  if (targetingTransitionParent) {
    targetingTransitionParent.intents.forEach((intent) => {
      delete targetingTransitionParent.parent.on[intent];
    });
  }

  const actionStateParent = getSmStateParentById(stateId, group);
  if (actionStateParent) {
    if (actionStateParent.initial === stateId) {
      actionStateParent.initial = null;
    }
    if (actionStateParent.states[stateId]) {
      delete actionStateParent.states[stateId];
    }
  }
}

export function getSmStateById(id: string, group: MachineState): MachineState | null {
  let result = null;
  if (group.id === id) return group;
  else {
    if (group.states) {
      for (let name in group.states) {
        result = getSmStateById(id, group.states[name]);
        if (result) break;
      }
    }
  }

  return result;
}

export function getSmStateParentById(id: string, group: MachineState): MachineState | null {
  let result = null;
  if (group.states) {
    for (let name in group.states) {
      if (group.states[name].id === id) {
        result = group;
        break;
      }
    }

    if (!result) {
      for (let name in group.states) {
        result = getSmStateParentById(id, group.states[name]);
        if (result) break;
      }
    }
  }

  return result;
}

export function getAllSmStatesNames(group: MachineState, result: string[] = []): string[] {
  if (group.id) result.push(group.id);
  if (group.states) {
    for (let name in group.states) {
      getAllSmStatesNames(group.states[name], result);
    }
  }
  return result;
}

export function getAllSmNonGroupStatesNames(group: MachineState, result: string[] = []): string[] {
  if (group.id && !group.states) result.push(group.id);
  if (group.states) {
    for (let name in group.states) {
      getAllSmNonGroupStatesNames(group.states[name], result);
    }
  }
  return result;
}

export function readFileAsText(file: File) {
  return new Promise(function (resolve, reject) {
    let fr = new FileReader();

    fr.onload = function () {
      resolve({ fileName: file.name, data: fr.result });
    };

    fr.onerror = function () {
      reject(fr);
    };

    fr.readAsText(file);
  });
}

export function exportJsonDump(obj: Object, fileName: string) {
  saveAs(
    new Blob([JSON.stringify(obj)], {
      type: 'application/json'
    }),
    fileName + '.json'
  );
}
