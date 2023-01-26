import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NbDialogRef } from '@nebular/theme';
import { TestingModule } from '@tock/testing';
import { EMPTY } from 'rxjs';

import { StateService } from '../../../../core-nlp/state.service';
import { AutocompleteInputComponent } from '../../../../shared/components';
import { ScenarioVersion, ScenarioItemFrom, SCENARIO_MODE, SCENARIO_STATE } from '../../../models';
import { IntentCreateComponent } from './intent-create.component';

const scenario = {
  id: '62fcbb7ae4d25c16a44071a1',
  name: 'testing scenario',
  category: 'scenarios',
  tags: ['testing'],
  applicationId: '62558f21b318632c9200b567',
  creationDate: '2022-08-17T09:57:14.428Z',
  updateDate: '2022-08-17T09:57:33.053Z',
  description: '',
  data: {
    mode: 'writing' as SCENARIO_MODE,
    scenarioItems: [
      {
        id: 0,
        from: 'client' as ScenarioItemFrom,
        text: 'Main intent',
        main: true
      },
      {
        id: 1,
        from: 'bot' as ScenarioItemFrom,
        text: 'Action'
      },
      {
        id: 2,
        from: 'client' as ScenarioItemFrom,
        text: 'Secondary intent',
        intentDefinition: { name: 'secondaryIntent', label: 'Secondary intent' }
      }
    ],
    contexts: []
  },
  state: 'draft' as SCENARIO_STATE
};
function getScenarioMock() {
  return JSON.parse(JSON.stringify(scenario)) as ScenarioVersion;
}

describe('IntentCreateComponent', () => {
  let component: IntentCreateComponent;
  let fixture: ComponentFixture<IntentCreateComponent>;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [IntentCreateComponent, AutocompleteInputComponent],
      providers: [
        {
          provide: NbDialogRef,
          useValue: {}
        },
        {
          provide: StateService,
          useValue: {
            currentIntentsCategories: EMPTY,
            intentExists: (intentName) => {
              if (intentName === 'intentExistingInApp') return true;
              return false;
            }
          }
        }
      ],
      imports: [TestingModule],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IntentCreateComponent);
    component = fixture.componentInstance;
    component.scenario = getScenarioMock();
    component.item = component.scenario.data.scenarioItems[0];
    fixture.detectChanges();
  });

  it('Should create', () => {
    expect(component).toBeTruthy();
    expect(component.form.value).toEqual({
      label: 'Main intent',
      name: 'mainIntent',
      category: 'scenarios',
      description: null,
      primary: true
    });
  });

  it('Should destroy', () => {
    expect(component.ngOnDestroy).toBeDefined();
    expect(component.destroy.isStopped).toBeFalsy();
    component.ngOnDestroy();
    expect(component.destroy.isStopped).toBeTruthy();
  });

  it('Should not accept non unic name in scenario', () => {
    let nameinput = fixture.debugElement.query(By.css('[data-testid="name"]'));
    nameinput.nativeElement.value = 'Secondary intent';
    nameinput.nativeElement.dispatchEvent(new Event('input'));
    nameinput.nativeElement.dispatchEvent(new Event('keyup'));
    fixture.detectChanges();
    expect(component.form.valid).toBeFalsy();
  });

  it('Should not accept non unic name in app', () => {
    let nameinput = fixture.debugElement.query(By.css('[data-testid="name"]'));
    nameinput.nativeElement.value = 'intent existing in app';
    nameinput.nativeElement.dispatchEvent(new Event('input'));
    nameinput.nativeElement.dispatchEvent(new Event('keyup'));
    fixture.detectChanges();
    expect(component.form.valid).toBeFalsy();
  });

  it('Should not accept unknown name', () => {
    let nameinput = fixture.debugElement.query(By.css('[data-testid="name"]'));
    nameinput.nativeElement.value = 'unknown';
    nameinput.nativeElement.dispatchEvent(new Event('input'));
    nameinput.nativeElement.dispatchEvent(new Event('keyup'));
    fixture.detectChanges();
    expect(component.form.valid).toBeFalsy();
  });
});
