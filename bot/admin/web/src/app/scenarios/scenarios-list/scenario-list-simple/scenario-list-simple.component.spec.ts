import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';

import { ScenarioListSimpleComponent } from './scenario-list-simple.component';
import { ScenarioService } from '../../services/scenario.service';
import { StateService } from '../../../core-nlp/state.service';
import { TestSharedModule } from '../../../shared/test-shared.module';

describe('ScenarioListSimpleComponent', () => {
  let component: ScenarioListSimpleComponent;
  let fixture: ComponentFixture<ScenarioListSimpleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScenarioListSimpleComponent],
      providers: [
        {
          provide: ScenarioService,
          useValue: {}
        },
        {
          provide: StateService,
          useValue: { currentApplication: { name: 'TestApplicationName' } }
        },
        { provide: DatePipe },
        { provide: Router, useValue: {} }
      ],
      schemas: [NO_ERRORS_SCHEMA],
      imports: [TestSharedModule]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScenarioListSimpleComponent);
    component = fixture.componentInstance;
    component.scenariosGroups = [];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
