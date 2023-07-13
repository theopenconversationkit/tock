import { TestBed } from '@angular/core/testing';
import { ScenarioDesignerService } from './scenario-designer.service';
import { ScenarioService } from '../services';
import { Router } from '@angular/router';

describe('ScenarioDesignerService', () => {
  let service: ScenarioDesignerService;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        { provide: ScenarioDesignerService, useClass: ScenarioDesignerService },
        { provide: ScenarioService, useValue: {} },
        { provide: Router, useValue: {} }
      ]
    });
    service = TestBed.inject(ScenarioDesignerService);
  });

  xit('should be created', () => {
    expect(service).toBeTruthy();
  });
});
