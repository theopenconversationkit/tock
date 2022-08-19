import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DialogService } from '../../../core-nlp/dialog.service';
import { Scenario, ScenarioItemFrom, SCENARIO_MODE, SCENARIO_STATE } from '../../models';
import { ModeStepperComponent } from './mode-stepper.component';

const scenarioWriting = {
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

const scenarioCasting = {
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
    scenarioItems: [
      { id: 0, from: 'client' as ScenarioItemFrom, text: 'Main intent', main: true },
      { id: 1, text: 'test1', from: 'bot' }
    ],
    contexts: []
  },
  state: 'draft' as SCENARIO_STATE
};

const scenarioProduction = {
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
    scenarioItems: [
      {
        id: 0,
        from: 'client' as ScenarioItemFrom,
        text: 'Main intent',
        main: true,
        intentDefinition: { name: 'intent1', label: 'intent1' }
      },
      {
        id: 1,
        text: 'action1',
        from: 'bot',
        tickActionDefinition: { name: 'action1', inputContextNames: [], outputContextNames: [] }
      },
      {
        id: 2,
        text: 'action2',
        from: 'bot',
        tickActionDefinition: { name: 'action2', inputContextNames: [], outputContextNames: [] }
      }
    ],
    contexts: []
  },
  state: 'draft' as SCENARIO_STATE
};

const scenarioPublishing = {
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
    scenarioItems: [
      {
        id: 0,
        from: 'client' as ScenarioItemFrom,
        text: 'Main intent',
        main: true,
        intentDefinition: { name: 'intent1', label: 'intent1' }
      },
      {
        id: 1,
        text: 'action1',
        from: 'bot',
        tickActionDefinition: {
          name: 'action1',
          inputContextNames: [],
          outputContextNames: ['test']
        }
      },
      {
        id: 2,
        text: 'action2',
        from: 'bot',
        tickActionDefinition: {
          name: 'action2',
          inputContextNames: ['test'],
          outputContextNames: []
        }
      }
    ],
    contexts: [],
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
      providers: [{ provide: DialogService, useValue: {} }],
      schemas: [NO_ERRORS_SCHEMA]
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

  it('Should destroy', () => {
    expect(component.ngOnDestroy).toBeDefined();
    expect(component.destroy.isStopped).toBeFalsy();
    component.ngOnDestroy();
    expect(component.destroy.isStopped).toBeTruthy();
  });

  it('should respect steps order', () => {
    const buttonWriting = fixture.debugElement.query(By.css('[data-testid="step-ctrl-writing"]'));
    const buttonCasting = fixture.debugElement.query(By.css('[data-testid="step-ctrl-casting"]'));
    const buttonProduction = fixture.debugElement.query(
      By.css('[data-testid="step-ctrl-production"]')
    );
    const buttonPublishing = fixture.debugElement.query(
      By.css('[data-testid="step-ctrl-publishing"]')
    );

    component.mode = SCENARIO_MODE.writing;
    fixture.detectChanges();
    expect(component.isStepPassed(SCENARIO_MODE.writing)).toBeFalsy();
    expect(component.isStepPassed(SCENARIO_MODE.casting)).toBeFalsy();
    expect(component.isStepPassed(SCENARIO_MODE.production)).toBeFalsy();
    expect(component.isStepPassed(SCENARIO_MODE.publishing)).toBeFalsy();
    expect(buttonWriting.nativeElement).toHaveClass('valid');
    expect(buttonWriting.nativeElement).toHaveClass('selected');
    expect(buttonCasting.nativeElement).not.toHaveClass('valid');
    expect(buttonCasting.nativeElement).not.toHaveClass('selected');
    expect(buttonProduction.nativeElement).not.toHaveClass('valid');
    expect(buttonProduction.nativeElement).not.toHaveClass('selected');
    expect(buttonPublishing.nativeElement).not.toHaveClass('valid');
    expect(buttonPublishing.nativeElement).not.toHaveClass('selected');

    component.mode = SCENARIO_MODE.casting;
    fixture.detectChanges();
    expect(component.isStepPassed(SCENARIO_MODE.writing)).toBeTruthy();
    expect(component.isStepPassed(SCENARIO_MODE.casting)).toBeFalsy();
    expect(component.isStepPassed(SCENARIO_MODE.production)).toBeFalsy();
    expect(component.isStepPassed(SCENARIO_MODE.publishing)).toBeFalsy();
    expect(buttonWriting.nativeElement).toHaveClass('valid');
    expect(buttonWriting.nativeElement).not.toHaveClass('selected');
    expect(buttonCasting.nativeElement).not.toHaveClass('valid');
    expect(buttonCasting.nativeElement).toHaveClass('selected');
    expect(buttonProduction.nativeElement).not.toHaveClass('valid');
    expect(buttonProduction.nativeElement).not.toHaveClass('selected');
    expect(buttonPublishing.nativeElement).not.toHaveClass('valid');
    expect(buttonPublishing.nativeElement).not.toHaveClass('selected');

    component.scenario = scenarioCasting as Scenario;
    component.mode = SCENARIO_MODE.production;
    fixture.detectChanges();
    expect(component.isStepPassed(SCENARIO_MODE.writing)).toBeTruthy();
    expect(component.isStepPassed(SCENARIO_MODE.casting)).toBeTruthy();
    expect(component.isStepPassed(SCENARIO_MODE.production)).toBeFalsy();
    expect(component.isStepPassed(SCENARIO_MODE.publishing)).toBeFalsy();
    expect(buttonWriting.nativeElement).toHaveClass('valid');
    expect(buttonWriting.nativeElement).not.toHaveClass('selected');
    expect(buttonCasting.nativeElement).toHaveClass('valid');
    expect(buttonCasting.nativeElement).not.toHaveClass('selected');
    expect(buttonProduction.nativeElement).not.toHaveClass('valid');
    expect(buttonProduction.nativeElement).toHaveClass('selected');
    expect(buttonPublishing.nativeElement).not.toHaveClass('valid');
    expect(buttonPublishing.nativeElement).not.toHaveClass('selected');

    component.scenario = scenarioProduction as Scenario;
    component.mode = SCENARIO_MODE.publishing;
    fixture.detectChanges();
    expect(component.isStepPassed(SCENARIO_MODE.writing)).toBeTruthy();
    expect(component.isStepPassed(SCENARIO_MODE.casting)).toBeTruthy();
    expect(component.isStepPassed(SCENARIO_MODE.production)).toBeTruthy();
    expect(component.isStepPassed(SCENARIO_MODE.publishing)).toBeFalsy();
    expect(buttonWriting.nativeElement).toHaveClass('valid');
    expect(buttonWriting.nativeElement).not.toHaveClass('selected');
    expect(buttonCasting.nativeElement).toHaveClass('valid');
    expect(buttonCasting.nativeElement).not.toHaveClass('selected');
    expect(buttonProduction.nativeElement).toHaveClass('valid');
    expect(buttonProduction.nativeElement).not.toHaveClass('selected');
    expect(buttonPublishing.nativeElement).not.toHaveClass('valid');
    expect(buttonPublishing.nativeElement).toHaveClass('selected');

    component.scenario = scenarioPublishing as Scenario;
    fixture.detectChanges();
    expect(buttonPublishing.nativeElement).toHaveClass('valid');
  });

  it('should not switch to mode casting if step is not valid', () => {
    spyOn(component.modeSwitch, 'emit');
    const buttonCasting = fixture.debugElement.query(By.css('[data-testid="step-ctrl-casting"]'));
    buttonCasting.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.modeSwitch.emit).not.toHaveBeenCalled();
  });

  it('should switch to mode casting if step is valid', () => {
    component.scenario = scenarioCasting as Scenario;

    spyOn(component.modeSwitch, 'emit');
    const buttonCasting = fixture.debugElement.query(By.css('[data-testid="step-ctrl-casting"]'));
    buttonCasting.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.modeSwitch.emit).toHaveBeenCalled();
  });

  it('should not switch to mode production if step is not valid', () => {
    spyOn(component.modeSwitch, 'emit');
    const buttonProduction = fixture.debugElement.query(
      By.css('[data-testid="step-ctrl-production"]')
    );
    buttonProduction.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.modeSwitch.emit).not.toHaveBeenCalled();
  });

  it('should switch to mode production if step is valid', () => {
    component.scenario = scenarioProduction as Scenario;

    spyOn(component.modeSwitch, 'emit');
    const buttonProduction = fixture.debugElement.query(
      By.css('[data-testid="step-ctrl-production"]')
    );
    buttonProduction.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.modeSwitch.emit).toHaveBeenCalled();
  });

  it('should not switch to mode publishing if step is not valid', () => {
    spyOn(component.modeSwitch, 'emit');
    const buttonPublishing = fixture.debugElement.query(
      By.css('[data-testid="step-ctrl-publishing"]')
    );
    buttonPublishing.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.modeSwitch.emit).not.toHaveBeenCalled();
  });

  it('should switch to mode publishing if step is valid', () => {
    component.scenario = scenarioPublishing as Scenario;

    spyOn(component.modeSwitch, 'emit');
    const buttonPublishing = fixture.debugElement.query(
      By.css('[data-testid="step-ctrl-publishing"]')
    );
    buttonPublishing.triggerEventHandler('click', null);
    fixture.detectChanges();

    expect(component.modeSwitch.emit).toHaveBeenCalled();
  });
});
