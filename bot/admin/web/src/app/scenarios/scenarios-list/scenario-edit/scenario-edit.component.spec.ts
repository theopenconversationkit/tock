import { NO_ERRORS_SCHEMA, SimpleChange } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { NbDialogRef, NbDialogService } from '@nebular/theme';
import { of } from 'rxjs';

import { ScenarioGroup } from '../../models';
import { ScenarioService } from '../../services';
import { ScenarioEditComponent } from './scenario-edit.component';
import { SpyOnCustomMatchers } from '../../../../testing/matchers/custom-matchers';
import { NbDialogServiceMock } from '../../../../testing/classMocked';
import { AutocompleteInputComponent, FormControlComponent } from '../../../shared/components';

class MockScenarioService {
  getState() {
    return {
      categories: ['category 1', 'category 2'],
      tags: ['tag 1', 'tag 2', 'tag 3']
    };
  }
}

const mockScenarioGroup = {
  id: 'abc',
  name: 'scenario',
  category: 'technology',
  description: 'description of scenario',
  tags: ['tag1', 'tag2']
} as ScenarioGroup;

describe('ScenarioEditComponent', () => {
  let component: ScenarioEditComponent;
  let fixture: ComponentFixture<ScenarioEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScenarioEditComponent, AutocompleteInputComponent, FormControlComponent],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: NbDialogService, useClass: NbDialogServiceMock },
        { provide: ScenarioService, useClass: MockScenarioService }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    jasmine.addMatchers(SpyOnCustomMatchers);
    fixture = TestBed.createComponent(ScenarioEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize an empty form when creating a new scenario group', () => {
    const scenarioGroup = {
      id: null,
      name: '',
      category: '',
      description: '',
      tags: []
    } as ScenarioGroup;

    component.ngOnChanges({ scenarioGroup: new SimpleChange(null, scenarioGroup, true) });
    fixture.detectChanges();

    expect(component.form.valid).toBeFalse();
    expect(component.form.value).toEqual({
      name: '',
      category: '',
      description: '',
      tags: []
    });
  });

  it('should initialize a form when editing a group of scenarios', () => {
    component.ngOnChanges({ scenarioGroup: new SimpleChange(null, mockScenarioGroup, true) });
    fixture.detectChanges();

    expect(component.form.valid).toBeTrue();
    expect(component.form.value).toEqual({
      name: 'scenario',
      category: 'technology',
      description: 'description of scenario',
      tags: ['tag1', 'tag2']
    });
  });

  it('should associate validators to the name', () => {
    expect(component.name.valid).toBeFalse();

    // name is required
    expect(component.name.errors.required).toBeTruthy();

    // set name to something correct
    component.name.setValue('title');
    expect(component.name.errors).toBeFalsy();
    expect(component.name.valid).toBeTrue();
  });

  describe('#close', () => {
    it('should call the onClose method without displaying a confirmation request message when the form is not dirty', () => {
      spyOn(component['nbDialogService'], 'open').and.returnValue({ onClose: of('yes') } as NbDialogRef<any>);
      spyOn(component.onClose, 'emit');

      component.close();

      expect(component['nbDialogService'].open).not.toHaveBeenCalled();
      expect(component.onClose.emit).toHaveBeenCalledOnceWith(true);
    });

    it('should call the onClose method after displaying a confirmation request message and confirm when the form is dirty', () => {
      spyOn(component['nbDialogService'], 'open').and.returnValue({ onClose: of('yes') } as NbDialogRef<any>);
      spyOn(component.onClose, 'emit');

      // To display the confirmation message, the form must have been modified
      component.form.markAsDirty();
      component.close();

      expect(component['nbDialogService'].open).toHaveBeenCalled();
      expect(component.onClose.emit).toHaveBeenCalledOnceWith(true);
    });

    it('should not call the onClose method after displaying a confirmation request message and cancel when the form is dirty', () => {
      spyOn(component['nbDialogService'], 'open').and.returnValue({ onClose: of('cancel') } as NbDialogRef<any>);
      spyOn(component.onClose, 'emit');

      // To display the confirmation message, the form must have been modified
      component.form.markAsDirty();
      component.close();

      expect(component['nbDialogService'].open).toHaveBeenCalled();
      expect(component.onClose.emit).not.toHaveBeenCalled();
    });
  });

  describe('#save', () => {
    it('should not call the onSave method when the form is not valid', () => {
      spyOn(component.onSave, 'emit');
      const scenarioGroup = {
        name: '',
        category: 'scenario',
        description: 'description',
        tags: []
      } as ScenarioGroup;

      component.ngOnChanges({ scenarioGroup: new SimpleChange(null, scenarioGroup, true) });
      fixture.detectChanges();

      expect(component.form.valid).toBeFalse();
      expect(component.onSave.emit).not.toHaveBeenCalled();
    });

    it('should call the onSave method when the form is valid and not redirect if not specified', () => {
      spyOn(component.onSave, 'emit');
      const scenarioGroup = {
        id: null,
        name: 'scenario 1',
        category: 'scenario',
        description: 'description',
        enabled: false,
        tags: []
      } as ScenarioGroup;
      component.scenarioGroup = scenarioGroup;
      component.ngOnChanges({ scenarioGroup: new SimpleChange(null, scenarioGroup, true) });
      fixture.detectChanges();

      component.save();

      expect(component.form.valid).toBeTrue();
      expect(component.onSave.emit).toHaveBeenCalledOnceWithDeepEquality({
        redirect: false,
        scenarioGroup
      });
    });

    it('should call the onSave method when the form is valid and redirect if specified', () => {
      spyOn(component.onSave, 'emit');
      const scenarioGroup = {
        id: null,
        name: 'scenario 1',
        category: 'scenario',
        description: 'description',
        enabled: false,
        tags: []
      } as ScenarioGroup;
      component.scenarioGroup = scenarioGroup;
      component.ngOnChanges({ scenarioGroup: new SimpleChange(null, scenarioGroup, true) });
      fixture.detectChanges();

      component.save(true);

      expect(component.form.valid).toBeTrue();
      expect(component.onSave.emit).toHaveBeenCalledOnceWithDeepEquality({
        redirect: true,
        scenarioGroup
      });
    });
  });

  it('should populate the categories array for the autocompletion with the elements stored in the state of the scenario when the component is initialized', () => {
    component.ngOnChanges({ scenarioGroup: new SimpleChange(null, null, true) });
    fixture.detectChanges();

    expect(component.categories).toHaveSize(2);
    expect(component.categories).toEqual(['category 1', 'category 2']);
  });

  it('should populate the tags array for the autocompletion with the elements stored in the state of the scenario when the component is initialized', (done) => {
    component.ngOnChanges({ scenarioGroup: new SimpleChange(null, null, true) });
    fixture.detectChanges();

    component.tagsAutocompleteValues.subscribe((v) => {
      expect(v).toHaveSize(3);
      expect(v).toEqual(['tag 1', 'tag 2', 'tag 3']);
      done();
    });
  });
});
