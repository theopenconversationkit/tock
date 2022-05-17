import { OrderByPipe } from './orderBy.pipe';

describe('OrderByPipe', () => {
  const pipe = new OrderByPipe();

  it('should return type error if the argument is not an array', () => {
    [undefined, null, '', 'a', 'aaaaaa', 0, 1, 15, true, false].forEach((arg) => {
      expect(() => {
        pipe.transform(arg, '');
      }).toThrowError('Invalid array argument');
    });
  });

  it('should return type error if the field is not empty', () => {
    [undefined, null, ''].forEach((arg) => {
      expect(() => {
        pipe.transform([], arg);
      }).toThrowError('The field parameter cannot be empty');
    });
  });

  it('should sort array by default in ascending order with an existing field', () => {
    const expectedResult = [
      [],
      [{ name: 'a' }],
      [{ name: 'a' }, { name: 'b' }, { name: 'c' }, { name: 'd' }],
      [{ name: 'a' }, { name: 'b' }, { name: 'c' }, { name: 'd' }],
      [
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
    ];

    const arrayToSort = [
      [],
      [{ name: 'a' }],
      [{ name: 'a' }, { name: 'b' }, { name: 'c' }, { name: 'd' }],
      [{ name: 'b' }, { name: 'd' }, { name: 'a' }, { name: 'c' }],
      [
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
      ]
    ];

    arrayToSort.forEach((arr, i) => {
      expect(arr.length === expectedResult[i].length).toBeTrue();
      expect(pipe.transform(arr, 'name')).toEqual(expectedResult[i]);
    });
  });

  it('should sort array in descending order with an existing field when the reverse parameter is true', () => {
    const expectedResult = [
      [],
      [{ name: 'a' }],
      [{ name: 'd' }, { name: 'c' }, { name: 'b' }, { name: 'a' }],
      [{ name: 'd' }, { name: 'c' }, { name: 'b' }, { name: 'a' }],
      [
        { name: undefined },
        { name: '' },
        { name: null },
        { name: 'tête' },
        { name: 'test' },
        { name: 'pirates' },
        { name: 'pirate' },
        { name: 'ok' },
        { name: 'intention' },
        { name: 'évaluation' },
        { name: 'enlever' },
        { name: 'conversation' }
      ]
    ];

    const arrayToSort = [
      [],
      [{ name: 'a' }],
      [{ name: 'a' }, { name: 'b' }, { name: 'c' }, { name: 'd' }],
      [{ name: 'b' }, { name: 'd' }, { name: 'a' }, { name: 'c' }],
      [
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
      ]
    ];

    arrayToSort.forEach((arr, i) => {
      expect(arr.length === expectedResult[i].length).toBeTrue();
      expect(pipe.transform(arr, 'name', true)).toEqual(expectedResult[i]);
    });
  });

  it('should sort an array in ascending order when 2 sort criteria are defined', () => {
    const expectedResult = [
      [],
      [{ name: 'a', category: 'default' }],
      [
        { name: 'a', category: 'default' },
        { name: 'b', category: 'default' },
        { name: 'c', category: 'test' },
        { name: 'd', category: 'test' }
      ],
      [
        { name: 'a', category: 'default' },
        { name: 'b', category: 'default' },
        { name: 'c', category: null },
        { name: 'd', category: '' }
      ],
      [
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
    ];

    const arrayToSort = [
      [],
      [{ name: 'a', category: 'default' }],
      [
        { name: 'b', category: 'default' },
        { name: 'c', category: 'test' },
        { name: 'a', category: 'default' },
        { name: 'd', category: 'test' }
      ],
      [
        { name: 'c', category: null },
        { name: 'a', category: 'default' },
        { name: 'd', category: '' },
        { name: 'b', category: 'default' }
      ],
      [
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
      ]
    ];

    arrayToSort.forEach((arr, i) => {
      expect(arr.length === expectedResult[i].length).toBeTrue();
      expect(pipe.transform(arr, 'category', false, 'name')).toEqual(expectedResult[i]);
    });
  });
});
