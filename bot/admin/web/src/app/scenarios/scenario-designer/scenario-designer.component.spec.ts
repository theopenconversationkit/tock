import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { NbToastrService } from '@nebular/theme';
import { of, Subject } from 'rxjs';
import { BotService } from '../../bot/bot-service';
import { DialogService } from '../../core-nlp/dialog.service';
import { StateService } from '../../core-nlp/state.service';
import { ScenarioVersion, ScenarioVersionExtended, SCENARIO_ITEM_FROM_CLIENT, SCENARIO_MODE, SCENARIO_STATE } from '../models';
import { ScenarioService } from '../services/scenario.service';
import { ScenarioDesignerComponent } from './scenario-designer.component';
import { ScenarioDesignerService } from './scenario-designer.service';

const testScenario: ScenarioVersionExtended = {
  id: '5',
  data: {
    mode: SCENARIO_MODE.writing,
    scenarioItems: [
      {
        id: 0,
        from: SCENARIO_ITEM_FROM_CLIENT,
        text: '',
        main: true
      }
    ],
    contexts: []
  },
  state: SCENARIO_STATE.draft
};

describe('ScenarioDesignerComponent', () => {
  let component: ScenarioDesignerComponent;
  let fixture: ComponentFixture<ScenarioDesignerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScenarioDesignerComponent],
      providers: [
        {
          provide: ScenarioService,
          useValue: {
            getScenarioVersion: () => of(testScenario),
            getActionHandlers: () => of([])
          }
        },
        { provide: ActivatedRoute, useValue: { params: of({ id: '5' }) } },
        { provide: NbToastrService, useValue: {} },
        { provide: StateService, useValue: { configurationChange: new Subject() } },
        {
          provide: ScenarioDesignerService,
          useValue: {
            scenarioDesignerCommunication: of({ type: 'updateScenarioBackup' })
          }
        },
        { provide: DialogService, useValue: {} },
        {
          provide: BotService,
          useValue: {
            i18nLabels: () => of({})
          }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScenarioDesignerComponent);
    component = fixture.componentInstance;
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

  it("should init scenario.data when it doesn't exists", () => {
    const scenarioCopy = JSON.parse(JSON.stringify(testScenario));
    delete scenarioCopy.data;
    spyOn(component['scenarioService'], 'getScenarioVersion').and.returnValue(of(scenarioCopy));
    component.ngOnInit();
    expect(component.scenarioVersion).toEqual(testScenario);
  });

  it('should set scenarioBackup on init', () => {
    expect(component.scenarioVersionBackup).toEqual(JSON.stringify(testScenario));
  });

  it('should show exit button', () => {
    let exitButton = fixture.debugElement.query(By.css('[data-testid="exit-button"]'));
    expect(exitButton).toBeTruthy();
  });

  it('should show save buttons if scenario is not read only', () => {
    let saveButton = fixture.debugElement.query(By.css('[data-testid="save-button"]'));
    expect(saveButton).toBeTruthy();

    let saveAnExitButton = fixture.debugElement.query(By.css('[data-testid="save-and-exit-button"]'));
    expect(saveAnExitButton).toBeTruthy();
  });

  it('should not show save buttons if scenario is read only', () => {
    const scenarioCopy = JSON.parse(JSON.stringify(testScenario));
    scenarioCopy.state = SCENARIO_STATE.current;
    spyOn(component['scenarioService'], 'getScenarioVersion').and.returnValue(of(scenarioCopy));
    component.ngOnInit();
    fixture.detectChanges();

    let saveButton = fixture.debugElement.query(By.css('[data-testid="save-button"]'));
    expect(saveButton).toBeFalsy();

    let saveAnExitButton = fixture.debugElement.query(By.css('[data-testid="save-and-exit-button"]'));
    expect(saveAnExitButton).toBeFalsy();
  });

  it('should display stepper component', () => {
    const compiled = fixture.debugElement.nativeElement;
    const stepper = compiled.querySelector('scenario-mode-stepper');
    expect(stepper).toBeTruthy();
  });

  it('should display scenario-conception when conditions are met', () => {
    component.scenarioVersion.data.mode = SCENARIO_MODE.writing;
    fixture.detectChanges();

    let compiled = fixture.debugElement.nativeElement;
    let stepper = compiled.querySelector('scenario-conception');
    expect(stepper).toBeTruthy();

    component.scenarioVersion.data.mode = SCENARIO_MODE.casting;
    fixture.detectChanges();

    compiled = fixture.debugElement.nativeElement;
    stepper = compiled.querySelector('scenario-conception');
    expect(stepper).toBeTruthy();
  });

  it('should display scenario-production when conditions are met', () => {
    component.scenarioVersion.data.mode = SCENARIO_MODE.production;
    fixture.detectChanges();

    let compiled = fixture.debugElement.nativeElement;
    let stepper = compiled.querySelector('scenario-production');
    expect(stepper).toBeTruthy();
  });

  it('should display scenario-publishing when conditions are met', () => {
    component.scenarioVersion.data.mode = SCENARIO_MODE.publishing;
    fixture.detectChanges();

    let compiled = fixture.debugElement.nativeElement;
    let stepper = compiled.querySelector('scenario-publishing');
    expect(stepper).toBeTruthy();
  });
});
