import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DialogService } from '../../../core-nlp/dialog.service';
import { ScenarioItemFrom, SCENARIO_MODE, SCENARIO_STATE } from '../../models';
import { ScenarioDesignerService } from '../scenario-designer.service';
import { ModeStepperComponent } from './mode-stepper.component';

const scenario = {
  id: '62fcbb7ae4d25c16a44071a1',
  name: 'testing scenario',
  category: 'scenarios',
  tags: ['testing'],
  applicationId: '62558f21b318632c9200b567',
  createDate: '2022-08-17T09:57:14.428Z',
  updateDate: '2022-08-17T09:57:33.053Z',
  description: '',
  data: {
    mode: 'writing' as SCENARIO_MODE,
    scenarioItems: [{ id: 0, from: 'client' as ScenarioItemFrom, text: 'Main intent', main: true }],
    contexts: []
  },
  state: 'draft' as SCENARIO_STATE
};

describe('ModeStepperComponent', () => {
  let component: ModeStepperComponent;
  let fixture: ComponentFixture<ModeStepperComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ModeStepperComponent],
      providers: [
        {
          provide: ScenarioDesignerService,
          useValue: { isStepValid: () => true }
        },
        { provide: DialogService, useValue: {} }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ModeStepperComponent);
    component = fixture.componentInstance;
    component.scenario = scenario;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
