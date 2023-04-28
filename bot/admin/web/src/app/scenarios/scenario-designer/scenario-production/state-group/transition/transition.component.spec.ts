import { ElementRef, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Subject } from 'rxjs';

import { TestingModule } from '../../../../../../testing';
import { MachineState } from '../../../../models';
import { ScenarioProductionService } from '../../scenario-production.service';
import { ScenarioTransitionComponent } from './transition.component';

const machineState: MachineState = {
  id: 'root',
  type: 'parallel',
  states: {
    Global: {
      id: 'Global',
      states: {
        WHICH: {
          id: 'WHICH',
          states: {},
          on: {}
        }
      },
      on: {},
      initial: 'WHICH'
    }
  },
  initial: 'Global',
  on: {}
};

const Intents = [
  {
    label: 'Henri IV de France',
    name: 'henriIvDeFrance',
    category: 'scenarios',
    primary: false,
    sentences: [],
    outputContextNames: ['FRANCE']
  }
];

describe('ScenarioTransitionComponent', () => {
  let component: ScenarioTransitionComponent;
  let fixture: ComponentFixture<ScenarioTransitionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScenarioTransitionComponent],
      imports: [TestingModule],
      providers: [
        {
          provide: ScenarioProductionService,
          useValue: {
            scenarioProductionItemsCommunication: new Subject<any>(),
            redrawPaths: () => {},
            registerTransitionComponent: () => {},
            unRegisterTransitionComponent: () => {},
            scenarioProductionStateComponents: {
              TEST: {
                stateWrapper: { nativeElement: {} }
              }
            }
          }
        },
        { provide: ElementRef, useValue: {} }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScenarioTransitionComponent);
    component = fixture.componentInstance;
    component.intents = Intents;
    component.transition = { name: 'henriIvDeFrance', target: 'TEST' };
    component.parentState = machineState.states.Global;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });

  it('Should destroy', () => {
    expect(component.ngOnDestroy).toBeDefined();
    expect(component.destroy.isStopped).toBeFalsy();
    component.ngOnDestroy();
    expect(component.destroy.isStopped).toBeTruthy();
  });
});
