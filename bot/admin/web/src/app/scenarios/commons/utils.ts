import {
  intentDefinition,
  machineState,
  Scenario,
  scenarioItem,
  SCENARIO_ITEM_FROM_BOT,
  SCENARIO_ITEM_FROM_CLIENT,
  TickActionDefinition
} from '../models';

export function normalize(str: string): string {
  return str.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
}

export function normalizedSnakeCase(str: string): string {
  return normalize(str)
    .trim()
    .replace(/\s+/g, '_')
    .replace(/[^A-Za-z0-9_]*/g, '')
    .toUpperCase();
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
  var r = parseInt(hexcolor.substr(0, 2), 16);
  var g = parseInt(hexcolor.substr(2, 2), 16);
  var b = parseInt(hexcolor.substr(4, 2), 16);
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

export function getScenarioIntents(scenario: Scenario): scenarioItem[] {
  return scenario.data.scenarioItems.filter((item) => item.from === SCENARIO_ITEM_FROM_CLIENT);
}

export function getScenarioIntentDefinitions(scenario: Scenario): intentDefinition[] {
  return getScenarioIntents(scenario)
    .filter((item) => item.intentDefinition)
    .map((item) => item.intentDefinition);
}

export function getScenarioActions(scenario: Scenario): scenarioItem[] {
  return scenario.data.scenarioItems.filter((item) => item.from === SCENARIO_ITEM_FROM_BOT);
}

export function getScenarioActionDefinitions(scenario: Scenario): TickActionDefinition[] {
  return getScenarioActions(scenario)
    .filter((item) => item.tickActionDefinition)
    .map((item) => item.tickActionDefinition);
}

export function getSmTransitionByName(name: string, group: machineState): string {
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

export function getAllSmTransitions(group: machineState, result = {}): object {
  if (group.on) Object.assign(result, group.on);
  if (group.states) {
    for (let name in group.states) {
      getAllSmTransitions(group.states[name], result);
    }
  }

  return result;
}

export function getAllSmTransitionNames(group: machineState, result = []): string[] {
  let results = [];
  const transitions = getAllSmTransitions(group);
  for (let name in transitions) {
    results.push(name);
  }

  return results;
}

export function getSmStateById(id: string, group: machineState): machineState | null {
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

export function getSmStateParentById(id: string, group: machineState): machineState | null {
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

export function getAllSmStatesNames(group, result = []) {
  if (group.id) result.push(group.id);
  if (group.states) {
    for (let name in group.states) {
      getAllSmStatesNames(group.states[name], result);
    }
  }
  return result;
}

export function getAllSmNonGroupStatesNames(group, result = []) {
  if (group.id && !group.states) result.push(group.id);
  if (group.states) {
    for (let name in group.states) {
      getAllSmNonGroupStatesNames(group.states[name], result);
    }
  }
  return result;
}
