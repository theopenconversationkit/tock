import { TestBed } from '@angular/core/testing';
import { ApplicationService } from '../../core-nlp/applications.service';

import { CurrentApplicationGuard } from './current-application.guard';

describe('ApplicationGuard', () => {
  let guard: CurrentApplicationGuard;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [{ provide: ApplicationService, useValue: {} }]
    }).compileComponents();

    guard = TestBed.inject(CurrentApplicationGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });
});
