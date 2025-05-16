/*
 * Copyright (C) 2017/2025 SNCF Connect & Tech
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

import { includesArray, isPrimitive, orderBy } from './utils';

describe('OrderBy', () => {
  describe('should return type error if the argument is not an array', () => {
    [undefined, null, '', 'a', 'aaaaaa', 0, 1, 15, true, false].forEach((arg: any, i: number) => {
      it(`sort ${i}`, () => {
        expect(() => {
          orderBy(arg, '');
        }).toThrowError('invalid array argument. The parameter must be an array');
      });
    });
  });

  describe('should return type error if the field is not empty', () => {
    [undefined, null, ''].forEach((arg: any, i: number) => {
      it(`sort ${i}`, () => {
        expect(() => {
          orderBy([], arg);
        }).toThrowError('the field parameter cannot be empty');
      });
    });
  });

  it('should return a type error if the array does not contain only objects', () => {
    const array = [{ name: 'a' }, { name: 'b' }, 'a'];

    expect(() => {
      orderBy(array, 'name');
    }).toThrowError('invalid array argument. The array must contain only objects');
  });

  describe('should sort array by default in ascending order with an existing field', () => {
    [
      { arrayToSort: [], expectedResult: [] },
      { arrayToSort: [{ name: 'a' }], expectedResult: [{ name: 'a' }] },
      {
        arrayToSort: [{ name: 'a' }, { name: 'b' }, { name: 'c' }, { name: 'd' }],
        expectedResult: [{ name: 'a' }, { name: 'b' }, { name: 'c' }, { name: 'd' }]
      },
      {
        arrayToSort: [{ name: 'b' }, { name: 'd' }, { name: 'a' }, { name: 'c' }],
        expectedResult: [{ name: 'a' }, { name: 'b' }, { name: 'c' }, { name: 'd' }]
      },
      {
        arrayToSort: [
          { name: 'test' },
          { name: 'ok' },
          { name: 'conversation' },
          { name: null },
          { name: 'intention' },
          { name: 'pirate' },
          { name: '' },
          { name: undefined },
          { name: 'tête' },
          { name: 'pirates' },
          { name: 'évaluation' },
          { name: 'enlever' }
        ],
        expectedResult: [
          { name: 'conversation' },
          { name: 'enlever' },
          { name: 'évaluation' },
          { name: 'intention' },
          { name: 'ok' },
          { name: 'pirate' },
          { name: 'pirates' },
          { name: 'test' },
          { name: 'tête' },
          { name: null },
          { name: '' },
          { name: undefined }
        ]
      }
    ].forEach((item, i) => {
      it(`sort ${i}`, () => {
        expect(orderBy(item.arrayToSort, 'name')).toEqual(item.expectedResult);
      });
    });
  });

  describe('should sort an array in ascending order when 2 sort criteria are defined', () => {
    [
      {
        arrayToSort: [],
        expectedResult: []
      },
      {
        arrayToSort: [{ name: 'a', category: 'default' }],
        expectedResult: [{ name: 'a', category: 'default' }]
      },
      {
        arrayToSort: [
          { name: 'b', category: 'default' },
          { name: 'c', category: 'test' },
          { name: 'a', category: 'default' },
          { name: 'd', category: 'test' }
        ],
        expectedResult: [
          { name: 'a', category: 'default' },
          { name: 'b', category: 'default' },
          { name: 'c', category: 'test' },
          { name: 'd', category: 'test' }
        ]
      },
      {
        arrayToSort: [
          { name: 'c', category: null },
          { name: 'a', category: 'default' },
          { name: 'd', category: '' },
          { name: 'b', category: 'default' }
        ],
        expectedResult: [
          { name: 'a', category: 'default' },
          { name: 'b', category: 'default' },
          { name: 'c', category: null },
          { name: 'd', category: '' }
        ]
      },
      {
        arrayToSort: [
          { name: 'test', category: 'default' },
          { name: 'ok', category: null },
          { name: 'conversation', category: 'test' },
          { name: 'intention', category: 'default' },
          { name: 'pirate', category: 'test' },
          { name: '', category: '' },
          { name: 'test', category: 'test' },
          { name: null, category: null },
          { name: 'tête', category: 'default' },
          { name: 'pirates', category: 'default' },
          { name: 'évaluation', category: 'default' },
          { name: 'enlever', category: 'default' }
        ],
        expectedResult: [
          { name: 'enlever', category: 'default' },
          { name: 'évaluation', category: 'default' },
          { name: 'intention', category: 'default' },
          { name: 'pirates', category: 'default' },
          { name: 'test', category: 'default' },
          { name: 'tête', category: 'default' },
          { name: 'conversation', category: 'test' },
          { name: 'pirate', category: 'test' },
          { name: 'test', category: 'test' },
          { name: 'ok', category: null },
          { name: '', category: '' },
          { name: null, category: null }
        ]
      }
    ].forEach((item, i) => {
      it(`sort ${i}`, () => {
        expect(orderBy(item.arrayToSort, 'category', false, 'name')).toEqual(item.expectedResult);
      });
    });
  });
});

describe('isPrimitive', () => {
  it('should detect strings, numbers, booleans, null and other primitives', () => {
    expect(isPrimitive(0)).toBeTrue();
    expect(isPrimitive(1e21)).toBeTrue();
    expect(isPrimitive('1e21')).toBeTrue();
    expect(isPrimitive('*/-')).toBeTrue();
    expect(isPrimitive(true)).toBeTrue();
    expect(isPrimitive(null)).toBeTrue();
    expect(isPrimitive(Symbol('test'))).toBeTrue();
    expect(isPrimitive(BigInt('0x1fffffffffffff'))).toBeTrue();
  });

  it('should not detect objects and functions', () => {
    expect(isPrimitive({})).toBeFalse();
    expect(isPrimitive([])).toBeFalse();
    expect(isPrimitive(new Map())).toBeFalse();
    expect(isPrimitive(() => {})).toBeFalse();
  });
});

describe('includesArray', () => {
  it('should detect that the array contains the array', () => {
    expect(includesArray([[1, 2, 3]], [1, 2, 3])).toBeTrue();
    expect(includesArray([[1, 2, 3]], [3, 2, 1])).toBeFalse();
    expect(
      includesArray(
        [
          [1, 2, 3],
          [3, 2, 1]
        ],
        [3, 2, 1]
      )
    ).toBeTrue();
    expect(includesArray([[1, 2, 3], [[3, 2, 1]]], [3, 2, 1])).toBeFalse();
    expect(includesArray([{ arr: [1, 2, 3] }], [1, 2, 3])).toBeFalse();
    expect(includesArray([[{ a: 1 }]], [{ a: 1 }])).toBeFalse();
  });
});
