import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { DatePipe, Location } from '@angular/common';
import { Routes } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';

import { ScenarioListSimpleComponent } from './scenario-list-simple.component';
import { ScenarioService } from '../../services/scenario.service';
import { ScenarioGroup, SCENARIO_STATE } from '../../models';
import { StateService } from '../../../core-nlp/state.service';
import { TestSharedModule } from '../../../shared/testing/test-shared.module';
import { ScenarioDesignerComponent } from '../../scenario-designer/scenario-designer.component';
import { ScenariosListComponent } from '../scenarios-list.component';
import { SpyOnCustomMatchers } from '../../../shared/testing/matchers/custom-matchers';

const mockScenariosGroups: ScenarioGroup[] = [
  {
    id: '1',
    name: 'scenario 1',
    tags: [],
    versions: [
      {
        id: 's1v1',
        state: SCENARIO_STATE.draft,
        creationDate: '01/01/1970'
      },
      {
        id: 's1v2',
        state: SCENARIO_STATE.draft,
        creationDate: '02/01/1970'
      },
      {
        id: 's1v3',
        state: SCENARIO_STATE.current,
        creationDate: '01/01/1970'
      },
      {
        id: 's1v4',
        state: SCENARIO_STATE.archive,
        creationDate: '01/01/1970'
      }
    ]
  },
  {
    id: '2',
    name: 'scenario 2',
    tags: [],
    versions: [
      {
        id: 's2v1',
        state: SCENARIO_STATE.archive,
        creationDate: '01/01/1970'
      },
      {
        id: 's2v2',
        state: SCENARIO_STATE.current,
        creationDate: '01/02/1970'
      }
    ]
  },
  {
    id: '3',
    name: 'scenario 3',
    tags: [],
    versions: [
      {
        id: 's3v1',
        state: SCENARIO_STATE.archive,
        creationDate: '01/01/1970'
      },
      {
        id: 's3v2',
        state: SCENARIO_STATE.archive,
        creationDate: '01/01/1980'
      },
      {
        id: 's3v3',
        state: SCENARIO_STATE.archive,
        creationDate: '01/01/2000'
      }
    ]
  },
  {
    id: '4',
    name: 'scenario 4',
    tags: [],
    versions: [
      {
        id: 's4v1',
        state: SCENARIO_STATE.draft,
        creationDate: '01/01/1970'
      }
    ]
  },
  {
    id: '5',
    name: 'scenario 5',
    tags: [],
    versions: [
      {
        id: 's5v1',
        state: SCENARIO_STATE.draft,
        creationDate: '01/01/1970'
      }
    ]
  }
];

describe('ScenarioListSimpleComponent', () => {
  let component: ScenarioListSimpleComponent;
  let fixture: ComponentFixture<ScenarioListSimpleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScenarioListSimpleComponent],
      imports: [
        TestSharedModule,
        RouterTestingModule.withRoutes([
          { path: 'scenarios', component: ScenariosListComponent },
          { path: ':scenarioGroupId/:scenarioVersionId', component: ScenarioDesignerComponent },
          { path: 'scenarios/:scenarioGroupId/:scenarioVersionId', component: ScenarioDesignerComponent }
        ] as Routes)
      ],
      providers: [
        {
          provide: ScenarioService,
          useValue: {}
        },
        {
          provide: StateService,
          useValue: { currentApplication: { name: 'TestApplicationName' } }
        },
        { provide: DatePipe }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    jasmine.addMatchers(SpyOnCustomMatchers);
    fixture = TestBed.createComponent(ScenarioListSimpleComponent);
    component = fixture.componentInstance;
    component.scenariosGroups = mockScenariosGroups;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create as many entries as the list contains', () => {
    const listElement = fixture.debugElement.queryAll(By.css('[data-testid="list"]'));

    expect(listElement).toHaveSize(mockScenariosGroups.length);

    listElement.forEach((child, i) => {
      const textElement: HTMLSpanElement = child.nativeElement.querySelector('[data-testid="name"]');
      expect(textElement.textContent.trim()).toBe(mockScenariosGroups[i].name);
    });
  });

  it('should add css indicator when a scenario group is selected', () => {
    component.selectedScenarioGroup = mockScenariosGroups[1];
    fixture.detectChanges();
    const listElement = fixture.debugElement.queryAll(By.css('[data-testid="list"]'));

    listElement.forEach((child, i) => {
      const element = child.nativeElement;

      if (i === 1) expect(element).toHaveClass('selected');
      else expect(element).not.toHaveClass('selected');
    });
  });

  it('should emit the scenario group when clicking on the edit button of an item', () => {
    spyOn(component.onEditScenarioGroup, 'emit');
    const listElement = fixture.debugElement.queryAll(By.css('[data-testid="list"]'));
    const buttonElement: HTMLButtonElement = listElement[0].query(By.css('[data-testid="edit-scenario-group"]')).nativeElement;

    buttonElement.click();

    expect(component.onEditScenarioGroup.emit).toHaveBeenCalledOnceWith(mockScenariosGroups[0]);
  });

  it('should emit the scenario group when clicking on the delete button of an item', () => {
    spyOn(component.onDeleteScenarioGroup, 'emit');
    const listElement = fixture.debugElement.queryAll(By.css('[data-testid="list"]'));
    const buttonElement: HTMLButtonElement = listElement[0].query(By.css('[data-testid="delete-scenario-group"]')).nativeElement;

    buttonElement.click();

    expect(component.onDeleteScenarioGroup.emit).toHaveBeenCalledOnceWith(mockScenariosGroups[0]);
  });

  describe('redirect to the designer from the scenario group', () => {
    it('should call the method when clicking on the button', () => {
      spyOn(component, 'design');
      const listElement = fixture.debugElement.queryAll(By.css('[data-testid="list"]'));
      const buttonElement = listElement[0].query(By.css('[data-testid="open-last-scenario-version"]'));

      buttonElement.triggerEventHandler('click', new MouseEvent('click'));

      expect(component.design).toHaveBeenCalledOnceWith(new MouseEvent('click'), mockScenariosGroups[0]);
    });

    [
      {
        description: 'should return the last version in draft if there is at least one, regardless of the status of the other versions',
        mock: mockScenariosGroups[0],
        expectedResult: '/scenarios/1/s1v2'
      },
      {
        description: 'should return the current published version if it exists and there is no draft version',
        mock: mockScenariosGroups[1],
        expectedResult: '/scenarios/2/s2v2'
      },
      {
        description: 'should return last archived version if no version with other status exists',
        mock: mockScenariosGroups[2],
        expectedResult: '/scenarios/3/s3v3'
      }
    ].forEach((test) => {
      it(
        test.description,
        fakeAsync(() => {
          const location = TestBed.inject(Location);

          component.design(new MouseEvent('click'), test.mock);
          tick();

          expect(location.path()).toBe(test.expectedResult);
        })
      );
    });
  });

  /**
   * The tests in this section are made from the first element of the list of scenarios groups
   */
  describe('scenario version', () => {
    it('should create as many entries as the version list contains', () => {
      const listElement = fixture.debugElement.query(By.css('[data-testid="list"]'));
      const versionListElement = listElement.queryAll(By.css('[data-testid="list-versions"]'));

      expect(versionListElement).toHaveSize(mockScenariosGroups[0].versions.length);

      versionListElement.forEach((child, i) => {
        const textElement: HTMLSpanElement = child.nativeElement.querySelector('[data-testid="version-state"]');
        expect(textElement.textContent.trim()).toBe(mockScenariosGroups[0].versions[i].state);
      });
    });

    it('should redirect to the designer when clicking on the button of an item', fakeAsync(() => {
      const location = TestBed.inject(Location);
      const listElement = fixture.debugElement.query(By.css('[data-testid="list"]'));
      const versionListElement = listElement.queryAll(By.css('[data-testid="list-versions"]'));
      const buttonElement = versionListElement[0].query(By.css('[data-testid="open-scenario-version"]'));

      buttonElement.triggerEventHandler('click', new MouseEvent('click'));
      tick();

      expect(location.path()).toBe('/1/s1v1');
    }));

    it('should emit the scenario group and the version when clicking on the duplicate button of an item', () => {
      spyOn(component.onDuplicateScenarioVersion, 'emit');
      const listElement = fixture.debugElement.query(By.css('[data-testid="list"]'));
      const versionListElement = listElement.queryAll(By.css('[data-testid="list-versions"]'));
      const buttonElement: HTMLButtonElement = versionListElement[0].query(
        By.css('[data-testid="duplicate-scenario-version"]')
      ).nativeElement;

      buttonElement.click();

      expect(component.onDuplicateScenarioVersion.emit).toHaveBeenCalledOnceWith({
        scenarioGroup: mockScenariosGroups[0],
        scenarioVersion: mockScenariosGroups[0].versions[0]
      });
    });

    it('should emit the scenario group and the version when clicking on the delete button of an item', () => {
      spyOn(component.onDeleteScenarioVersion, 'emit');
      const listElement = fixture.debugElement.query(By.css('[data-testid="list"]'));
      const versionListElement = listElement.queryAll(By.css('[data-testid="list-versions"]'));
      const buttonElement: HTMLButtonElement = versionListElement[0].query(By.css('[data-testid="delete-scenario-version"]')).nativeElement;

      buttonElement.click();

      expect(component.onDeleteScenarioVersion.emit).toHaveBeenCalledOnceWith({
        scenarioGroup: mockScenariosGroups[0],
        scenarioVersion: mockScenariosGroups[0].versions[0]
      });
    });

    it('should call the method when clicking on the download button of an item', () => {
      spyOn(component, 'download');
      const listElement = fixture.debugElement.query(By.css('[data-testid="list"]'));
      const versionListElement = listElement.queryAll(By.css('[data-testid="list-versions"]'));
      const buttonElement: HTMLButtonElement = versionListElement[0].query(By.css('[data-testid="export-scenario-version"]')).nativeElement;

      buttonElement.click();

      expect(component.download).toHaveBeenCalledOnceWith(mockScenariosGroups[0], mockScenariosGroups[0].versions[0]);
    });
  });

  /**
   * if you click on the same criterion, the sorting is reversed
   */
  describe('#orderBy', () => {
    [
      { criteria: 'name', defaultReverse: true },
      { criteria: 'category', defaultReverse: false }
    ].forEach((test) => {
      it(`should emit the criteria when clicking on the order by ${test.criteria}`, () => {
        const spy = spyOn(component.onOrderBy, 'emit');
        const buttonElement: HTMLSpanElement = fixture.debugElement.query(
          By.css(`[data-testid="order-by-${test.criteria}"]`)
        ).nativeElement;

        buttonElement.click();

        expect(component.onOrderBy.emit).toHaveBeenCalledOnceWithDeepEquality({ criteria: test.criteria, reverse: test.defaultReverse });

        spy.calls.reset();
        buttonElement.click();

        expect(component.onOrderBy.emit).toHaveBeenCalledOnceWithDeepEquality({ criteria: test.criteria, reverse: !test.defaultReverse });
      });
    });
  });
});
