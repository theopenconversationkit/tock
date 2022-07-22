import { machineState } from '../models';

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

export function getStateMachineActionParentById(id: string, group: machineState): machineState {
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
        result = getStateMachineActionParentById(id, group.states[name]);
        if (result) break;
      }
    }
  }

  return result;
}
