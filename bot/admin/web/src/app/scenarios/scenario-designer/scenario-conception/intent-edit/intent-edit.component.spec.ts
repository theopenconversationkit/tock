import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NbButtonModule, NbCardModule, NbDialogRef, NbIconModule, NbInputModule, NbToggleModule, NbTooltipModule } from '@nebular/theme';

import { IntentEditComponent } from './intent-edit.component';
import { ScenarioVersion, ScenarioItemFrom, SCENARIO_MODE, SCENARIO_STATE } from '../../../models';
import { StateService } from '../../../../core-nlp/state.service';
import { TestingModule } from '../../../../../testing';

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
        main: true,
        intentDefinition: { name: 'mainIntent', label: 'Main intent', primary: true }
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
        intentDefinition: { name: 'secondaryIntent', label: 'Secondary intent', primary: false }
      }
    ],
    contexts: []
  },
  state: 'draft' as SCENARIO_STATE
};
function getScenarioMock() {
  return JSON.parse(JSON.stringify(scenario)) as ScenarioVersion;
}

describe('IntentEditComponent', () => {
  let component: IntentEditComponent;
  let fixture: ComponentFixture<IntentEditComponent>;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [IntentEditComponent],
      providers: [
        {
          provide: NbDialogRef,
          useValue: {}
        },
        {
          provide: StateService,
          useValue: {}
        }
      ],
      imports: [TestingModule, NbInputModule, NbButtonModule, NbIconModule, NbTooltipModule, NbToggleModule, NbCardModule]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IntentEditComponent);
    component = fixture.componentInstance;
    let scenarioMock = getScenarioMock();
    component.item = scenarioMock.data.scenarioItems[0];
    component.contexts = scenarioMock.data.contexts;
    fixture.detectChanges();
  });

  it('Should create', () => {
    expect(component).toBeTruthy();
    expect(component.form.getRawValue()).toEqual({
      sentences: [],
      contextsEntities: [],
      outputContextNames: [],
      primary: true
    });
  });

  it('Should destroy', () => {
    expect(component.ngOnDestroy).toBeDefined();
    expect(component.destroy.isStopped).toBeFalsy();
    component.ngOnDestroy();
    expect(component.destroy.isStopped).toBeTruthy();
  });
});
