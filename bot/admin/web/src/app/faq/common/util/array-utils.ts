/**
 * Array related utilities
 */

export type Bucket<T> = {
  index: number,
  item: T;
}

/**
 * Get existing couple (position, item) equivalent to given item
 * @param arr
 * @param item
 * @param equivalence
 */
export function getBucket<T>(arr: T[], item: T, equivalence: (a: T, b: T) => boolean): Bucket<T> {
  const existingBucket = arr.map((arrItem, index) => {
    return { item: arrItem, index };
  }).filter(bucket => equivalence(bucket.item, item))[0];

  if (!existingBucket) {
    throw new Error("Original element location lost"); // must never happens
  }

  return existingBucket;
}

/**
 * Get item position
 * @param arr
 * @param item
 * @param equivalence
 */
export function getPosition<T>(arr: T[], item: T, equivalence: (a: T, b: T) => boolean): number {
  const existingBucket = getBucket(arr, item, equivalence);

  return existingBucket.index;
}

export function hasItem<T>(arr: T[], item: T, equivalence: (a: T, b: T) => boolean): boolean {
  return arr.some(arrItem => equivalence(arrItem, item));
}
