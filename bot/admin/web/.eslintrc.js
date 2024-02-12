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
