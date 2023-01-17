import { TestBed } from '@angular/core/testing';

import { CurrentApplicationGuard } from './current-application.guard';

describe('ApplicationGuard', () => {
  let guard: CurrentApplicationGuard;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    guard = TestBed.inject(CurrentApplicationGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });
});
