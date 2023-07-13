import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { NbDialogService } from '@nebular/theme';
import { of } from 'rxjs';
import { BotConfigurationService } from '../../core/bot-configuration.service';

import { BotConfigurationGuard } from './bot-configuration.guard';

describe('BotConfigurationGuard', () => {
  let guard: BotConfigurationGuard;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [
        {
          provide: NbDialogService,
          useValue: { open: () => ({ onClose: (val: any) => of(val) }) }
        },
        { provide: BotConfigurationService, useValue: {} },
        { provide: Router, useValue: {} }
      ]
    }).compileComponents();

    guard = TestBed.inject(BotConfigurationGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });
});
