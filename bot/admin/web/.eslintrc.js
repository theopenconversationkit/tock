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

module.exports = exports = {
  root: true,
  ignorePatterns: ['projects/**/*'],
  overrides: [
    {
      files: ['*.ts'],
      parserOptions: {
        project: ['tsconfig.json'],
        createDefaultProgram: true,
        tsconfigRootDir: __dirname
      },
      extends: ['plugin:@angular-eslint/recommended', 'plugin:@angular-eslint/template/process-inline-templates'],
      rules: {
        '@angular-eslint/directive-selector': [
          'error',
          {
            prefix: 'tock',
            style: 'camelCase',
            type: 'attribute'
          }
        ],
        '@angular-eslint/component-selector': [
          'error',
          {
            prefix: 'tock',
            style: 'kebab-case',
            type: 'element'
          }
        ],
        'brace-style': ['error', '1tbs'],
        '@angular-eslint/no-output-on-prefix': 0
      }
    },
    {
      files: ['*.html'],
      extends: ['plugin:@angular-eslint/template/recommended'],
      rules: {}
    }
  ]
};
