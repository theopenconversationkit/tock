import { orderBy } from './utils';

describe('OrderBy', () => {
  it('should return type error if the argument is not an array', () => {
    [undefined, null, '', 'a', 'aaaaaa', 0, 1, 15, true, false].forEach((arg: any) => {
      expect(() => {
        orderBy(arg, '');
      }).toThrowError('Invalid array argument');
    });
  });

  it('should return type error if the field is not empty', () => {
    [undefined, null, ''].forEach((arg) => {
      expect(() => {
        orderBy([], arg);
      }).toThrowError('The field parameter cannot be empty');
    });
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
