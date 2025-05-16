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
