export const SpyOnCustomMatchers: jasmine.CustomMatcherFactories = {
  /**
   * {@link expect} the actual (a {@link Spy}) to have been called exactly once, and exactly with the argument.
   * @function
   * @name matchers#toHaveBeenCalledOnceWithDeepEquality
   * @param {any} actual
   * @param {Object | Array} expected The argument to look for
   * @example
   * expect(mySpy).toHaveBeenCalledOnceWithDeepEquality({ firstname: 'John', lastname: 'DOE' });
   * expect(mySpy).toHaveBeenCalledOnceWithDeepEquality([1, 'john doe', { username: 'john doe' }]);
   */
  toHaveBeenCalledOnceWithDeepEquality: (util: jasmine.MatchersUtil): jasmine.CustomMatcher => ({
    compare: function (actual: any, expected: Object | Array<any>) {
      if (!((!!expected && expected.constructor === Object) || Array.isArray(expected)))
        throw new Error('The expected argument type is a literal object or an array.');

      if (!jasmine.isSpy(actual)) throw new Error(`Expected a spy, but got ${util.pp(actual)}.`);

      const actualArg = actual.calls.allArgs()[0][0];
      const passOnce = actual.calls.count() === 1;
      const pass = passOnce && util.equals(actualArg, expected);
      const message = pass
        ? ''
        : !passOnce
        ? `Expected the function to be called only once, but it was called ${actual.calls.count()} times`
        : `It was expected that the function would have been called exactly once with:\n ${util.pp(
            expected
          )},\nbut it was called with:\n ${util.pp(actualArg)}`;

      return {
        pass,
        message
      };
    }
  })
};
