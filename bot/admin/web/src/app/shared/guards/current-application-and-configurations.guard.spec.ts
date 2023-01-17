import { TestBed } from '@angular/core/testing';

import { CurrentApplicationAndConfigurationsGuard } from './current-application-and-configurations.guard';

describe('CurrentApplicationAndConfigurationsGuard', () => {
  let guard: CurrentApplicationAndConfigurationsGuard;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    guard = TestBed.inject(CurrentApplicationAndConfigurationsGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });
});
