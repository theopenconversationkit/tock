import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { NbDialogService } from '@nebular/theme';
import { of } from 'rxjs';
import { ApplicationService } from '../../core-nlp/applications.service';
import { BotConfigurationService } from '../../core/bot-configuration.service';

import { CurrentApplicationAndConfigurationsGuard } from './current-application-and-configurations.guard';

describe('CurrentApplicationAndConfigurationsGuard', () => {
  let guard: CurrentApplicationAndConfigurationsGuard;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [
        {
          provide: NbDialogService,
          useValue: { open: () => ({ onClose: (val: any) => of(val) }) }
        },
        { provide: ApplicationService, useValue: {} },
        { provide: BotConfigurationService, useValue: {} },
        { provide: Router, useValue: {} }
      ]
    }).compileComponents();

    guard = TestBed.inject(CurrentApplicationAndConfigurationsGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });
});
