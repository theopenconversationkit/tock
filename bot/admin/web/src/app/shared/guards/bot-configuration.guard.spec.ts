import { TestBed } from '@angular/core/testing';

import { BotConfigurationGuard } from './bot-configuration.guard';

describe('BotConfigurationGuard', () => {
  let guard: BotConfigurationGuard;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    guard = TestBed.inject(BotConfigurationGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });
});
