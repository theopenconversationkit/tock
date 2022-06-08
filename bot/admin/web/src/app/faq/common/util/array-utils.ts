/*
 * Copyright (C) 2017/2022 e-voyageurs technologies
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

/**
 * Array related utilities
 */

/**
 * (position, item) of a given item in Array
 */
export type ArrayEntry<T> = {
  index: number,
  item: T;
}

/**
 * Find (position, item) of given item for specified equivalence function
 *
 * @param arr Array
 * @param item Item which is equivalent to another/itself item in array
 * @param equivalence Equivalence function
 */
export function findEntry<T>(arr: T[], item: T, equivalence: (a: T, b: T) => boolean): ArrayEntry<T> | undefined {
  return arr.map((arrItem, index) => {
    return {item: arrItem, index};
  }).filter(bucket => equivalence(bucket.item, item))[0];
}

/**
 * Get (position, item) for a given item which is equivalent to another/itself item in array
 *
 * @param arr Array
 * @param item Item which is equivalent to another/itself item in array
 * @param equivalence Equivalence function
 */
export function getEntry<T>(arr: T[], item: T, equivalence: (a: T, b: T) => boolean): ArrayEntry<T> {
  const found = findEntry(arr, item, equivalence);
  if (found === undefined) {
    throw new Error("Original element location lost"); // must never happens
  }
  return <ArrayEntry<T>>found;
}

/**
 * Get item position
 * @param arr
 * @param item
 * @param equivalence
 */
export function getPosition<T>(arr: T[], item: T, equivalence: (a: T, b: T) => boolean): number {
  const existingBucket = getEntry(arr, item, equivalence);

  return existingBucket.index;
}

export function hasItem<T>(arr: T[], item: T, equivalence: (a: T, b: T) => boolean): boolean {
  return arr.some(arrItem => equivalence(arrItem, item));
}
