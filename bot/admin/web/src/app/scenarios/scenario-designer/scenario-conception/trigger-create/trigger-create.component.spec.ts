import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NbButtonModule, NbCardModule, NbDialogRef, NbIconModule, NbInputModule } from '@nebular/theme';

import { TriggerCreateComponent } from './trigger-create.component';
import { ScenarioVersionExtended, SCENARIO_MODE, SCENARIO_STATE } from '../../../../scenarios/models';
import { FormControlComponent } from '../../../../shared/components';
import { TestingModule } from '../../../../../testing';

const scenarioVersion: ScenarioVersionExtended = {
  state: SCENARIO_STATE.draft,
  id: '1',
  data: {
    mode: SCENARIO_MODE.casting,
    triggers: ['aaaaa', 'bbbbb'],
    scenarioItems: [
      {
        id: 0,
        from: 'client',
        text: 'Je souhaite déclarer un bris de glace svp',
        main: true,
        intentDefinition: {
          label: 'Test trigger',
          name: 'testtrigger',
          category: 'test',
          primary: true
        }
      }
    ]
  }
};

describe('EventCreateComponent', () => {
  let component: TriggerCreateComponent;
  let fixture: ComponentFixture<TriggerCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TriggerCreateComponent, FormControlComponent],
      imports: [TestingModule, NbButtonModule, NbCardModule, NbIconModule, NbInputModule],
      providers: [
        {
          provide: NbDialogRef,
          useValue: {}
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TriggerCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Should format name in lower case and remove special character', () => {
    [
      { value: 'test', expected: 'test' },
      { value: 'TEST', expected: 'test' },
      { value: 'test123', expected: 'test123' },
      { value: 't e st', expected: 'test' },
      { value: 'test&"\'(-_)=$*!:;,<>~#{[|`\\^@]}+°^¨£µ%§/.?', expected: 'test' },
      { value: 'testéèçàùê', expected: 'testeecaue' }
    ].forEach((test, i) => {
      it(`${i}`, () => {
        const nameinput = fixture.debugElement.query(By.css('[data-testid="trigger"]'));

        nameinput.nativeElement.value = test.value;
        nameinput.nativeElement.dispatchEvent(new Event('input'));
        nameinput.nativeElement.dispatchEvent(new Event('keyup'));
        fixture.detectChanges();

        expect(component.trigger.value).toBe(test.expected);
      });
    });
  });

  it('should associate validators to the name', () => {
    component.scenarioVersion = scenarioVersion;
    fixture.detectChanges();

    expect(component.trigger.valid).toBeFalse();

    // trigger is required
    expect(component.trigger.errors.required).toBeTruthy();
    expect(component.trigger.errors.minlength).toBeFalsy();
    expect(component.trigger.errors.custom).toBeFalsy();
    expect(component.trigger.valid).toBeFalse();

    // set the trigger to short text (less than 5 characters)
    component.trigger.setValue('test');
    expect(component.trigger.errors.required).toBeFalsy();
    expect(component.trigger.errors.minlength).toBeTruthy();
    expect(component.trigger.errors.custom).toBeFalsy();
    expect(component.trigger.errors.minlength.requiredLength).toBe(5);
    expect(component.trigger.valid).toBeFalse();

    // set the trigger of an already defined value in the list of event
    component.trigger.setValue('aaaaa');
    expect(component.trigger.errors.required).toBeFalsy();
    expect(component.trigger.errors.minlength).toBeFalsy();
    expect(component.trigger.errors.custom).toBeTruthy();
    expect(component.trigger.valid).toBeFalse();

    // set the trigger of an already defined intent
    component.trigger.setValue('testtrigger');
    expect(component.trigger.errors.required).toBeFalsy();
    expect(component.trigger.errors.minlength).toBeFalsy();
    expect(component.trigger.errors.custom).toBeTruthy();
    expect(component.trigger.valid).toBeFalse();

    // set trigger to something correct
    component.trigger.setValue('testvalue');
    expect(component.trigger.errors).toBeFalsy();
    expect(component.trigger.valid).toBeTrue();
  });
});
