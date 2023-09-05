export function isPrimitive(arg): boolean {
  var type = typeof arg;
  return arg == null || (type != 'object' && type != 'function');
}
