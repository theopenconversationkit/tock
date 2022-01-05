/*
 * Copyright (C) 2017/2021 e-voyageurs technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

export function truncate(input?: string, len = 40): string {
  if (input && input.length > len) {
    return input.substring(0, len) + '...';
  }
  return input;
}

export function noAccents(value?: string): string {
  if (!value) {
    return value;
  }
  // see https://stackoverflow.com/questions/5700636/using-javascript-to-perform-text-matches-with-without-accented-characters
  return value.normalize('NFD').replace(/[\u0300-\u036f]/g, "");
}

export function verySimilar(a?: string, b?: string): boolean {
  const left = (noAccents(a) || '').trim();
  const right = (noAccents(b) || '').trim();

  return left === right;
}

export function somewhatSimilar(a?: string, b?: string): boolean {
  function simplify(value?: string): string | undefined {
    return value
      ?.replace(/[.,\/#!$%\^&\*;:{}=\-_`~()]/g,"") // no ponctuation
      ?.replace(/\s\s+/g, " "); // merge consecutive spaces
  }

  return verySimilar(simplify(a), simplify(b));
}
