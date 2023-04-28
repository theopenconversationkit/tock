import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { Location } from '@angular/common';
import { Routes } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';
import {
  NbAccordionModule,
  NbButtonModule,
  NbDialogRef,
  NbDialogService,
  NbIconModule,
  NbSpinnerModule,
  NbTagModule,
  NbToggleModule,
  NbTooltipModule
} from '@nebular/theme';

import { ScenarioListSimpleComponent } from './scenario-list-simple.component';
import { ScenarioService } from '../../services';
import { ScenarioGroupExtended, SCENARIO_STATE } from '../../models';
import { StateService } from '../../../core-nlp/state.service';
import { ScenarioDesignerComponent } from '../../scenario-designer/scenario-designer.component';
import { ScenariosListComponent } from '../scenarios-list.component';
import { SpyOnCustomMatchers } from '../../../../testing/matchers/custom-matchers';
import { TestingModule } from '../../../../testing';
import { StubNbDialogService, StubStateService } from '../../../../testing/stubs';

const mockScenariosGroups: ScenarioGroupExtended[] = [
  {
    id: '1',
    name: 'scenario 1',
    tags: [],
    enabled: true,
    unknownAnswerId: 'app_test',
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
    enabled: false,
    unknownAnswerId: 'app_test',
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
    enabled: false,
    unknownAnswerId: 'app_test',
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
    enabled: false,
    unknownAnswerId: 'app_test',
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
    enabled: false,
    unknownAnswerId: 'app_test',
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
        TestingModule,
        RouterTestingModule.withRoutes([
          { path: 'scenarios', component: ScenariosListComponent },
          { path: ':scenarioGroupId/:scenarioVersionId', component: ScenarioDesignerComponent },
          { path: 'scenarios/:scenarioGroupId/:scenarioVersionId', component: ScenarioDesignerComponent }
        ] as Routes),
        NbAccordionModule,
        NbButtonModule,
        NbIconModule,
        NbSpinnerModule,
        NbTagModule,
        NbToggleModule,
        NbTooltipModule
      ],
      providers: [
        {
          provide: ScenarioService,
          useValue: { redirectToDesigner: () => {}, patchScenarioGroupState: () => {} }
        },
        { provide: StateService, useClass: StubStateService },
        { provide: NbDialogService, useClass: StubNbDialogService }
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

  describe('when click on the button to delete a scenario group', () => {
    it('should call the method', () => {
      spyOn(component, 'deleteScenarioGroup');

      const listElement = fixture.debugElement.queryAll(By.css('[data-testid="list"]'));
      const buttonElement: HTMLButtonElement = listElement[0].query(By.css('[data-testid="delete-scenario-group"]')).nativeElement;

      buttonElement.click();

      expect(component.deleteScenarioGroup).toHaveBeenCalledOnceWith(new PointerEvent('click'), mockScenariosGroups[0]);
    });

    it('should emit scenario group when confirmation message is confirmed', () => {
      spyOn(component['nbDialogService'], 'open').and.returnValue({ onClose: of('delete') } as NbDialogRef<any>);
      spyOn(component.onDeleteScenarioGroup, 'emit');

      component.deleteScenarioGroup(new PointerEvent('click'), mockScenariosGroups[0]);

      expect(component.onDeleteScenarioGroup.emit).toHaveBeenCalledOnceWith(mockScenariosGroups[0]);
    });

    it('should not emit scenario group when confirmation message is not confirmed', () => {
      spyOn(component['nbDialogService'], 'open').and.returnValue({ onClose: of('cancel') } as NbDialogRef<any>);
      spyOn(component.onDeleteScenarioGroup, 'emit');

      component.deleteScenarioGroup(new PointerEvent('click'), mockScenariosGroups[0]);

      expect(component.onDeleteScenarioGroup.emit).not.toHaveBeenCalledOnceWith(mockScenariosGroups[0]);
    });
  });

  describe('when click on the toggle button to activate / deactivate tick story from a scenario group', () => {
    it('should call the method', () => {
      spyOn(component, 'toggleTickEnabled');

      const listElement = fixture.debugElement.queryAll(By.css('[data-testid="list"]'));
      const toggleElement: HTMLElement = listElement[0].query(By.css('[data-testid="toggle-tick"]')).nativeElement;

      toggleElement.dispatchEvent(new Event('mousedown'));

      expect(component.toggleTickEnabled).toHaveBeenCalledOnceWith(mockScenariosGroups[0]);
    });

    it('should emit faq to disable when confirmation message is confirmed', () => {
      spyOn(component['nbDialogService'], 'open').and.returnValue({ onClose: of('disable') } as NbDialogRef<any>);
      spyOn(component.onToggleScenarioGroup, 'emit');

      component.toggleTickEnabled(mockScenariosGroups[0]);

      expect(component.onToggleScenarioGroup.emit).toHaveBeenCalledOnceWith(mockScenariosGroups[0]);
    });

    it('should emit faq to enable when confirmation message is confirmed', () => {
      spyOn(component['nbDialogService'], 'open').and.returnValue({ onClose: of('enable') } as NbDialogRef<any>);
      spyOn(component.onToggleScenarioGroup, 'emit');

      component.toggleTickEnabled(mockScenariosGroups[1]);

      expect(component.onToggleScenarioGroup.emit).toHaveBeenCalledOnceWith(mockScenariosGroups[1]);
    });

    it('should not emit faq when confirmation message is not confirmed', () => {
      spyOn(component['nbDialogService'], 'open').and.returnValue({ onClose: of('cancel') } as NbDialogRef<any>);
      spyOn(component.onToggleScenarioGroup, 'emit');

      component.toggleTickEnabled(mockScenariosGroups[0]);

      expect(component.onToggleScenarioGroup.emit).not.toHaveBeenCalledOnceWith(mockScenariosGroups[0]);
    });
  });

  describe('redirect to the designer from the scenario group', () => {
    it('should call the method when clicking on the button', () => {
      spyOn(component, 'design');
      const listElement = fixture.debugElement.queryAll(By.css('[data-testid="list"]'));
      const buttonElement = listElement[0].query(By.css('[data-testid="open-last-scenario-version"]'));

      buttonElement.triggerEventHandler('click', new PointerEvent('click'));

      expect(component.design).toHaveBeenCalledOnceWith(new PointerEvent('click'), mockScenariosGroups[0]);
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

    describe('when click on the button to delete a scenario version', () => {
      it('should call the method', () => {
        spyOn(component, 'deleteScenarioVersion');

        const listElement = fixture.debugElement.query(By.css('[data-testid="list"]'));
        const versionListElement = listElement.queryAll(By.css('[data-testid="list-versions"]'));
        const buttonElement: HTMLButtonElement = versionListElement[0].query(
          By.css('[data-testid="delete-scenario-version"]')
        ).nativeElement;

        buttonElement.click();

        expect(component.deleteScenarioVersion).toHaveBeenCalledOnceWith(
          new PointerEvent('click'),
          mockScenariosGroups[0],
          mockScenariosGroups[0].versions[0]
        );
      });

      it('should emit scenario group and the version when confirmation message is confirmed', () => {
        spyOn(component['nbDialogService'], 'open').and.returnValue({ onClose: of('delete') } as NbDialogRef<any>);
        spyOn(component.onDeleteScenarioVersion, 'emit');

        component.deleteScenarioVersion(new PointerEvent('click'), mockScenariosGroups[0], mockScenariosGroups[0].versions[0]);

        expect(component.onDeleteScenarioVersion.emit).toHaveBeenCalledOnceWith({
          scenarioGroup: mockScenariosGroups[0],
          scenarioVersion: mockScenariosGroups[0].versions[0]
        });
      });

      it('should not emit scenario group and the version when confirmation message is not confirmed', () => {
        spyOn(component['nbDialogService'], 'open').and.returnValue({ onClose: of('cancel') } as NbDialogRef<any>);
        spyOn(component.onDeleteScenarioVersion, 'emit');

        component.deleteScenarioVersion(new PointerEvent('click'), mockScenariosGroups[0], mockScenariosGroups[0].versions[0]);

        expect(component.onDeleteScenarioVersion.emit).not.toHaveBeenCalledOnceWith({
          scenarioGroup: mockScenariosGroups[0],
          scenarioVersion: mockScenariosGroups[0].versions[0]
        });
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
