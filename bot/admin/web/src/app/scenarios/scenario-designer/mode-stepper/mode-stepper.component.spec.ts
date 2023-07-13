import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NbButtonModule, NbDialogService, NbIconModule, NbTooltipModule } from '@nebular/theme';

import { ModeStepperComponent } from './mode-stepper.component';
import { ScenarioVersion, ScenarioItemFrom, SCENARIO_MODE, SCENARIO_STATE, TempSentence } from '../../models';
import { Sentence } from '../../../model/nlp';
import { TestingModule } from '../../../../testing';
import { StubNbDialogService } from '../../../../testing/stubs';

const scenarioWriting: ScenarioVersion = {
  id: '62fcbb7ae4d25c16a44071a1',
  creationDate: '2022-08-17T09:57:14.428Z',
  updateDate: '2022-08-17T09:57:33.053Z',
  data: {
    mode: SCENARIO_MODE.writing,
    scenarioItems: [{ id: 0, from: 'client' as ScenarioItemFrom, text: 'Main intent', main: true }],
    contexts: [],
    triggers: []
  },
  state: 'draft' as SCENARIO_STATE
};

const scenarioCasting: ScenarioVersion = {
  id: '62fcbb7ae4d25c16a44071a1',
  creationDate: '2022-08-17T09:57:14.428Z',
  updateDate: '2022-08-17T09:57:33.053Z',
  data: {
    mode: SCENARIO_MODE.casting,
    scenarioItems: [
      { id: 0, from: 'client' as ScenarioItemFrom, text: 'Main intent', main: true },
      { id: 1, text: 'test1', from: 'bot' }
    ],
    contexts: [],
    triggers: []
  },
  state: 'draft' as SCENARIO_STATE
};

const scenarioProduction: ScenarioVersion = {
  id: '62fcbb7ae4d25c16a44071a1',
  creationDate: '2022-08-17T09:57:14.428Z',
  updateDate: '2022-08-17T09:57:33.053Z',
  data: {
    mode: SCENARIO_MODE.production,
    scenarioItems: [
      {
        id: 0,
        from: 'client' as ScenarioItemFrom,
        text: 'Main intent',
        main: true,
        intentDefinition: { name: 'intent1', label: 'intent1', sentences: [{} as TempSentence], _sentences: [{} as Sentence] }
      },
      {
        id: 1,
        text: 'action1',
        from: 'bot',
        actionDefinition: { name: 'action1', inputContextNames: [], outputContextNames: [] }
      },
      {
        id: 2,
        text: 'action2',
        from: 'bot',
        actionDefinition: { name: 'action2', inputContextNames: [], outputContextNames: [] }
      }
    ],
    contexts: [],
    triggers: []
  },
  state: 'draft' as SCENARIO_STATE
};

const scenarioPublishing: ScenarioVersion = {
  id: '62fcbb7ae4d25c16a44071a1',
  creationDate: '2022-08-17T09:57:14.428Z',
  updateDate: '2022-08-17T09:57:33.053Z',
  data: {
    mode: SCENARIO_MODE.publishing,
    scenarioItems: [
      {
        id: 0,
        from: 'client' as ScenarioItemFrom,
        text: 'Main intent',
        main: true,
        intentDefinition: {
          name: 'intent1',
          label: 'intent1',
          sentences: [{} as TempSentence],
          _sentences: [{} as Sentence]
        }
      },
      {
        id: 1,
        text: 'action1',
        from: 'bot',
        actionDefinition: {
          name: 'action1',
          inputContextNames: [],
          outputContextNames: ['test']
        }
      },
      {
        id: 2,
        text: 'action2',
        from: 'bot',
        actionDefinition: {
          name: 'action2',
          inputContextNames: ['test'],
          outputContextNames: []
        }
      }
    ],
    contexts: [],
    triggers: [],
    stateMachine: {
      id: 'root',
      type: 'parallel',
      states: {
        Global: {
          id: 'Global',
          states: {
            action1: { id: 'action1' },
            action2: { id: 'action2' }
          }
        }
      },
      initial: 'Global',
      on: { intent1: '#Global' }
    }
  },
  state: 'draft' as SCENARIO_STATE
};

describe('ModeStepperComponent', () => {
  let component: ModeStepperComponent;
  let fixture: ComponentFixture<ModeStepperComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ModeStepperComponent],
      imports: [TestingModule, NbButtonModule, NbIconModule, NbTooltipModule],
      providers: [{ provide: NbDialogService, useClass: StubNbDialogService }]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ModeStepperComponent);
    component = fixture.componentInstance;
    component.scenario = scenarioWriting;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('should respect steps order', () => {
    [
      {
        description: 'scenario mode writing',
        scenario: scenarioWriting,
        mode: SCENARIO_MODE.writing,
        stepPassed: { writing: false, casting: false, production: false, publishing: false },
        buttonClass: {
          writing: ['step', 'valid', 'selected'],
          casting: ['step'],
          production: ['step'],
          publishing: ['step']
        }
      },
      {
        description: 'scenario mode casting',
        scenario: scenarioWriting,
        mode: SCENARIO_MODE.casting,
        stepPassed: { writing: true, casting: false, production: false, publishing: false },
        buttonClass: {
          writing: ['step', 'valid'],
          casting: ['step', 'selected'],
          production: ['step'],
          publishing: ['step']
        }
      },
      {
        description: 'scenario mode production',
        scenario: scenarioCasting,
        mode: SCENARIO_MODE.production,
        stepPassed: { writing: true, casting: true, production: false, publishing: false },
        buttonClass: {
          writing: ['step', 'valid'],
          casting: ['step', 'valid'],
          production: ['step', 'selected'],
          publishing: ['step']
        }
      },
      {
        description: 'scenario mode publishing',
        scenario: scenarioProduction,
        mode: SCENARIO_MODE.publishing,
        stepPassed: { writing: true, casting: true, production: true, publishing: false },
        buttonClass: {
          writing: ['step', 'valid'],
          casting: ['step', 'valid'],
          production: ['step', 'valid'],
          publishing: ['step', 'selected']
        }
      },
      {
        description: 'scenario mode publishing valid',
        scenario: scenarioPublishing,
        mode: SCENARIO_MODE.publishing,
        stepPassed: { writing: true, casting: true, production: true, publishing: false },
        buttonClass: {
          writing: ['step', 'valid'],
          casting: ['step', 'valid'],
          production: ['step', 'valid'],
          publishing: ['step', 'valid', 'selected']
        }
      }
    ].forEach((test) => {
      it(test.description, () => {
        component.scenario = test.scenario;
        component.mode = test.mode;
        fixture.detectChanges();

        const buttonWriting: HTMLButtonElement = fixture.debugElement.query(By.css('[data-testid="step-ctrl-writing"]')).nativeElement;
        const buttonCasting: HTMLButtonElement = fixture.debugElement.query(By.css('[data-testid="step-ctrl-casting"]')).nativeElement;
        const buttonProduction: HTMLButtonElement = fixture.debugElement.query(
          By.css('[data-testid="step-ctrl-production"]')
        ).nativeElement;
        const buttonPublishing: HTMLButtonElement = fixture.debugElement.query(
          By.css('[data-testid="step-ctrl-publishing"]')
        ).nativeElement;

        expect(component.isStepPassed(SCENARIO_MODE.writing)).toBe(test.stepPassed.writing);
        expect(component.isStepPassed(SCENARIO_MODE.casting)).toBe(test.stepPassed.casting);
        expect(component.isStepPassed(SCENARIO_MODE.production)).toBe(test.stepPassed.production);
        expect(component.isStepPassed(SCENARIO_MODE.publishing)).toBe(test.stepPassed.publishing);
        expect(test.buttonClass.writing.every((i) => buttonWriting.classList.contains(i))).toBeTrue();
        expect(test.buttonClass.casting.every((i) => buttonCasting.classList.contains(i))).toBeTrue();
        expect(test.buttonClass.production.every((i) => buttonProduction.classList.contains(i))).toBeTrue();
        expect(test.buttonClass.publishing.every((i) => buttonPublishing.classList.contains(i))).toBeTrue();
      });
    });
  });

  it('should not switch to mode casting if step is not valid', () => {
    spyOn(component.modeSwitch, 'emit');
    const buttonCasting = fixture.debugElement.query(By.css('[data-testid="step-ctrl-casting"]'));
    buttonCasting.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.modeSwitch.emit).not.toHaveBeenCalled();
  });

  it('should switch to mode casting if step is valid', () => {
    component.scenario = scenarioCasting as ScenarioVersion;

    spyOn(component.modeSwitch, 'emit');
    const buttonCasting = fixture.debugElement.query(By.css('[data-testid="step-ctrl-casting"]'));
    buttonCasting.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.modeSwitch.emit).toHaveBeenCalled();
  });

  it('should not switch to mode production if step is not valid', () => {
    spyOn(component.modeSwitch, 'emit');
    const buttonProduction = fixture.debugElement.query(By.css('[data-testid="step-ctrl-production"]'));
    buttonProduction.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.modeSwitch.emit).not.toHaveBeenCalled();
  });

  it('should switch to mode production if step is valid', () => {
    component.scenario = scenarioProduction as ScenarioVersion;

    spyOn(component.modeSwitch, 'emit');
    const buttonProduction = fixture.debugElement.query(By.css('[data-testid="step-ctrl-production"]'));
    buttonProduction.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.modeSwitch.emit).toHaveBeenCalled();
  });

  it('should not switch to mode publishing if step is not valid', () => {
    spyOn(component.modeSwitch, 'emit');
    const buttonPublishing = fixture.debugElement.query(By.css('[data-testid="step-ctrl-publishing"]'));
    buttonPublishing.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.modeSwitch.emit).not.toHaveBeenCalled();
  });

  it('should switch to mode publishing if step is valid', () => {
    component.scenario = scenarioPublishing as ScenarioVersion;

    spyOn(component.modeSwitch, 'emit');
    const buttonPublishing = fixture.debugElement.query(By.css('[data-testid="step-ctrl-publishing"]'));
    buttonPublishing.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.modeSwitch.emit).toHaveBeenCalled();
  });
});
