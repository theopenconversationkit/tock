import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import {
  NbButtonModule,
  NbCardModule,
  NbCheckboxModule,
  NbFormFieldModule,
  NbIconModule,
  NbInputModule,
  NbOptionModule,
  NbSelectModule
} from '@nebular/theme';
import { of } from 'rxjs';

import { ScenarioFiltersComponent } from './scenario-filters.component';
import { ScenarioService } from '../../services';
import { TestingModule } from '../../../../testing';
import { SpyOnCustomMatchers } from '../../../../testing/matchers';

describe('ScenarioFiltersComponent', () => {
  let component: ScenarioFiltersComponent;
  let fixture: ComponentFixture<ScenarioFiltersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        TestingModule,
        NbButtonModule,
        NbCardModule,
        NbCheckboxModule,
        NbFormFieldModule,
        NbIconModule,
        NbInputModule,
        NbOptionModule,
        NbSelectModule
      ],
      declarations: [ScenarioFiltersComponent],
      providers: [
        {
          provide: ScenarioService,
          useValue: {
            state$: of({
              tags: ['tag1', 'tag2']
            })
          }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    jasmine.addMatchers(SpyOnCustomMatchers);
    fixture = TestBed.createComponent(ScenarioFiltersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not show clear button when no filters are active', () => {
    component.form.patchValue({ search: '', tags: [], enabled: null });
    fixture.detectChanges();
    let element = fixture.debugElement.query(By.css('[data-testid="clear-button"]'));

    expect(element).toBeFalsy();
    expect(component.isFiltered).toBeFalse();
  });

  /**
   * TODO fix test
   * Error: Can't assign single value if select is marked as multiple
   * Waiting upgrade of nebular to last version
   */
  describe('should show clear button when at least one filter is active', () => {
    [
      { description: 'search active', formValue: { search: 'test', tags: [], enabled: null } },
      { description: 'tags active', formValue: { search: '', tags: ['tag1', 'tag2'], enabled: false } },
      { description: 'all field active', formValue: { search: 'test', tags: ['tag1', 'tag2'], enabled: true } }
    ].forEach((parameter) => {
      it(parameter.description, () => {
        component.form.patchValue(parameter.formValue);
        component.tags.setValue([]);
        fixture.detectChanges();
        const element = fixture.debugElement.query(By.css('[data-testid="clear-button"]'));

        expect(element).toBeTruthy();
      });
    });
  });

  /**
   * TODO fix test
   * Error: Can't assign single value if select is marked as multiple
   * Waiting upgrade of nebular to last version
   */
  describe('should clear form when the method is called', () => {
    [
      { description: 'search active', formValue: { search: 'test', tags: [], enabled: null } },
      { description: 'tags active', formValue: { search: '', tags: ['tag1', 'tag2'], enabled: false } },
      { description: 'all field active', formValue: { search: 'test', tags: ['tag1', 'tag2'], enabled: true } }
    ].forEach((parameter) => {
      it(parameter.description, () => {
        component.form.patchValue(parameter.formValue);

        component.clearFilters();

        expect(component.search.value).toBeNull();
        expect(component.tags.value).toEqual([]);
        expect(component.enabled.value).toBeNull();
      });
    });
  });

  it('should emit the filters after 500ms after one of them is changed', fakeAsync(() => {
    const onFilterSpy = spyOn(component.onFilter, 'emit');

    expect(onFilterSpy).not.toHaveBeenCalled();

    component.form.patchValue({ search: 'test' });
    fixture.detectChanges();

    tick(400);
    expect(onFilterSpy).not.toHaveBeenCalled();

    tick(500);
    expect(onFilterSpy).toHaveBeenCalledOnceWithDeepEquality({ search: 'test', tags: [], enabled: null });

    onFilterSpy.calls.reset();
    tick(600);

    expect(onFilterSpy).not.toHaveBeenCalled();

    component.form.patchValue({ tags: ['test'] });
    fixture.detectChanges();

    tick(400);
    expect(onFilterSpy).not.toHaveBeenCalled();

    tick(500);
    expect(onFilterSpy).toHaveBeenCalledOnceWithDeepEquality({ search: 'test', tags: ['test'], enabled: null });

    onFilterSpy.calls.reset();
    tick(600);

    expect(onFilterSpy).not.toHaveBeenCalled();
  }));

  it('should update enabled field when the method is called', () => {
    // null to true
    component.enabled.setValue(null);
    component.enabledCheckChanged();
    expect(component.enabled.value).toBeTrue();

    // true to false
    component.enabledCheckChanged();
    expect(component.enabled.value).toBeFalse();

    // false to null
    component.enabledCheckChanged();
    expect(component.enabled.value).toBeNull();
  });
});
