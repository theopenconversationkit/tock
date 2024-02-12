import { isUrl, normalizedCamelCase, normalizedSnakeCase, normalizedSnakeCaseUpper } from './strings.utils';
import { includesArray, isPrimitive, orderBy } from './utils';

describe('normalizedSnakeCase', () => {
  it(`Should normalize string and transform to snake case`, () => {
    expect(normalizedSnakeCase('Hello world')).toEqual('Hello_world');
    expect(normalizedSnakeCase('&é"( (-è_ _çàç) = 0°×÷¡²³ê')).toEqual('e_e___cac_0e');
    expect(normalizedSnakeCase('a-B-c _1__2--3')).toEqual('a_B_c__1__2_3');
  });
});

describe('normalizedSnakeCaseUpper', () => {
  it(`Should normalize string and transform to snake upper case`, () => {
    expect(normalizedSnakeCaseUpper('Hello world')).toEqual('HELLO_WORLD');
    expect(normalizedSnakeCaseUpper('&é"( (-è_ _çàç) = 0°×÷¡²³ê')).toEqual('E_E___CAC_0E');
    expect(normalizedSnakeCaseUpper('a-B-c _1__2--3')).toEqual('A_B_C__1__2_3');
  });
});

describe('normalizedCamelCase', () => {
  it(`Should normalize string and transform to camel case`, () => {
    expect(normalizedCamelCase('Hello world')).toEqual('helloWorld');
    expect(normalizedCamelCase('&é"( (-è_ _çàç) = 0°×÷¡²³ê')).toEqual('EECac0E');
    expect(normalizedCamelCase('a-B-c _1__2--3')).toEqual('aBC123');
  });
});

describe('isUrl', () => {
  it(`Should normalize string and transform to camel case`, () => {
    expect(isUrl('Hello world')).toBeFalse();
    expect(isUrl('test.com')).toBeFalse();
    expect(isUrl('http://a')).toBeFalse();
    expect(isUrl('http://a.b')).toBeFalse();
    expect(isUrl('http://123')).toBeFalse();

    expect(isUrl('http://test.com')).toBeTrue();
    expect(isUrl('https://test.com')).toBeTrue();
    expect(isUrl('http://www.test.com')).toBeTrue();
    expect(isUrl('http://www.test.com?p=1')).toBeTrue();
    expect(isUrl('http://aa.bb')).toBeTrue();
    expect(isUrl('http://102.25.36.24')).toBeTrue();
  });
});
