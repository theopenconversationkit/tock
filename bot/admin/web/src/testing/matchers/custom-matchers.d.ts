declare namespace jasmine {
  interface Matchers<T> {
    toHaveBeenCalledOnceWithDeepEquality(expected: Object | Array<any>): boolean;
  }
}
